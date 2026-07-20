const auth = require('../../utils/auth')
const request = require('../../utils/request')

const DRAFT_KEY = 'quiz_draft'

function parseInvite(options) {
  if (options.inviteCode) return options.inviteCode.toUpperCase()
  if (!options.scene) return ''
  try {
    const scene = decodeURIComponent(options.scene)
    const match = scene.match(/(?:^|&)i=([23456789ABCDEFGHJKMNPQRSTUVWXYZ]{8})(?:&|$)/)
    return match ? match[1] : ''
  } catch (error) {
    return ''
  }
}

Page({
  data: {
    phase: 'loading',
    inviteCode: '',
    questions: [],
    scaleOptions: [],
    config: null,
    currentIndex: 0,
    currentQuestion: null,
    currentAnswer: 0,
    progress: 0,
    answers: {},
    boosted: [],
    boostedText: '',
    boostCandidates: [],
    expandAll: false,
    submitting: false
  },

  onLoad(options) {
    const inviteCode = parseInvite(options)
    if (!inviteCode) {
      this.exitWithError('邀请二维码或邀请码格式不正确')
      return
    }
    this.pendingDraft = null
    this.setData({ inviteCode })
    auth.ensureLogin()
      .then(() => request({ url: '/invite/verify', method: 'POST', data: { code: inviteCode } }))
      .then(() => request({ url: '/questions' }))
      .then(data => this.prepare(data))
      .catch(error => this.exitWithError(error.message || '无法开始测评'))
  },

  prepare(data) {
    const scaleOptions = data.config.scaleLabels.map((label, index) => ({ value: index + 1, label }))
    const draft = wx.getStorageSync(DRAFT_KEY)
    if (draft && draft.inviteCode === this.data.inviteCode) {
      this.pendingDraft = draft
    }
    this.setData({
      phase: 'intro',
      questions: data.questions,
      config: data.config,
      scaleOptions,
      currentQuestion: data.questions[0],
      progress: 2.5
    })
  },

  startQuiz() {
    if (!this.pendingDraft) {
      this.setData({ phase: 'quiz' })
      return
    }
    wx.showModal({
      title: '发现上次作答',
      content: '是否继续上次未完成的测评？',
      confirmText: '继续作答',
      cancelText: '重新开始',
      success: result => {
        if (result.confirm) {
          this.restoreDraft(this.pendingDraft)
        } else {
          wx.removeStorageSync(DRAFT_KEY)
          this.pendingDraft = null
          this.setData({ phase: 'quiz', answers: {}, boosted: [], boostedText: '', expandAll: false })
        }
      }
    })
  },

  restoreDraft(draft) {
    const answers = draft.answers || {}
    const firstUnanswered = this.data.questions.findIndex(question => !answers[question.id])
    const currentIndex = firstUnanswered === -1 ? this.data.questions.length - 1 : firstUnanswered
    const boosted = draft.boosted || []
    this.setData({ answers, boosted, boostedText: boosted.join('、'), phase: 'quiz' })
    this.showQuestion(currentIndex)
  },

  selectAnswer(event) {
    const value = Number(event.currentTarget.dataset.value)
    const questionId = this.data.currentQuestion.id
    const answers = Object.assign({}, this.data.answers, { [questionId]: value })
    this.setData({ answers, currentAnswer: value })
    this.saveDraft(answers, this.data.boosted)
    setTimeout(() => {
      if (this.data.currentIndex === this.data.questions.length - 1) {
        this.openBoost()
      } else {
        this.showQuestion(this.data.currentIndex + 1)
      }
    }, 300)
  },

  previous() {
    if (this.data.currentIndex > 0) this.showQuestion(this.data.currentIndex - 1)
  },

  next() {
    if (!this.data.currentAnswer) {
      wx.showToast({ title: '请先选择符合程度', icon: 'none' })
      return
    }
    if (this.data.currentIndex === this.data.questions.length - 1) this.openBoost()
    else this.showQuestion(this.data.currentIndex + 1)
  },

  showQuestion(index) {
    const question = this.data.questions[index]
    this.setData({
      currentIndex: index,
      currentQuestion: question,
      currentAnswer: this.data.answers[question.id] || 0,
      progress: Math.round(((index + 1) / this.data.questions.length) * 100)
    })
  },

  openBoost() {
    const unanswered = this.data.questions.filter(question => !this.data.answers[question.id])
    if (unanswered.length) {
      wx.showToast({ title: `还有 ${unanswered.length} 题未作答`, icon: 'none' })
      this.showQuestion(this.data.questions.indexOf(unanswered[0]))
      return
    }
    this.refreshBoostCandidates(this.data.boosted, this.data.expandAll)
    this.setData({ phase: 'boost' })
  },

  toggleBoost(event) {
    const questionId = Number(event.currentTarget.dataset.id)
    const boosted = this.data.boosted.slice()
    const index = boosted.indexOf(questionId)
    if (index >= 0) boosted.splice(index, 1)
    else if (boosted.length < this.data.config.boostCount) boosted.push(questionId)
    else {
      wx.showToast({ title: `最多选择 ${this.data.config.boostCount} 题`, icon: 'none' })
      return
    }
    this.setData({ boosted, boostedText: boosted.join('、') })
    this.refreshBoostCandidates(boosted, this.data.expandAll)
    this.saveDraft(this.data.answers, boosted)
  },

  toggleExpand() {
    const expandAll = !this.data.expandAll
    this.setData({ expandAll })
    this.refreshBoostCandidates(this.data.boosted, expandAll)
  },

  refreshBoostCandidates(boosted, expandAll) {
    const candidates = this.data.questions
      .map(question => ({
        id: question.id,
        content: question.content,
        score: this.data.answers[question.id],
        selected: boosted.includes(question.id)
      }))
      .sort((left, right) => right.score - left.score || left.id - right.id)
      .filter(item => expandAll || item.score >= 5 || item.selected)
    this.setData({ boostCandidates: candidates })
  },

  review() {
    if (this.data.boosted.length !== this.data.config.boostCount) {
      wx.showToast({ title: `请选择恰好 ${this.data.config.boostCount} 题`, icon: 'none' })
      return
    }
    this.setData({ phase: 'review' })
  },

  backToBoost() {
    this.setData({ phase: 'boost' })
  },

  submit() {
    if (this.data.submitting) return
    const answers = this.data.questions.map(question => ({ q: question.id, v: this.data.answers[question.id] }))
    this.setData({ submitting: true })
    request({
      url: '/tests/submit',
      method: 'POST',
      data: { inviteCode: this.data.inviteCode, answers, boosted: this.data.boosted }
    }).then(result => {
      wx.removeStorageSync(DRAFT_KEY)
      getApp().globalData.latestResult = result
      wx.setStorageSync('latest_result', result)
      wx.redirectTo({ url: `/pages/report/index?resultId=${result.id}` })
    }).catch(error => {
      wx.showModal({ title: '提交失败', content: error.message || '请稍后重试', showCancel: false })
    }).finally(() => this.setData({ submitting: false }))
  },

  saveDraft(answers, boosted) {
    wx.setStorageSync(DRAFT_KEY, { inviteCode: this.data.inviteCode, answers, boosted, updatedAt: Date.now() })
  },

  exitWithError(message) {
    wx.showToast({ title: message, icon: 'none', duration: 2200 })
    setTimeout(() => wx.reLaunch({ url: '/pages/home/index' }), 2200)
  }
})
