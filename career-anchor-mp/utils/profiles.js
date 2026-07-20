const request = require('./request')

const CACHE_KEY = 'anchor_profiles_cache'
const CACHE_TTL = 24 * 60 * 60 * 1000

function validCache(cache, now) {
  return cache && Array.isArray(cache.data) && cache.data.length === 8 &&
    Number(cache.savedAt) > 0 && now - cache.savedAt < CACHE_TTL
}

function getAnchorProfiles(forceRefresh) {
  const now = Date.now()
  const cached = wx.getStorageSync(CACHE_KEY)
  if (!forceRefresh && validCache(cached, now)) return Promise.resolve(cached.data)

  return request({ url: '/anchor-profiles' }).then(data => {
    if (!Array.isArray(data) || data.length !== 8) throw new Error('职业锚解析文案不完整')
    wx.setStorageSync(CACHE_KEY, { savedAt: now, data })
    return data
  })
}

module.exports = { CACHE_KEY, CACHE_TTL, validCache, getAnchorProfiles }
