Page({
  data: { result: null, json: '' },

  onLoad() {
    const result = getApp().globalData.latestResult || wx.getStorageSync('latest_result')
    if (!result) {
      wx.reLaunch({ url: '/pages/home/index' })
      return
    }
    this.setData({ result, json: JSON.stringify(result, null, 2) })
  },

  home() {
    wx.reLaunch({ url: '/pages/home/index' })
  }
})
