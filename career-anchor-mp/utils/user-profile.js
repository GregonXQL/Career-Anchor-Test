function normalizeNickname(value) {
  return String(value || '').trim()
}

function hasDistinguishingNickname(value) {
  const nickname = normalizeNickname(value)
  return Boolean(nickname && nickname !== '微信用户')
}

function isProfileComplete(profile) {
  return Boolean(profile
    && hasDistinguishingNickname(profile.nickname)
    && String(profile.avatarUrl || '').startsWith('data:image/'))
}

module.exports = {
  normalizeNickname,
  hasDistinguishingNickname,
  isProfileComplete
}
