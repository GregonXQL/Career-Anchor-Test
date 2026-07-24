const test = require('node:test')
const assert = require('node:assert/strict')
const fs = require('node:fs')
const path = require('node:path')
const { normalizeNickname, hasDistinguishingNickname, isProfileComplete } = require('../utils/user-profile')

const root = path.resolve(__dirname, '..')

test('nickname normalization accepts a chosen nickname but rejects the generic placeholder', () => {
  assert.equal(normalizeNickname('  小林  '), '小林')
  assert.equal(hasDistinguishingNickname('小林'), true)
  assert.equal(hasDistinguishingNickname('微信用户'), false)
  assert.equal(hasDistinguishingNickname('   '), false)
  assert.equal(isProfileComplete({ nickname: '小林', avatarUrl: 'data:image/jpeg;base64,abc' }), true)
  assert.equal(isProfileComplete({ nickname: '小林', avatarUrl: '' }), false)
})

test('profile page offers WeChat and local profile choices and is registered before home', () => {
  const app = JSON.parse(fs.readFileSync(path.join(root, 'app.json'), 'utf8'))
  const markup = fs.readFileSync(path.join(root, 'pages/profile/index.wxml'), 'utf8')

  assert.equal(app.pages.includes('pages/profile/index'), true)
  assert.match(markup, /open-type="chooseAvatar"/)
  assert.match(markup, /type="nickname"/)
  assert.match(markup, /自己上传头像/)
})
