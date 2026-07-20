const request = require('../../utils/request')

Page({
  data: { loading: true, records: [], total: 0 },

  onLoad() {
    request({ url: '/results?page=1&size=50' })
      .then(data => this.setData({ records: data.records, total: data.total }))
      .catch(error => wx.showToast({ title: error.message || '加载失败', icon: 'none' }))
      .finally(() => this.setData({ loading: false }))
  },

  open(event) {
    const id = event.currentTarget.dataset.id
    wx.showLoading({ title: '加载中' })
    request({ url: `/results/${id}` })
      .then(result => {
        getApp().globalData.latestResult = result
        wx.setStorageSync('latest_result', result)
        wx.navigateTo({ url: '/pages/result/index' })
      })
      .catch(error => wx.showToast({ title: error.message || '加载失败', icon: 'none' }))
      .finally(() => wx.hideLoading())
  }
})
