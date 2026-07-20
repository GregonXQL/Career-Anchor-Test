const auth = require('../../utils/auth')

Page({
  data: { loading: false },

  onShow() {
    if (auth.getToken()) {
      wx.reLaunch({ url: '/pages/home/index' })
    }
  },

  enter() {
    if (this.data.loading) return
    this.setData({ loading: true })
    auth.login()
      .then(() => wx.reLaunch({ url: '/pages/home/index' }))
      .catch(error => wx.showToast({ title: error.message || '登录失败', icon: 'none' }))
      .finally(() => this.setData({ loading: false }))
  },

  admin() {
    wx.navigateTo({ url: '/pages/admin/login/index' })
  }
})
