const request = require('../../utils/request')

Page({
  data: { resultCount: 0, countLoading: true },

  onShow() {
    this.setData({ countLoading: true })
    request({ url: '/results?page=1&size=1' })
      .then(data => this.setData({ resultCount: data.total, countLoading: false }))
      .catch(() => this.setData({ countLoading: false }))
  },

  start() {
    wx.navigateTo({ url: '/pages/invite/index' })
  },

  history() {
    wx.navigateTo({ url: '/pages/history/index' })
  },

  admin() {
    wx.navigateTo({ url: '/pages/admin/login/index' })
  }
})
