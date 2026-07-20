const ANCHOR_NAMES = {
  TECHNICAL: '技术/职能型',
  MANAGERIAL: '管理型',
  AUTONOMY: '自主/独立型',
  SECURITY: '安全/稳定型',
  CREATIVITY: '创造/创业型',
  SERVICE: '服务型',
  CHALLENGE: '挑战型',
  LIFESTYLE: '生活型'
}

const STATUS_LABELS = {
  ACTIVE: '有效',
  USED: '已用尽',
  EXPIRED: '已过期',
  DISABLED: '已停用'
}

function resultViews(records) {
  return (records || []).map(record => ({
    ...record,
    top1Name: ANCHOR_NAMES[record.top1] || record.top1,
    userLabel: record.nickname === '微信用户'
      ? `微信用户 · ${record.openidSuffix || '------'}`
      : `${record.nickname} · ${record.openidSuffix || '------'}`
  }))
}

function inviteViews(records) {
  return (records || []).map(invite => ({
    ...invite,
    statusLabel: STATUS_LABELS[invite.status] || invite.status,
    channelLabel: invite.channel === 'QR' ? '二维码' : '手动码',
    canOperate: invite.status === 'ACTIVE',
    usageText: `${invite.usedCount}/${invite.maxUses}`
  }))
}

function resultQuery(filters, page, size) {
  const params = [`page=${page}`, `size=${size}`]
  if (filters.keyword) params.push(`keyword=${encodeURIComponent(filters.keyword.trim())}`)
  if (filters.from) params.push(`from=${filters.from}`)
  if (filters.to) params.push(`to=${filters.to}`)
  return params.join('&')
}

module.exports = { ANCHOR_NAMES, STATUS_LABELS, resultViews, inviteViews, resultQuery }
