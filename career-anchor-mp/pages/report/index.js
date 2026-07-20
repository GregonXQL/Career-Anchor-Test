const request = require('../../utils/request')
const { getAnchorProfiles } = require('../../utils/profiles')
const { buildReportView, radarVertices } = require('../../utils/report')

Page({
  data: {
    loading: true,
    error: '',
    report: null,
    displayScores: [],
    topProfiles: [],
    adminMode: false
  },

  onLoad(options) {
    const resultId = Number(options.resultId || 0)
    this.adminMode = options.admin === '1'
    this.setData({ adminMode: this.adminMode })
    this.resultId = resultId
    const latest = getApp().globalData.latestResult || wx.getStorageSync('latest_result')
    const resultPromise = resultId
      ? request({
        url: this.adminMode ? `/admin/results/${resultId}` : `/results/${resultId}`,
        admin: this.adminMode
      })
      : latest ? Promise.resolve(latest) : Promise.reject(new Error('未找到测评结果'))

    Promise.all([resultPromise, getAnchorProfiles(false)])
      .then(([result, profiles]) => {
        const report = buildReportView(result, profiles)
        this.radarValues = report.radar
        this.setData({
          loading: false,
          report,
          displayScores: report.displayScores,
          topProfiles: report.topProfiles
        })
        this.drawRadarWhenReady()
      })
      .catch(error => this.setData({ loading: false, error: error.message || '报告加载失败' }))
  },

  onReady() {
    this.pageReady = true
    this.drawRadarWhenReady()
  },

  onUnload() {
    this.destroyed = true
  },

  retry() {
    const admin = this.adminMode ? '&admin=1' : ''
    wx.redirectTo({ url: `/pages/report/index?resultId=${this.resultId || ''}${admin}` })
  },

  toggleProfile(event) {
    const index = Number(event.currentTarget.dataset.index)
    const key = `topProfiles[${index}].expanded`
    this.setData({ [key]: !this.data.topProfiles[index].expanded })
  },

  saveLongImage() {
    wx.showToast({ title: '保存长图将在二期开放', icon: 'none' })
  },

  home() {
    if (this.adminMode) {
      wx.navigateBack({
        delta: 1,
        fail: () => wx.redirectTo({ url: '/pages/admin/dashboard/index' })
      })
      return
    }
    wx.reLaunch({ url: '/pages/home/index' })
  },

  drawRadarWhenReady() {
    if (!this.pageReady || !this.radarValues || this.destroyed) return
    wx.nextTick(() => this.drawRadar())
  },

  drawRadar() {
    const query = wx.createSelectorQuery().in(this)
    query.select('#radarCanvas').fields({ node: true, size: true }).exec(result => {
      const target = result && result[0]
      if (!target || !target.node || !target.width || this.destroyed) return
      const canvas = target.node
      const context = canvas.getContext('2d')
      const system = wx.getWindowInfo ? wx.getWindowInfo() : wx.getSystemInfoSync()
      const pixelRatio = system.pixelRatio || 1
      canvas.width = target.width * pixelRatio
      canvas.height = target.height * pixelRatio
      context.scale(pixelRatio, pixelRatio)
      this.paintRadar(context, target.width, target.height)
    })
  },

  paintRadar(context, width, height) {
    const centerX = width / 2
    const centerY = height / 2 + 4
    const radius = Math.min(width, height) * 0.31
    const dimension = this.radarValues.length
    const pointAt = (index, ringRadius) => {
      const angle = -Math.PI / 2 + index * Math.PI * 2 / dimension
      return {
        x: centerX + Math.cos(angle) * ringRadius,
        y: centerY + Math.sin(angle) * ringRadius
      }
    }
    const polygon = (points, strokeStyle, fillStyle, lineWidth) => {
      context.beginPath()
      points.forEach((point, index) => index ? context.lineTo(point.x, point.y) : context.moveTo(point.x, point.y))
      context.closePath()
      if (fillStyle) {
        context.fillStyle = fillStyle
        context.fill()
      }
      context.strokeStyle = strokeStyle
      context.lineWidth = lineWidth
      context.stroke()
    }

    context.clearRect(0, 0, width, height)
    for (let ring = 1; ring <= 4; ring += 1) {
      polygon(Array.from({ length: dimension }, (_, index) => pointAt(index, radius * ring / 4)),
        ring === 4 ? '#C0B6AD' : '#E5DED7', null, 1)
    }
    for (let index = 0; index < dimension; index += 1) {
      const point = pointAt(index, radius)
      context.beginPath()
      context.moveTo(centerX, centerY)
      context.lineTo(point.x, point.y)
      context.strokeStyle = '#E5DED7'
      context.lineWidth = 1
      context.stroke()
    }

    const values = this.radarValues.map(item => item.value)
    polygon(radarVertices(values, centerX, centerY, radius), '#8A6A4F', 'rgba(168,128,102,0.28)', 2)

    context.fillStyle = '#6F6259'
    context.font = '12px -apple-system, BlinkMacSystemFont, sans-serif'
    this.radarValues.forEach((item, index) => {
      const angle = -Math.PI / 2 + index * Math.PI * 2 / dimension
      const label = pointAt(index, radius + 22)
      const cosine = Math.cos(angle)
      context.textAlign = cosine > 0.3 ? 'left' : cosine < -0.3 ? 'right' : 'center'
      context.textBaseline = Math.sin(angle) > 0.3 ? 'top' : Math.sin(angle) < -0.3 ? 'bottom' : 'middle'
      context.fillText(`${item.name} ${item.value}`, label.x, label.y)
    })
  }
})
