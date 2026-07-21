const PRIVACY_ACCEPTED_KEY = 'privacy_notice_accepted_v1'

function hasAccepted(api = wx) {
  return api.getStorageSync(PRIVACY_ACCEPTED_KEY) === true
}

function rememberAccepted(api = wx) {
  api.setStorageSync(PRIVACY_ACCEPTED_KEY, true)
}

function authorize(api = wx) {
  return new Promise((resolve, reject) => {
    if (typeof api.requirePrivacyAuthorize !== 'function') {
      rememberAccepted(api)
      resolve()
      return
    }
    api.requirePrivacyAuthorize({
      success() {
        rememberAccepted(api)
        resolve()
      },
      fail() {
        reject(new Error('需要同意隐私保护指引后才能继续使用'))
      }
    })
  })
}

function openContract(api = wx) {
  return new Promise((resolve, reject) => {
    if (typeof api.openPrivacyContract !== 'function') {
      reject(new Error('当前微信版本暂不支持查看隐私保护指引'))
      return
    }
    api.openPrivacyContract({ success: resolve, fail: reject })
  })
}

module.exports = {
  PRIVACY_ACCEPTED_KEY,
  hasAccepted,
  authorize,
  openContract
}
