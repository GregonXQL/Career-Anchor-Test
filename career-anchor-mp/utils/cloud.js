const { cloudEnv, serviceName, apiPrefix } = require('./config')

function callContainer(options) {
  if (!wx.cloud || !wx.cloud.callContainer) {
    return Promise.reject(new Error('当前微信基础库不支持云托管调用'))
  }

  return wx.cloud.callContainer({
    config: {
      env: cloudEnv
    },
    path: `${apiPrefix}${options.url}`,
    method: options.method || 'GET',
    data: options.data,
    header: Object.assign({
      'X-WX-SERVICE': serviceName,
      'content-type': 'application/json'
    }, options.header || {})
  })
}

module.exports = callContainer
