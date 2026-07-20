const INVITE_PATTERN = /(?:^|&)i=([23456789ABCDEFGHJKMNPQRSTUVWXYZ]{8})(?:&|$)/

function parseInviteOptions(options) {
  if (options && options.inviteCode) return String(options.inviteCode).trim().toUpperCase()
  if (!options || !options.scene) return ''
  try {
    const scene = decodeURIComponent(options.scene)
    const match = scene.match(INVITE_PATTERN)
    return match ? match[1] : ''
  } catch (error) {
    return ''
  }
}

module.exports = { INVITE_PATTERN, parseInviteOptions }
