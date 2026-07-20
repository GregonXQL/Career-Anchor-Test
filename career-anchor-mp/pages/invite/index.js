const request = require('../../utils/request')

function slotsFor(code) {
  return Array.from({ length: 8 }, (_, index) => code[index] || '')
}

Page({
  data: { code: '', slots: slotsFor(''), loading: false, shaking: false },

  input(event) {
    const code = (event.detail.value || '').toUpperCase().replace(/[^A-Z0-9]/g, '').slice(0, 8)
    this.setData({ code, slots: slotsFor(code) })
  },

  verify() {
    if (this.data.code.length !== 8 || this.data.loading) {
      this.fail('请输入 8 位邀请码')
      return
    }
    this.setData({ loading: true })
    request({ url: '/invite/verify', method: 'POST', data: { code: this.data.code } })
      .then(() => wx.navigateTo({ url: `/pages/quiz/index?inviteCode=${this.data.code}` }))
      .catch(error => this.fail(error.message || '邀请码无效'))
      .finally(() => this.setData({ loading: false }))
  },

  fail(message) {
    this.setData({ shaking: true })
    wx.showToast({ title: message, icon: 'none' })
    setTimeout(() => this.setData({ shaking: false }), 450)
  }
})
