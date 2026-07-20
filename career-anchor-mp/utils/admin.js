const auth = require('./auth')
const request = require('./request')

function login(password) {
  return auth.ensureLogin()
    .then(() => request({
      url: '/auth/admin-login',
      method: 'POST',
      data: { password }
    }))
    .then(data => {
      auth.setAdminToken(data.adminToken)
      return data
    })
}

function requestAdmin(options) {
  return request(Object.assign({}, options, { admin: true }))
}

module.exports = { login, requestAdmin }
