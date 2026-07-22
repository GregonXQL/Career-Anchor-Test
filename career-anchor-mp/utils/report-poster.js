const { radarVertices } = require('./report')

const DESIGN_WIDTH = 375
const DESIGN_HEIGHT = 1600

function posterSize(width) {
  const safeWidth = Math.max(320, Math.min(430, Number(width) || DESIGN_WIDTH))
  return {
    width: safeWidth,
    height: Math.round(safeWidth * DESIGN_HEIGHT / DESIGN_WIDTH)
  }
}

function roundedRect(context, x, y, width, height, radius) {
  const safeRadius = Math.min(radius, width / 2, height / 2)
  context.beginPath()
  context.moveTo(x + safeRadius, y)
  context.lineTo(x + width - safeRadius, y)
  context.quadraticCurveTo(x + width, y, x + width, y + safeRadius)
  context.lineTo(x + width, y + height - safeRadius)
  context.quadraticCurveTo(x + width, y + height, x + width - safeRadius, y + height)
  context.lineTo(x + safeRadius, y + height)
  context.quadraticCurveTo(x, y + height, x, y + height - safeRadius)
  context.lineTo(x, y + safeRadius)
  context.quadraticCurveTo(x, y, x + safeRadius, y)
  context.closePath()
}

function fillRoundedRect(context, x, y, width, height, radius, color) {
  roundedRect(context, x, y, width, height, radius)
  context.fillStyle = color
  context.fill()
}

function fitText(context, text, maxWidth) {
  const value = String(text || '')
  if (context.measureText(value).width <= maxWidth) return value
  let result = value
  while (result.length && context.measureText(`${result}…`).width > maxWidth) result = result.slice(0, -1)
  return `${result}…`
}

function drawRadar(context, radar, centerX, centerY, radius) {
  const pointAt = (index, ringRadius) => {
    const angle = -Math.PI / 2 + index * Math.PI * 2 / radar.length
    return {
      x: centerX + Math.cos(angle) * ringRadius,
      y: centerY + Math.sin(angle) * ringRadius
    }
  }
  const polygon = (points, strokeStyle, fillStyle, lineWidth) => {
    context.beginPath()
    points.forEach((point, index) => index ? context.lineTo(point.x, point.y) : context.moveTo(point.x, point.y))
    context.closePath()
    if (fillStyle) {
      context.fillStyle = fillStyle
      context.fill()
    }
    context.strokeStyle = strokeStyle
    context.lineWidth = lineWidth
    context.stroke()
  }

  for (let ring = 1; ring <= 4; ring += 1) {
    polygon(radar.map((_, index) => pointAt(index, radius * ring / 4)), '#E5DED7', null, 0.7)
  }
  radar.forEach((_, index) => {
    const point = pointAt(index, radius)
    context.beginPath()
    context.moveTo(centerX, centerY)
    context.lineTo(point.x, point.y)
    context.strokeStyle = '#E5DED7'
    context.lineWidth = 0.7
    context.stroke()
  })
  polygon(radarVertices(radar.map(item => item.value), centerX, centerY, radius),
    '#8A6A4F', 'rgba(168,128,102,0.28)', 1.5)

  context.fillStyle = '#6F6259'
  context.font = '10px sans-serif'
  radar.forEach((item, index) => {
    const angle = -Math.PI / 2 + index * Math.PI * 2 / radar.length
    const point = pointAt(index, radius + 15)
    const cosine = Math.cos(angle)
    context.textAlign = cosine > 0.3 ? 'left' : cosine < -0.3 ? 'right' : 'center'
    context.textBaseline = Math.sin(angle) > 0.3 ? 'top' : Math.sin(angle) < -0.3 ? 'bottom' : 'middle'
    context.fillText(`${item.name} ${item.value}`, point.x, point.y)
  })
}

