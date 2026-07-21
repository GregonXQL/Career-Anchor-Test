const test = require('node:test')
const assert = require('node:assert/strict')
const fs = require('node:fs')
const path = require('node:path')

const root = path.resolve(__dirname, '..')
const read = relative => fs.readFileSync(path.join(root, relative), 'utf8')

test('M5 enables native privacy checking and guards direct scan entry', () => {
  const app = JSON.parse(read('app.json'))
  assert.equal(app.__usePrivacyCheck__, true)
  assert.equal(app.lazyCodeLoading, 'requiredComponents')
  assert.match(read('pages/launch/index.js'), /requirePrivacyAuthorize|privacy\.authorize/)
  assert.match(read('pages/quiz/index.js'), /privacy\.hasAccepted/)
  assert.match(read('pages/quiz/index.js'), /redirect=quiz/)
})

test('M5 keeps the disclaimer on both home and report pages', () => {
  const expected = '不构成任何诊断或决策依据'
  assert.match(read('pages/home/index.wxml'), new RegExp(expected))
  assert.match(read('pages/report/index.wxml'), new RegExp(expected))
  assert.match(read('pages/report/index.wxml'), /职业价值雷达/)
  assert.doesNotMatch(read('pages/report/index.wxml'), /能力倾向雷达/)
})

test('M5 key async pages expose skeleton and empty states', () => {
  for (const page of ['quiz', 'report', 'history']) {
    assert.match(read(`pages/${page}/index.wxml`), /skeleton/)
  }
  assert.match(read('pages/history/index.wxml'), /还没有测评记录/)
  const admin = read('pages/admin/dashboard/index.wxml')
  assert.match(admin, /admin-skeleton-list/)
  assert.match(admin, /暂无符合条件的测评结果/)
  assert.match(admin, /还没有邀请码/)
  assert.match(admin, /retryCurrent/)
})
