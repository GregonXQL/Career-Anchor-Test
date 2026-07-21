const test = require('node:test')
const assert = require('node:assert/strict')
const { PRIVACY_ACCEPTED_KEY, hasAccepted, authorize, openContract } = require('../utils/privacy')

function storageApi(initial) {
  const values = Object.assign({}, initial)
  return {
    getStorageSync: key => values[key],
    setStorageSync: (key, value) => { values[key] = value },
    values
  }
}

test('privacy acceptance is remembered only after native authorization succeeds', async () => {
  const api = Object.assign(storageApi(), {
    requirePrivacyAuthorize(options) { options.success() }
  })
  assert.equal(hasAccepted(api), false)
  await authorize(api)
  assert.equal(api.values[PRIVACY_ACCEPTED_KEY], true)
  assert.equal(hasAccepted(api), true)
})

test('privacy rejection keeps acceptance unset', async () => {
  const api = Object.assign(storageApi(), {
    requirePrivacyAuthorize(options) { options.fail({ errMsg: 'deny' }) }
  })
  await assert.rejects(authorize(api), /需要同意隐私保护指引/)
  assert.equal(hasAccepted(api), false)
})

test('older clients can accept the local notice and privacy contract errors clearly', async () => {
  const api = storageApi()
  await authorize(api)
  assert.equal(hasAccepted(api), true)
  await assert.rejects(openContract(api), /暂不支持/)
})
