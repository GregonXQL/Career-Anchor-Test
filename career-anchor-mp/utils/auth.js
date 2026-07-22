const callContainer = require('./cloud')

const TOKEN_KEY = 'auth_token'
const ADMIN_TOKEN_KEY = 'admin_token'

function getToken() {
  return wx.getStorageSync(TOKEN_KEY) || ''
}

function setToken(token) {
  wx.setStorageSync(TOKEN_KEY, token)
}

function clearToken() {
  wx.removeStorageSync(TOKEN_KEY)
  clearAdminToken()
}

function getAdminToken() {
  return wx.getStorageSync(ADMIN_TOKEN_KEY) || ''
}

function setAdminToken(token) {
  wx.setStorageSync(ADMIN_TOKEN_KEY, token)
}

function clearAdminToken() {
  wx.removeStorageSync(ADMIN_TOKEN_KEY)
}

function login() {
  return new Promise((resolve, reject) => {
    wx.login({
      success(loginResult) {
        if (!loginResult.code) {
          reject(new Error('微信登录未返回 code'))
          return
        }
        callContainer({
          url: '/auth/wx-login',
          method: 'POST',
          data: { code: loginResult.code }
        }).then(response => {
          const body = response.data || {}
          if (response.statusCode >= 200 && response.statusCode < 300 && body.code === 0) {
            setToken(body.data.token)
            resolve(body.data)
            return
          }
          reject(new Error(body.msg || '登录失败'))
        })
          .catch(reject)
      },
      fail: reject
    })
  })
}

function ensureLogin() {
  return getToken() ? Promise.resolve(getToken()) : login().then(data => data.token)
}

module.exports = {
  getToken,
  setToken,
  clearToken,
  getAdminToken,
  setAdminToken,
  clearAdminToken,
  login,
  ensureLogin
}
