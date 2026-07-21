const auth = require('../../../utils/auth')
const { requestAdmin } = require('../../../utils/admin')
const { resultViews, inviteViews, resultQuery } = require('../../../utils/admin-view')

const PAGE_SIZE = 10

Page({
  data: {
    tab: 'overview',
    stats: null,
    loading: true,
    error: '',
    results: [],
    resultTotal: 0,
    resultPage: 0,
    resultHasMore: true,
    keyword: '',
    from: '',
    to: '',
    invites: [],
    inviteTotal: 0,
    invitePage: 0,
    inviteHasMore: true,
    inviteStatus: 'ALL',
    statusOptions: [
      { value: 'ALL', label: '全部' },
      { value: 'ACTIVE', label: '有效' },
      { value: 'USED', label: '已用尽' },
      { value: 'EXPIRED', label: '已过期' },
      { value: 'DISABLED', label: '已停用' }
    ],
    manualCount: '10',
    manualExpires: '',
    manualRemark: '',
    qrMaxUses: '1',
    qrExpires: '',
    qrRemark: '',
    creating: false,
    createdCodes: [],
    createdCodesText: '',
    qrImage: '',
    qrCode: ''
  },

  onLoad() {
    if (!auth.getAdminToken()) {
      wx.redirectTo({ url: '/pages/admin/login/index' })
      return
    }
    this.loadStats()
  },

  onReachBottom() {
    if (this.data.tab === 'results' && this.data.resultHasMore && !this.data.loading) {
      this.loadResults(false)
    }
    if (this.data.tab === 'invites' && this.data.inviteHasMore && !this.data.loading) {
      this.loadInvites(false)
    }
  },

  switchTab(event) {
    const tab = event.currentTarget.dataset.tab
    this.setData({ tab, error: '' })
    if (tab === 'overview') this.loadStats()
    if (tab === 'results' && !this.data.results.length) this.loadResults(true)
    if (tab === 'invites' && !this.data.invites.length) this.loadInvites(true)
  },

  loadStats() {
    this.setData({ loading: true, error: '' })
    requestAdmin({ url: '/admin/stats' })
      .then(stats => this.setData({ stats, loading: false }))
      .catch(error => this.setData({ loading: false, error: error.message || '概览加载失败' }))
  },

  retryCurrent() {
    if (this.data.tab === 'overview') this.loadStats()
    if (this.data.tab === 'results') this.loadResults(true)
    if (this.data.tab === 'invites') this.loadInvites(true)
  },

  loadResults(reset) {
    const page = reset ? 1 : this.data.resultPage + 1
    this.setData({ loading: true, error: '' })
    const query = resultQuery(this.data, page, PAGE_SIZE)
    requestAdmin({ url: `/admin/results?${query}` })
      .then(data => {
        const next = resultViews(data.records)
        const results = reset ? next : this.data.results.concat(next)
        this.setData({
          results,
          resultTotal: data.total,
          resultPage: page,
          resultHasMore: results.length < data.total,
          loading: false
        })
      })
      .catch(error => this.setData({ loading: false, error: error.message || '结果加载失败' }))
  },

  searchInput(event) { this.setData({ keyword: event.detail.value }) },
  chooseFrom(event) { this.setData({ from: event.detail.value }) },
  chooseTo(event) { this.setData({ to: event.detail.value }) },
  clearDates() { this.setData({ from: '', to: '' }) },
  searchResults() { this.loadResults(true) },

  openResult(event) {
    wx.navigateTo({ url: `/pages/report/index?resultId=${event.currentTarget.dataset.id}&admin=1` })
  },

  loadInvites(reset) {
    const page = reset ? 1 : this.data.invitePage + 1
    this.setData({ loading: true, error: '' })
    requestAdmin({ url: `/admin/invites?page=${page}&size=${PAGE_SIZE}&status=${this.data.inviteStatus}` })
      .then(data => {
        const next = inviteViews(data.records)
        const invites = reset ? next : this.data.invites.concat(next)
        this.setData({
          invites,
          inviteTotal: data.total,
          invitePage: page,
          inviteHasMore: invites.length < data.total,
          loading: false
        })
      })
      .catch(error => this.setData({ loading: false, error: error.message || '邀请码加载失败' }))
  },

  selectStatus(event) {
    this.setData({ inviteStatus: event.currentTarget.dataset.status }, () => this.loadInvites(true))
  },

  manualCountInput(event) { this.setData({ manualCount: event.detail.value }) },
  manualRemarkInput(event) { this.setData({ manualRemark: event.detail.value }) },
  manualExpiresChange(event) { this.setData({ manualExpires: event.detail.value }) },
  clearManualExpires() { this.setData({ manualExpires: '' }) },
  qrMaxUsesInput(event) { this.setData({ qrMaxUses: event.detail.value }) },
  qrRemarkInput(event) { this.setData({ qrRemark: event.detail.value }) },
  qrExpiresChange(event) { this.setData({ qrExpires: event.detail.value }) },
  clearQrExpires() { this.setData({ qrExpires: '' }) },

  createManual() {
    const count = Number(this.data.manualCount)
    if (!Number.isInteger(count) || count < 1 || count > 100) {
      wx.showToast({ title: '数量需为 1–100', icon: 'none' })
      return
    }
    this.setData({ creating: true })
    requestAdmin({
      url: '/admin/invites',
      method: 'POST',
      data: {
        count,
        expiresAt: this.data.manualExpires ? `${this.data.manualExpires}T23:59:59` : null,
        remark: this.data.manualRemark
      }
    }).then(invites => {
      const createdCodes = invites.map(invite => invite.code)
      this.setData({ createdCodes, createdCodesText: createdCodes.join('\n') })
      this.loadInvites(true)
      wx.showToast({ title: `已生成 ${createdCodes.length} 个`, icon: 'success' })
    }).catch(error => wx.showToast({ title: error.message || '生成失败', icon: 'none' }))
      .finally(() => this.setData({ creating: false }))
  },

  copyCreated() {
    wx.setClipboardData({ data: this.data.createdCodes.join('\n') })
  },

  createQr() {
    const maxUses = Number(this.data.qrMaxUses)
    if (!Number.isInteger(maxUses) || maxUses < 1 || maxUses > 10000) {
      wx.showToast({ title: '次数需为 1–10000', icon: 'none' })
      return
    }
    this.setData({ creating: true })
    requestAdmin({
      url: '/admin/invites/qr',
      method: 'POST',
      data: {
        maxUses,
        expiresAt: this.data.qrExpires ? `${this.data.qrExpires}T23:59:59` : null,
        remark: this.data.qrRemark
      }
    }).then(data => {
      this.setData({ qrImage: data.qrImage, qrCode: data.invite.code })
      this.loadInvites(true)
    }).catch(error => wx.showToast({ title: error.message || '二维码生成失败', icon: 'none' }))
      .finally(() => this.setData({ creating: false }))
  },

  copyCode(event) {
    wx.setClipboardData({ data: event.currentTarget.dataset.code })
  },

  generateQr(event) {
    const id = event.currentTarget.dataset.id
    const code = event.currentTarget.dataset.code
    requestAdmin({ url: `/admin/invites/${id}/qrcode` })
      .then(data => this.setData({ qrImage: data.qrImage, qrCode: code }))
      .catch(error => wx.showToast({ title: error.message || '生成失败', icon: 'none' }))
  },

  disableInvite(event) {
    const id = event.currentTarget.dataset.id
    wx.showModal({
      title: '停用邀请码',
      content: '停用后对应小程序码也会立即失效，是否继续？',
      success: result => {
        if (!result.confirm) return
        requestAdmin({ url: `/admin/invites/${id}/disable`, method: 'PATCH' })
          .then(() => this.loadInvites(true))
          .catch(error => wx.showToast({ title: error.message || '停用失败', icon: 'none' }))
      }
    })
  },

  closeQr() { this.setData({ qrImage: '', qrCode: '' }) },

  noop() {},

  saveQr() {
    this.writeQr(filePath => wx.saveImageToPhotosAlbum({
      filePath,
      success: () => wx.showToast({ title: '已保存到相册', icon: 'success' }),
      fail: error => wx.showToast({ title: error.errMsg || '保存失败，请检查相册权限', icon: 'none' })
    }))
  },

  shareQr() {
    if (!wx.showShareImageMenu) {
      wx.showToast({ title: '当前微信版本暂不支持图片转发', icon: 'none' })
      return
    }
    this.writeQr(filePath => wx.showShareImageMenu({
      path: filePath,
      fail: error => wx.showToast({ title: error.errMsg || '转发失败', icon: 'none' })
    }))
  },

  writeQr(onReady) {
    const base64 = String(this.data.qrImage).replace(/^data:image\/png;base64,/, '')
    const filePath = `${wx.env.USER_DATA_PATH}/invite-${this.data.qrCode}.png`
    wx.getFileSystemManager().writeFile({
      filePath,
      data: base64,
      encoding: 'base64',
      success: () => onReady(filePath),
      fail: () => wx.showToast({ title: '图片写入失败', icon: 'none' })
    })
  },

  logout() {
    auth.clearAdminToken()
    wx.redirectTo({ url: '/pages/admin/login/index' })
  }
})