function paintReportPoster(context, report, width, height) {
  if (!report || !report.top1 || !Array.isArray(report.displayScores)
      || !Array.isArray(report.topProfiles) || !Array.isArray(report.radar)) {
    throw new Error('报告长图数据不完整')
  }
  const scale = width / DESIGN_WIDTH
  context.save()
  context.scale(scale, scale)
  const canvasHeight = height / scale
  context.fillStyle = '#F5F2ED'
  context.fillRect(0, 0, DESIGN_WIDTH, canvasHeight)

  const gradient = context.createLinearGradient(18, 20, 357, 220)
  gradient.addColorStop(0, '#765844')
  gradient.addColorStop(0.58, '#A88066')
  gradient.addColorStop(1, '#C0A385')
  roundedRect(context, 18, 20, 339, 218, 18)
  context.fillStyle = gradient
  context.fill()
  context.fillStyle = 'rgba(255,255,255,.76)'
  context.font = '12px sans-serif'
  context.textAlign = 'left'
  context.textBaseline = 'alphabetic'
  context.fillText('职业锚测评报告 · 首要职业锚', 38, 54)
  context.fillStyle = '#FFFFFF'
  context.font = 'bold 30px sans-serif'
  context.fillText(report.top1.nameCn, 38, 105)
  context.fillStyle = 'rgba(255,255,255,.72)'
  context.font = '11px sans-serif'
  context.fillText(report.top1.nameEn || '', 38, 127)
  context.strokeStyle = 'rgba(255,255,255,.25)'
  context.beginPath()
  context.moveTo(38, 148)
  context.lineTo(337, 148)
  context.stroke()
  context.fillStyle = '#FFFFFF'
  context.font = '14px sans-serif'
  context.fillText(fitText(context, report.top1.tagline, 295), 38, 185)

  fillRoundedRect(context, 18, 254, 339, 442, 16, '#FFFFFF')
  context.fillStyle = '#57483F'
  context.font = 'bold 19px sans-serif'
  context.fillText('八维职业锚', 36, 291)
  context.fillStyle = '#9B9189'
  context.font = '10px sans-serif'
  context.fillText('按原始分排序 · 展示分封顶 100', 36, 311)
  report.displayScores.slice(0, 8).forEach((item, index) => {
    const y = 344 + index * 42
    context.fillStyle = index < 3 ? '#6B5140' : '#71665F'
    context.font = index < 3 ? 'bold 12px sans-serif' : '12px sans-serif'
    context.textAlign = 'left'
    context.fillText(`${index + 1}. ${item.nameCn}`, 36, y)
    context.textAlign = 'right'
    context.font = 'bold 12px sans-serif'
    context.fillText(`${item.percent}/100`, 337, y)
    fillRoundedRect(context, 36, y + 10, 301, 7, 4, '#EEE9E3')
    fillRoundedRect(context, 36, y + 10, 301 * Math.max(0.04, Math.min(1, item.percent / 100)), 7, 4,
      index === 0 ? '#8A6A4F' : index < 3 ? '#B58C72' : '#CFC4BB')
  })

  fillRoundedRect(context, 18, 712, 339, 366, 16, '#FFFFFF')
  context.fillStyle = '#57483F'
  context.font = 'bold 19px sans-serif'
  context.textAlign = 'left'
  context.fillText('职业价值雷达', 36, 750)
  drawRadar(context, report.radar, 187.5, 902, 102)

  fillRoundedRect(context, 18, 1094, 339, 342, 16, '#FFFFFF')
  context.fillStyle = '#57483F'
  context.font = 'bold 19px sans-serif'
  context.textAlign = 'left'
  context.fillText('我的 Top 3 职业锚', 36, 1132)
  report.topProfiles.slice(0, 3).forEach((profile, index) => {
    const y = 1165 + index * 82
    context.fillStyle = '#D8CBC0'
    context.font = 'bold 27px sans-serif'
    context.fillText(`0${index + 1}`, 36, y + 23)
    context.fillStyle = '#57483F'
    context.font = 'bold 15px sans-serif'
    context.fillText(profile.nameCn, 84, y + 8)
    context.fillStyle = '#7E736C'
    context.font = '11px sans-serif'
    context.fillText(fitText(context, profile.tagline, 245), 84, y + 31)
    if (index < 2) {
      context.strokeStyle = '#EEE8E2'
      context.beginPath()
      context.moveTo(84, y + 58)
      context.lineTo(337, y + 58)
      context.stroke()
    }
  })

  context.fillStyle = '#81756D'
  context.font = '11px sans-serif'
  context.textAlign = 'left'
  context.fillText(`测评编号  ${report.resultNo}`, 28, 1474)
  context.textAlign = 'right'
  context.fillText(report.createdAt || '', 347, 1474)
  context.fillStyle = '#9B9189'
  context.font = '10px sans-serif'
  context.textAlign = 'center'
  context.fillText('测评结果仅供个人职业探索参考，不构成任何诊断或决策依据。', 187.5, 1512)
  context.fillStyle = '#8A6A4F'
  context.font = 'bold 12px sans-serif'
  context.fillText('职业锚测评', 187.5, 1550)
  context.restore()
}

module.exports = {
  posterSize,
  paintReportPoster,
  fitText
}
