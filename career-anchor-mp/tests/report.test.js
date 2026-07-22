const test = require('node:test')
const assert = require('node:assert/strict')
const { ANCHOR_ORDER, buildReportView, buildHistoryView, radarVertices } = require('../utils/report')
const { posterSize, paintReportPoster, fitText } = require('../utils/report-poster')
const { CACHE_TTL, validCache } = require('../utils/profiles')

const profiles = ANCHOR_ORDER.map(anchor => ({
  anchor,
  nameCn: `${anchor}型`,
  nameEn: anchor,
  tagline: `${anchor} tagline`,
  traits: [{ title: '特点', desc: '说明' }],
  strengths: [{ title: '优势', desc: '说明' }],
  risks: [{ title: '挑战', desc: '说明' }],
  advices: [{ title: '建议', desc: '说明' }],
  careers: [{ title: '职业', desc: '说明' }]
}))

test('buildReportView sorts raw scores and keeps radar in fixed anchor order', () => {
  const scores = ANCHOR_ORDER.map((anchor, index) => ({
    anchor,
    nameCn: `${anchor}型`,
    raw: 10 + index,
    avg: 2 + index / 5,
    percent: 30 + index
  }))
  const view = buildReportView({
    id: 23,
    createdAt: '2026-07-20 10:00:00',
    scaleMax: 6,
    scores,
    top3: ['LIFESTYLE', 'CHALLENGE', 'SERVICE']
  }, profiles)

  assert.equal(view.resultNo, 'CA-000023')
  assert.deepEqual(view.displayScores.slice(0, 3).map(item => item.anchor),
    ['LIFESTYLE', 'CHALLENGE', 'SERVICE'])
  assert.deepEqual(view.radar.map(item => item.anchor), ANCHOR_ORDER)
  assert.equal(view.topProfiles[0].expanded, true)
  assert.deepEqual(view.topProfiles[0].sections.map(section => section.title),
    ['特点描述', '职场优势', '潜在挑战', '发展建议', '适合职业'])
})

test('buildReportView rejects incomplete profiles or scores', () => {
  const result = { id: 1, scores: [], top3: ['TECHNICAL'] }
  assert.throws(() => buildReportView(result, profiles.slice(1)), /缺少职业锚解析/)
  assert.throws(() => buildReportView(result, profiles), /缺少维度分数/)
})

test('buildHistoryView renders Chinese summaries and stable tones', () => {
  const records = [{
    id: 1,
    createdAt: '2026-07-20 10:08:00',
    top1: 'TECHNICAL',
    top2: 'SERVICE',
    top3: 'LIFESTYLE'
  }]
  const item = buildHistoryView(records, profiles)[0]
  assert.equal(item.date, '2026-07-20')
  assert.equal(item.time, '10:08')
  assert.equal(item.resultNo, 'CA-000001')
  assert.equal(item.top3Text, 'TECHNICAL · SERVICE · LIFESTYLE')
  assert.equal(item.tone, 'bronze')
})

test('radarVertices clamps values and returns eight finite points', () => {
  const points = radarVertices([-1, 20, 40, 60, 80, 100, 120, null], 100, 100, 80)
  assert.equal(points.length, 8)
  points.forEach(point => {
    assert.equal(Number.isFinite(point.x), true)
    assert.equal(Number.isFinite(point.y), true)
    assert.equal(point.x >= 20 && point.x <= 180, true)
    assert.equal(point.y >= 20 && point.y <= 180, true)
  })
})

test('profile cache is valid for exactly eight profiles before 24 hours', () => {
  const now = 2 * CACHE_TTL
  assert.equal(validCache({ savedAt: now - CACHE_TTL + 1, data: profiles }, now), true)
  assert.equal(validCache({ savedAt: now - CACHE_TTL, data: profiles }, now), false)
  assert.equal(validCache({ savedAt: now - 100, data: profiles.slice(1) }, now), false)
})

test('report poster renders a complete shareable summary at a safe long-image size', () => {
  const scores = ANCHOR_ORDER.map((anchor, index) => ({
    anchor,
    nameCn: `${anchor}型`,
    raw: 20 - index,
    avg: 3,
    percent: 80 - index * 5
  }))
  const report = buildReportView({
    id: 23,
    createdAt: '2026-07-20 10:00:00',
    scaleMax: 6,
    scores,
    top3: ANCHOR_ORDER.slice(0, 3)
  }, profiles)
  const drawnText = []
  const context = {
    save() {}, restore() {}, scale() {}, fillRect() {}, beginPath() {}, moveTo() {}, lineTo() {},
    quadraticCurveTo() {}, closePath() {}, fill() {}, stroke() {},
    createLinearGradient() { return { addColorStop() {} } },
    measureText(text) { return { width: String(text).length * 8 } },
    fillText(text) { drawnText.push(String(text)) }
  }
  const size = posterSize(375)

  paintReportPoster(context, report, size.width, size.height)

  assert.deepEqual(size, { width: 375, height: 1600 })
  assert.equal(drawnText.includes('职业锚测评报告 · 首要职业锚'), true)
  assert.equal(drawnText.some(text => text.includes('CA-000023')), true)
  assert.equal(drawnText.includes('我的 Top 3 职业锚'), true)
  assert.equal(fitText(context, '这是一段很长的文字', 24).endsWith('…'), true)
})
