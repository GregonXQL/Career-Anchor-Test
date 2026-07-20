const request = require('../../utils/request')
const { getAnchorProfiles } = require('../../utils/profiles')
const { buildHistoryView } = require('../../utils/report')

const PAGE_SIZE = 10

Page({
  data: {
    loading: true,
    loadingMore: false,
    records: [],
    total: 0,
    page: 0,
    hasMore: true,
    error: ''
  },

  onLoad() {
    this.bootstrap()
  },

  bootstrap() {
    getAnchorProfiles(false)
      .then(profiles => {
        this.profiles = profiles
        return this.loadPage(1)
      })
      .catch(error => this.setData({ loading: false, error: error.message || '加载失败' }))
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loadingMore) this.loadPage(this.data.page + 1)
  },

  loadPage(page) {
    if (page > 1) this.setData({ loadingMore: true })
    return request({ url: `/results?page=${page}&size=${PAGE_SIZE}` })
      .then(data => {
        const next = buildHistoryView(data.records, this.profiles)
        const records = page === 1 ? next : this.data.records.concat(next)
        this.setData({
          loading: false,
          loadingMore: false,
          records,
          total: data.total,
          page,
          hasMore: records.length < data.total,
          error: ''
        })
      })
      .catch(error => {
        this.setData({ loading: false, loadingMore: false, error: error.message || '加载失败' })
        if (page > 1) wx.showToast({ title: error.message || '加载失败', icon: 'none' })
      })
  },

  retry() {
    this.setData({ loading: true, error: '' })
    this.bootstrap()
  },

  open(event) {
    const id = event.currentTarget.dataset.id
    wx.navigateTo({ url: `/pages/report/index?resultId=${id}` })
  },

  start() {
    wx.navigateTo({ url: '/pages/invite/index' })
  }
})
