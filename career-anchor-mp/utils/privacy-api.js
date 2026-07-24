const UNDECLARED_SCOPE = 'api scope is not declared in privacy agreement'

function privacyApiError(error, capability) {
  const raw = String((error && (error.errMsg || error.message)) || '')
  if (raw.includes(UNDECLARED_SCOPE)) {
    return {
      undeclared: true,
      title: '隐私权限未声明',
      message: `请先在微信公众平台“用户隐私保护指引”中声明${capability}用途，然后重新发布小程序。`
    }
  }
  return {
    undeclared: false,
    title: '操作失败',
    message: raw || '操作失败，请稍后重试'
  }
}

function showPrivacyApiError(api, error, capability) {
  const detail = privacyApiError(error, capability)
  if (detail.undeclared) {
    api.showModal({
      title: detail.title,
      content: detail.message,
      showCancel: false
    })
    return
  }
  api.showToast({ title: detail.message, icon: 'none' })
}

module.exports = {
  privacyApiError,
  showPrivacyApiError
}
