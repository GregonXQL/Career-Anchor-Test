const auth = require('../../utils/auth')
const privacy = require('../../utils/privacy')
const request = require('../../utils/request')
const { parseInviteOptions } = require('../../utils/invite')
const { isProfileComplete } = require('../../utils/user-profile')

Page({
  data: {
    loading: false,
    privacyVisible: false,
    privacyError: ''
  },

  onLoad(options) {
    const inviteCode = options.redirect === 'quiz' ? parseInviteOptions(options) : ''
    this.redirectUrl = inviteCode ? `/pages/quiz/index?inviteCode=${inviteCode}` : ''
    this.pendingEnter = Boolean(this.redirectUrl)
    this.setData({ privacyVisible: !privacy.hasAccepted() })
  },

  onShow() {
    if (auth.getToken() && privacy.hasAccepted()) {
      this.goNext()
    } else if (this.redirectUrl && privacy.hasAccepted() && !this.data.loading) {
      this.pendingEnter = false
      this.login()
    }
  },

  enter() {
    if (this.data.loading) return
    if (!privacy.hasAccepted()) {
      this.pendingEnter = true
      this.setData({ privacyVisible: true, privacyError: '' })
      return
    }
    this.login()
  },

  login() {
    this.setData({ loading: true })
    auth.login()
      .then(() => this.goNext())
      .catch(error => wx.showToast({ title: error.message || '登录失败', icon: 'none' }))
      .finally(() => this.setData({ loading: false }))
  },

  agreePrivacy() {
    if (this.data.loading) return
    this.setData({ loading: true, privacyError: '' })
    privacy.authorize()
      .then(() => {
        this.setData({ privacyVisible: false })
        if (auth.getToken()) {
          this.goNext()
        } else if (this.pendingEnter) {
          this.pendingEnter = false
          this.login()
        } else {
          this.setData({ loading: false })
        }
      })
      .catch(error => this.setData({ privacyError: error.message, loading: false }))
  },

  goNext() {
    if (this.checkingProfile) return
    this.checkingProfile = true
    const target = this.redirectUrl || '/pages/home/index'
    request({ url: '/users/me' })
      .then(profile => {
        const url = isProfileComplete(profile)
          ? target
          : `/pages/profile/index?redirect=${encodeURIComponent(target)}`
        wx.reLaunch({ url })
      })
      .catch(error => wx.showToast({ title: error.message || '用户资料加载失败', icon: 'none' }))
      .finally(() => { this.checkingProfile = false })
  },

  rejectPrivacy() {
    this.pendingEnter = false
    this.setData({
      privacyVisible: false,
      privacyError: '你可以稍后点击“微信一键进入”，重新阅读并同意隐私保护指引。'
    })
  },

  openPrivacyContract() {
    privacy.openContract()
      .catch(error => wx.showToast({ title: error.message || '暂时无法打开隐私保护指引', icon: 'none' }))
  },

  admin() {
    if (!privacy.hasAccepted()) {
      this.setData({ privacyVisible: true, privacyError: '' })
      return
    }
    wx.navigateTo({ url: '/pages/admin/login/index' })
  }
})
