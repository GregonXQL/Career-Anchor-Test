const auth = require('../../../utils/auth')
const admin = require('../../../utils/admin')

Page({
  data: {
    password: '',
    loading: false,
    error: ''
  },

  onLoad() {
    if (auth.getAdminToken()) {
      wx.redirectTo({ url: '/pages/admin/dashboard/index' })
    }
  },

  input(event) {
    this.setData({ password: event.detail.value, error: '' })
  },

  submit() {
    if (!this.data.password || this.data.loading) return
    this.setData({ loading: true, error: '' })
    admin.login(this.data.password)
      .then(() => wx.redirectTo({ url: '/pages/admin/dashboard/index' }))
      .catch(error => this.setData({ error: error.message || '管理员登录失败' }))
      .finally(() => this.setData({ loading: false }))
  }
})
