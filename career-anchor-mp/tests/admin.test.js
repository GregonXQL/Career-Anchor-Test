const test = require('node:test')
const assert = require('node:assert/strict')

const { parseInviteOptions } = require('../utils/invite')
const { resultViews, inviteViews, resultQuery } = require('../utils/admin-view')

test('scan scene parses exactly one valid eight-character invite code', () => {
  assert.equal(parseInviteOptions({ scene: encodeURIComponent('i=M3TEST88') }), 'M3TEST88')
  assert.equal(parseInviteOptions({ scene: encodeURIComponent('x=1&i=ABCDEFGH') }), 'ABCDEFGH')
  assert.equal(parseInviteOptions({ scene: encodeURIComponent('i=M3LOCAL88') }), '')
  assert.equal(parseInviteOptions({ scene: '%' }), '')
})

test('manual invite option is normalized for the shared quiz flow', () => {
  assert.equal(parseInviteOptions({ inviteCode: ' m3test88 ' }), 'M3TEST88')
})

test('admin result and invite views expose localized management labels', () => {
  const results = resultViews([{ id: 1, nickname: '微信用户', openidSuffix: 'ABC123', top1: 'CHALLENGE' }])
  assert.equal(results[0].top1Name, '挑战型')
  assert.equal(results[0].userLabel, '微信用户 · ABC123')

  const invites = inviteViews([{ status: 'ACTIVE', channel: 'QR', usedCount: 1, maxUses: 5 }])
  assert.equal(invites[0].statusLabel, '有效')
  assert.equal(invites[0].channelLabel, '二维码')
  assert.equal(invites[0].usageText, '1/5')
  assert.equal(invites[0].canOperate, true)
})

test('admin result query encodes keyword and optional date boundaries', () => {
  assert.equal(
    resultQuery({ keyword: ' 张 三 ', from: '2026-07-01', to: '2026-07-20' }, 2, 10),
    'page=2&size=10&keyword=%E5%BC%A0%20%E4%B8%89&from=2026-07-01&to=2026-07-20'
  )
})
