const request = require('../../utils/request')

Page({
  data: { resultCount: 0 },

  onShow() {
    request({ url: '/results?page=1&size=1' })
      .then(data => this.setData({ resultCount: data.total }))
      .catch(() => {})
  },

  start() {
    wx.navigateTo({ url: '/pages/invite/index' })
  },

  history() {
    wx.navigateTo({ url: '/pages/history/index' })
  }
})
