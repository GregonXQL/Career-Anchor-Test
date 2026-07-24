const request = require('../../utils/request')
const { isProfileComplete } = require('../../utils/user-profile')
const { parseInviteOptions } = require('../../utils/invite')

Page({
  data: {
    resultCount: 0,
    countLoading: true,
    profileLoading: true,
    profile: null,
    pendingInviteCode: ''
  },

  onLoad(options) {
    this.setData({ pendingInviteCode: parseInviteOptions(options) })
  },

  onShow() {
    this.setData({ countLoading: true })
    request({ url: '/results?page=1&size=1' })
      .then(data => this.setData({ resultCount: data.total, countLoading: false }))
      .catch(() => this.setData({ countLoading: false }))
    this.loadProfile()
  },

  loadProfile() {
    this.setData({ profileLoading: true })
    request({ url: '/users/me' })
      .then(profile => this.setData({ profile, profileLoading: false }))
      .catch(() => this.setData({ profileLoading: false }))
  },

  start() {
    if (this.data.profileLoading) {
      wx.showToast({ title: '正在读取用户信息', icon: 'none' })
      return
    }
    const target = this.data.pendingInviteCode
      ? `/pages/quiz/index?inviteCode=${this.data.pendingInviteCode}`
      : '/pages/invite/index'
    if (!isProfileComplete(this.data.profile)) {
      wx.navigateTo({ url: `/pages/profile/index?redirect=${encodeURIComponent(target)}` })
      return
    }
    wx.navigateTo({ url: target })
  },

  editProfile() {
    wx.navigateTo({ url: `/pages/profile/index?redirect=${encodeURIComponent('/pages/home/index')}` })
  },

  history() {
    wx.navigateTo({ url: '/pages/history/index' })
  },

  admin() {
    wx.navigateTo({ url: '/pages/admin/login/index' })
  }
})
