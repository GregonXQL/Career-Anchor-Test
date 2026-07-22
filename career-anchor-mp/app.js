App({
  onLaunch() {
    if (!wx.cloud) {
      console.error('当前微信基础库不支持 wx.cloud')
      return
    }
    wx.cloud.init()
  },

  globalData: {
    latestResult: null
  }
})
