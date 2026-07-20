const { baseUrl } = require('./config')
const auth = require('./auth')

function request(options) {
  return new Promise((resolve, reject) => {
    const header = Object.assign({}, options.header || {})
    const token = options.admin ? auth.getAdminToken() : auth.getToken()
    if (token) {
      header.Authorization = `Bearer ${token}`
    }
    wx.request({
      url: `${baseUrl}${options.url}`,
      method: options.method || 'GET',
      data: options.data,
      header,
      success(response) {
        const body = response.data || {}
        if (response.statusCode >= 200 && response.statusCode < 300 && body.code === 0) {
          resolve(body.data)
          return
        }
        if (response.statusCode === 401 || body.code === 40101) {
          if (options.admin) {
            auth.clearAdminToken()
            wx.redirectTo({ url: '/pages/admin/login/index' })
          } else {
            auth.clearToken()
            wx.reLaunch({ url: '/pages/launch/index' })
          }
        }
        const error = new Error(body.msg || `请求失败（${response.statusCode}）`)
        error.code = body.code
        error.statusCode = response.statusCode
        reject(error)
      },
      fail: reject
    })
  })
}

module.exports = request
