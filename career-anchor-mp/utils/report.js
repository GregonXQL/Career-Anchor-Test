const ANCHOR_ORDER = [
  'TECHNICAL', 'MANAGERIAL', 'AUTONOMY', 'SECURITY',
  'CREATIVITY', 'SERVICE', 'CHALLENGE', 'LIFESTYLE'
]

const SHORT_NAMES = {
  TECHNICAL: '技术/职能',
  MANAGERIAL: '管理',
  AUTONOMY: '自主/独立',
  SECURITY: '安全/稳定',
  CREATIVITY: '创造/创业',
  SERVICE: '服务',
  CHALLENGE: '挑战',
  LIFESTYLE: '生活'
}

const TONES = {
  TECHNICAL: 'bronze',
  MANAGERIAL: 'champagne',
  AUTONOMY: 'clay',
  SECURITY: 'taupe',
  CREATIVITY: 'copper',
  SERVICE: 'sand',
  CHALLENGE: 'deep',
  LIFESTYLE: 'oat'
}

const SECTION_DEFINITIONS = [
  ['traits', '特点描述'],
  ['strengths', '职场优势'],
  ['risks', '潜在挑战'],
  ['advices', '发展建议'],
  ['careers', '适合职业']
]

function profileMap(profiles) {
  return (profiles || []).reduce((map, profile) => {
    map[profile.anchor] = profile
    return map
  }, {})
}

function assertCompleteProfiles(map) {
  ANCHOR_ORDER.forEach(anchor => {
    if (!map[anchor]) throw new Error(`缺少职业锚解析：${anchor}`)
  })
}

function buildReportView(result, profiles) {
  if (!result || !Array.isArray(result.scores) || !Array.isArray(result.top3)) {
    throw new Error('报告数据不完整')
  }
  const map = profileMap(profiles)
  assertCompleteProfiles(map)
  const byAnchor = result.scores.reduce((scores, score) => {
    scores[score.anchor] = score
    return scores
  }, {})
  ANCHOR_ORDER.forEach(anchor => {
    if (!byAnchor[anchor]) throw new Error(`缺少维度分数：${anchor}`)
  })

  const topSet = new Set(result.top3)
  const displayScores = result.scores
    .slice()
    .sort((left, right) => right.raw - left.raw || ANCHOR_ORDER.indexOf(left.anchor) - ANCHOR_ORDER.indexOf(right.anchor))
    .map((score, index) => ({
      ...score,
      shortName: SHORT_NAMES[score.anchor],
      rank: index + 1,
      top: topSet.has(score.anchor),
      barClass: index === 0 ? 'bar-first' : index < 3 ? 'bar-top' : 'bar-normal',
      barWidth: Math.max(4, Math.min(100, score.percent))
    }))

  const topProfiles = result.top3.map((anchor, index) => {
    const profile = map[anchor]
    return {
      ...profile,
      rank: index + 1,
      tone: TONES[anchor],
      expanded: index === 0,
      sections: SECTION_DEFINITIONS.map(([key, title]) => ({ title, items: profile[key] || [] }))
    }
  })

  return {
    id: result.id,
    resultNo: `CA-${String(result.id).padStart(6, '0')}`,
    createdAt: result.createdAt,
    scaleMax: result.scaleMax,
    top1: topProfiles[0],
    displayScores,
    topProfiles,
    radar: ANCHOR_ORDER.map(anchor => ({
      anchor,
      name: SHORT_NAMES[anchor],
      value: byAnchor[anchor].percent
    }))
  }
}

function buildHistoryView(records, profiles) {
  const map = profileMap(profiles)
  return (records || []).map(record => ({
    ...record,
    resultNo: `CA-${String(record.id).padStart(6, '0')}`,
    top1Name: map[record.top1] ? map[record.top1].nameCn : SHORT_NAMES[record.top1],
    top3Text: [record.top1, record.top2, record.top3]
      .map(anchor => map[anchor] ? map[anchor].nameCn.replace('型', '') : SHORT_NAMES[anchor])
      .join(' · '),
    tone: TONES[record.top1] || 'bronze',
    date: String(record.createdAt || '').slice(0, 10),
    time: String(record.createdAt || '').slice(11, 16)
  }))
}

function radarVertices(values, centerX, centerY, radius) {
  if (!Array.isArray(values) || values.length !== ANCHOR_ORDER.length) {
    throw new Error('雷达图必须包含 8 个维度')
  }
  return values.map((value, index) => {
    const angle = -Math.PI / 2 + index * Math.PI * 2 / values.length
    const safeValue = Math.max(0, Math.min(100, Number(value) || 0))
    return {
      x: centerX + Math.cos(angle) * radius * safeValue / 100,
      y: centerY + Math.sin(angle) * radius * safeValue / 100
    }
  })
}

module.exports = {
  ANCHOR_ORDER,
  SHORT_NAMES,
  TONES,
  buildReportView,
  buildHistoryView,
  radarVertices
}
