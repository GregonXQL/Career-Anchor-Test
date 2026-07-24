const request = require('../../utils/request')
const { normalizeNickname, isProfileComplete } = require('../../utils/user-profile')
const { showPrivacyApiError } = require('../../utils/privacy-api')

const DEFAULT_REDIRECT = '/pages/home/index'
const MAX_AVATAR_BASE64_LENGTH = 350000

function safeRedirect(value) {
  let decoded = ''
  try {
    decoded = decodeURIComponent(String(value || ''))
  } catch (error) {
    return DEFAULT_REDIRECT
  }
  return /^\/pages\/(home|invite|quiz)\/index(?:\?.*)?$/.test(decoded) ? decoded : DEFAULT_REDIRECT
}

Page({
  data: {
    loading: true,
    saving: false,
    nickname: '',
    avatarUrl: ''
  },

  onLoad(options) {
    this.redirectUrl = safeRedirect(options.redirect)
    request({ url: '/users/me' })
      .then(profile => this.setData({
        loading: false,
        nickname: profile.nickname || '',
        avatarUrl: profile.avatarUrl || ''
      }))
      .catch(error => {
        this.setData({ loading: false })
        wx.showToast({ title: error.message || '资料加载失败', icon: 'none' })
      })
  },

  nicknameInput(event) {
    this.setData({ nickname: event.detail.value })
  },

  chooseWechatAvatar(event) {
    const path = event.detail && event.detail.avatarUrl
    if (path) this.prepareAvatar(path)
  },

  chooseLocalAvatar() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      sizeType: ['compressed'],
      success: result => {
        const file = result.tempFiles && result.tempFiles[0]
        if (file && file.tempFilePath) this.prepareAvatar(file.tempFilePath)
      },
      fail: error => {
        if (!String(error.errMsg || '').includes('cancel')) {
          showPrivacyApiError(wx, error, '选择照片或拍摄')
        }
      }
    })
  },

  prepareAvatar(sourcePath) {
    wx.showLoading({ title: '正在处理头像', mask: true })
    this.compressAvatar(sourcePath)
      .then(path => this.readAvatar(path))
      .then(avatarUrl => this.setData({ avatarUrl }))
      .catch(error => wx.showToast({ title: error.message || '头像处理失败', icon: 'none' }))
      .finally(() => wx.hideLoading())
  },

  compressAvatar(sourcePath) {
    return new Promise(resolve => wx.compressImage({
      src: sourcePath,
      quality: 68,
      compressedWidth: 256,
      compressedHeight: 256,
      success: result => resolve(result.tempFilePath),
      fail: () => resolve(sourcePath)
    }))
  },

  readAvatar(filePath) {
    return new Promise((resolve, reject) => wx.getFileSystemManager().readFile({
      filePath,
      encoding: 'base64',
      success: result => {
        const base64 = String(result.data || '')
        if (!base64 || base64.length > MAX_AVATAR_BASE64_LENGTH) {
          reject(new Error('头像过大，请选择较小的图片'))
          return
        }
        const mime = /\.png(?:$|\?)/i.test(filePath) ? 'image/png' : 'image/jpeg'
        resolve(`data:${mime};base64,${base64}`)
      },
      fail: () => reject(new Error('头像读取失败'))
    }))
  },

  save() {
    if (this.data.saving) return
    const profile = {
      nickname: normalizeNickname(this.data.nickname),
      avatarUrl: this.data.avatarUrl
    }
    if (!isProfileComplete(profile)) {
      wx.showToast({ title: '请填写昵称并选择头像', icon: 'none' })
      return
    }
    this.setData({ saving: true })
    request({
      url: '/users/me',
      method: 'PUT',
      data: profile
    }).then(() => wx.reLaunch({ url: this.redirectUrl }))
      .catch(error => wx.showToast({ title: error.message || '资料保存失败', icon: 'none' }))
      .finally(() => this.setData({ saving: false }))
  }
})
