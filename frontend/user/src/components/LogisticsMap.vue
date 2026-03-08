<script setup>
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading, CircleCheck, Van, Location, Clock } from '@element-plus/icons-vue'
import { ORDER_API, request } from '@/api/config.js'

const props = defineProps({
  orderId: { type: [String, Number], required: true },
  userId: { type: [String, Number], required: true },
  productType: { type: Number, default: 2 },
  orderStatus: { type: Number, default: 3 },
  visible: { type: Boolean, default: false }
})

const emit = defineEmits(['arrived', 'close'])

const loading = ref(true)
const mapContainer = ref(null)
const logisticsInfo = ref(null)
const isArrived = ref(false)

// 物流轨迹时间轴（仅陆运）
const trackingHistory = ref([])
let lastCity = '' // 记录上一次的城市，避免重复添加
let lastGeocoderTime = 0 // 上次逆地理编码时间
const geocoderQueue = [] // 逆地理编码请求队列
let geocoderProcessing = false // 是否正在处理队列

let map = null
let vehicleMarker = null
let routePoints = []
const currentIndexRef = ref(0)
const totalPointsRef = ref(0)
let currentIndex = 0
let animationTimer = null
let checkTimer = null
let currentPosition = null // 当前位置，用于关闭时保存

const BAIDU_MAP_AK = 'ORPbAYGBKIeGKBGAr9n4UF9BRwbSHA8z'

// 计算进度百分比
const progress = computed(() => {
  if (isArrived.value) return 100
  if (totalPointsRef.value === 0) return 0
  return Math.round((currentIndexRef.value / Math.max(totalPointsRef.value - 1, 1)) * 100)
})

// 运输方式文本
const transportText = computed(() => {
  return logisticsInfo.value?.transportType === 1 ? '空运' : '陆运'
})

// 状态文本
const statusText = computed(() => {
  if (isArrived.value) return '已到达'
  return '运输中'
})

// 逆地理编码获取地址信息（带节流，确保不超过3次/秒）
const getAddressFromPoint = (point) => {
  return new Promise((resolve) => {
    const now = Date.now()
    const minInterval = 350 // 最小间隔350ms，确保不超过3次/秒
    const waitTime = Math.max(0, minInterval - (now - lastGeocoderTime))
    
    setTimeout(() => {
      lastGeocoderTime = Date.now()
      const { BMapGL } = window
      const geocoder = new BMapGL.Geocoder()
      geocoder.getLocation(point, (result) => {
        if (result) {
          const addr = result.addressComponents
          resolve({
            province: addr.province || '',
            city: addr.city || '',
            district: addr.district || '',
            full: result.address || ''
          })
        } else {
          resolve(null)
        }
      })
    }, waitTime)
  })
}

// 添加轨迹记录（使用队列处理，避免并发）
const addTrackingRecord = (point, status = 'transit') => {
  if (!logisticsInfo.value || logisticsInfo.value.transportType === 1) return // 仅陆运
  
  // 添加到队列
  geocoderQueue.push({ point, status })
  processGeocoderQueue()
}

// 处理逆地理编码队列
const processGeocoderQueue = async () => {
  if (geocoderProcessing || geocoderQueue.length === 0) return
  geocoderProcessing = true
  
  while (geocoderQueue.length > 0) {
    const { point, status } = geocoderQueue.shift()
    
    const addr = await getAddressFromPoint(point)
    if (!addr) continue
    
    // 如果城市相同，不重复添加（到达状态除外）
    const cityKey = addr.province + addr.city
    if (cityKey === lastCity && status !== 'arrived') continue
    lastCity = cityKey
    
    const now = new Date()
    const timeStr = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
    
    trackingHistory.value.unshift({
      time: timeStr,
      province: addr.province,
      city: addr.city,
      district: addr.district,
      status,
      description: status === 'arrived' ? '已到达目的地' : 
                   status === 'start' ? '已发货，开始运输' : 
                   `途经 ${addr.city || addr.province}`
    })
    
    // 最多保留10条记录
    if (trackingHistory.value.length > 10) {
      trackingHistory.value.pop()
    }
  }
  
  geocoderProcessing = false
}

const loadBaiduMapScript = () => {
  return new Promise((resolve, reject) => {
    if (window.BMapGL) { resolve(); return }
    window.initBaiduMap = () => resolve()
    const script = document.createElement('script')
    script.src = `https://api.map.baidu.com/api?v=1.0&type=webgl&ak=${BAIDU_MAP_AK}&callback=initBaiduMap`
    script.onerror = reject
    document.head.appendChild(script)
  })
}

const fetchLogistics = async () => {
  try {
    const response = await request(ORDER_API.GET_LOGISTICS, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ orderId: props.orderId, userId: props.userId, productType: props.productType })
    })
    const result = await response.json()
    if (result.code === 0 && result.data) {
      logisticsInfo.value = result.data
      return result.data
    } else {
      throw new Error(result.message || '获取物流信息失败')
    }
  } catch (error) {
    console.error('获取物流信息失败:', error)
    ElMessage.error('获取物流信息失败')
    return null
  }
}

const createVehicleMarker = (type, position, rotation = 0) => {
  const { BMapGL } = window
  vehicleMarker = new BMapGL.Label('', { position, offset: new BMapGL.Size(-15, -15) })
  vehicleMarker.setStyle({ border: 'none', background: 'transparent', overflow: 'visible' })
  vehicleMarker.setContent(`<div style="font-size:30px;transform:rotate(${rotation}deg);transform-origin:center;">✈️</div>`)
  map.addOverlay(vehicleMarker)
}

// 计算飞机旋转角度：让机头对准移动方向
const calcRotation = (fromPoint, toPoint) => {
  const dx = toPoint.lng - fromPoint.lng
  const dy = toPoint.lat - fromPoint.lat
  // atan2返回弧度，转角度。CSS rotate顺时针为正
  // 地图坐标系：x向右(东)为正，y向上(北)为正
  // atan2(dy,dx): 正东=0°, 正北=90°, 正西=180°, 正南=-90°
  // ✈️ emoji机头朝向右上(东北)，约45°
  // CSS rotate顺时针为正，所以需要: -(atan2角度 - 45)
  const angle = Math.atan2(dy, dx) * 180 / Math.PI
  return -(angle - 45)
}

const updateVehicleRotation = (fromPoint, toPoint) => {
  if (!vehicleMarker) return
  const rotation = calcRotation(fromPoint, toPoint)
  vehicleMarker.setContent(`<div style="font-size:30px;transform:rotate(${rotation}deg);transform-origin:center;">✈️</div>`)
}

const createTruckMarker = (position, rotation = 0) => {
  const { BMapGL } = window
  vehicleMarker = new BMapGL.Label('', { position, offset: new BMapGL.Size(-12, -18) })
  vehicleMarker.setStyle({ border: 'none', background: 'transparent', overflow: 'visible' })
  // 俯视图货车SVG - 车头朝上，轮子在车身下方微露
  const truckSvg = `<svg width="24" height="36" viewBox="0 0 24 36" style="transform:rotate(${rotation}deg);transform-origin:center;">
    <!-- 轮子（在车身下层） -->
    <rect x="1" y="6" width="4" height="8" rx="1" fill="#222"/>
    <rect x="19" y="6" width="4" height="8" rx="1" fill="#222"/>
    <rect x="1" y="22" width="4" height="8" rx="1" fill="#222"/>
    <rect x="19" y="22" width="4" height="8" rx="1" fill="#222"/>
    <!-- 车厢主体 -->
    <rect x="4" y="10" width="16" height="22" rx="2" fill="#3B82F6"/>
    <rect x="5" y="11" width="14" height="20" rx="1" fill="#60A5FA"/>
    <!-- 车厢纹理 -->
    <line x1="5" y1="17" x2="19" y2="17" stroke="#3B82F6" stroke-width="1"/>
    <line x1="5" y1="23" x2="19" y2="23" stroke="#3B82F6" stroke-width="1"/>
    <!-- 驾驶室 -->
    <rect x="5" y="2" width="14" height="10" rx="2" fill="#2563EB"/>
    <!-- 挡风玻璃 -->
    <rect x="7" y="3" width="10" height="5" rx="1" fill="#93C5FD"/>
  </svg>`
  vehicleMarker.setContent(truckSvg)
  map.addOverlay(vehicleMarker)
}

// 计算货车旋转角度
const calcTruckRotation = (fromPoint, toPoint) => {
  const dx = toPoint.lng - fromPoint.lng
  const dy = toPoint.lat - fromPoint.lat
  const angle = Math.atan2(dy, dx) * 180 / Math.PI
  // SVG货车默认车头朝上(北)，即90度方向
  return -(angle - 90)
}

const updateTruckRotation = (fromPoint, toPoint) => {
  if (!vehicleMarker) return
  const rotation = calcTruckRotation(fromPoint, toPoint)
  const truckSvg = `<svg width="24" height="36" viewBox="0 0 24 36" style="transform:rotate(${rotation}deg);transform-origin:center;">
    <rect x="1" y="6" width="4" height="8" rx="1" fill="#222"/>
    <rect x="19" y="6" width="4" height="8" rx="1" fill="#222"/>
    <rect x="1" y="22" width="4" height="8" rx="1" fill="#222"/>
    <rect x="19" y="22" width="4" height="8" rx="1" fill="#222"/>
    <rect x="4" y="10" width="16" height="22" rx="2" fill="#3B82F6"/>
    <rect x="5" y="11" width="14" height="20" rx="1" fill="#60A5FA"/>
    <line x1="5" y1="17" x2="19" y2="17" stroke="#3B82F6" stroke-width="1"/>
    <line x1="5" y1="23" x2="19" y2="23" stroke="#3B82F6" stroke-width="1"/>
    <rect x="5" y="2" width="14" height="10" rx="2" fill="#2563EB"/>
    <rect x="7" y="3" width="10" height="5" rx="1" fill="#93C5FD"/>
  </svg>`
  vehicleMarker.setContent(truckSvg)
}

const reportPosition = async (point) => {
  if (!logisticsInfo.value) return
  try {
    const response = await request(ORDER_API.CHECK_DELIVERED, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ logisticsId: logisticsInfo.value.logisticsId, currentLat: point.lat.toString(), currentLng: point.lng.toString() })
    })
    const result = await response.json()
    if (result.code === 0 && result.data?.isArrived) {
      isArrived.value = true
      stopAnimation()
      ElMessage.success('货物已到达！')
      emit('arrived')
    }
  } catch (error) {
    console.error('上报位置失败:', error)
  }
}

const stopAnimation = () => {
  if (animationTimer) { 
    cancelAnimationFrame(animationTimer)
    animationTimer = null 
  }
  if (checkTimer) { clearInterval(checkTimer); checkTimer = null }
}

// 关闭时保存当前位置
const saveCurrentPosition = async () => {
  if (!logisticsInfo.value || !currentPosition || isArrived.value) return
  try {
    await request(ORDER_API.CHECK_DELIVERED, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ 
        logisticsId: logisticsInfo.value.logisticsId, 
        currentLat: currentPosition.lat.toString(), 
        currentLng: currentPosition.lng.toString() 
      })
    })
  } catch (error) {
    console.error('保存当前位置失败:', error)
  }
}

// 清理资源
const cleanup = async () => {
  stopAnimation()
  await saveCurrentPosition()
  currentPosition = null
  if (map) { map.destroy(); map = null }
}

const startAnimation = (isAir = false, startIdx = 0) => {
  if (routePoints.length === 0) return
  currentIndex = startIdx
  currentIndexRef.value = startIdx
  totalPointsRef.value = routePoints.length
  currentPosition = routePoints[currentIndex]
  
  // 计算每段的累计距离
  const distances = [0]
  let totalDistance = 0
  for (let i = 1; i < routePoints.length; i++) {
    const dx = routePoints[i].lng - routePoints[i-1].lng
    const dy = routePoints[i].lat - routePoints[i-1].lat
    totalDistance += Math.sqrt(dx * dx + dy * dy)
    distances.push(totalDistance)
  }
  
  // 从起始点开始的距离
  const startDistance = distances[startIdx]
  const remainingDistance = totalDistance - startDistance
  
  const speed = 0.00015 // 每毫秒移动的距离（经纬度单位）
  const startTime = performance.now()
  
  const animate = (timestamp) => {
    if (!animationTimer) return
    
    const elapsed = timestamp - startTime
    const traveledDistance = startDistance + elapsed * speed
    
    if (traveledDistance >= totalDistance) {
      // 动画结束，到达目的地
      currentIndex = routePoints.length - 1
      currentIndexRef.value = currentIndex
      currentPosition = routePoints[currentIndex]
      if (vehicleMarker) vehicleMarker.setPosition(currentPosition)
      // 陆运添加到达记录
      if (!isAir) {
        addTrackingRecord(currentPosition, 'arrived')
      }
      // 上报最终位置，触发到达判断
      reportPosition(currentPosition)
      return
    }
    
    // 二分查找当前在哪两个点之间
    let fromIdx = 0
    for (let i = 1; i < distances.length; i++) {
      if (distances[i] > traveledDistance) {
        fromIdx = i - 1
        break
      }
      fromIdx = i
    }
    const toIdx = Math.min(fromIdx + 1, routePoints.length - 1)
    
    // 计算在这一段中的进度
    const segmentStart = distances[fromIdx]
    const segmentEnd = distances[toIdx]
    const segmentLength = segmentEnd - segmentStart
    const segmentProgress = segmentLength > 0 ? (traveledDistance - segmentStart) / segmentLength : 0
    
    const from = routePoints[fromIdx]
    const to = routePoints[toIdx]
    
    // 插值计算当前位置
    const lng = from.lng + (to.lng - from.lng) * segmentProgress
    const lat = from.lat + (to.lat - from.lat) * segmentProgress
    const interpolated = new window.BMapGL.Point(lng, lat)
    
    if (vehicleMarker) {
      vehicleMarker.setPosition(interpolated)
      if (isAir) {
        updateVehicleRotation(from, to)
      } else {
        updateTruckRotation(from, to)
      }
    }
    
    currentPosition = interpolated
    
    // 更新进度
    if (fromIdx !== currentIndex) {
      currentIndex = fromIdx
      currentIndexRef.value = currentIndex
      if (currentIndex % 10 === 0) {
        reportPosition(interpolated)
        // 陆运时每隔一段距离记录轨迹
        if (!isAir && currentIndex % 50 === 0) {
          addTrackingRecord(interpolated, 'transit')
        }
      }
    }
    
    animationTimer = requestAnimationFrame(animate)
  }
  
  animationTimer = requestAnimationFrame(animate)
}

const drawFlightPath = (origin, dest, startPoint = null) => {
  const { BMapGL } = window
  const midLng = (origin.lng + dest.lng) / 2
  const midLat = (origin.lat + dest.lat) / 2
  const distance = Math.sqrt(Math.pow(dest.lng - origin.lng, 2) + Math.pow(dest.lat - origin.lat, 2))
  const controlLat = midLat + distance * 0.3
  routePoints = []
  for (let i = 0; i <= 100; i++) {
    const t = i / 100
    const lng = Math.pow(1 - t, 2) * origin.lng + 2 * (1 - t) * t * midLng + Math.pow(t, 2) * dest.lng
    const lat = Math.pow(1 - t, 2) * origin.lat + 2 * (1 - t) * t * controlLat + Math.pow(t, 2) * dest.lat
    routePoints.push(new BMapGL.Point(lng, lat))
  }
  map.addOverlay(new BMapGL.Polyline(routePoints, { strokeColor: '#409EFF', strokeWeight: 3, strokeOpacity: 0.8, strokeStyle: 'dashed' }))
  
  // 找到起始索引
  let startIdx = 0
  if (startPoint) {
    // 找到最近的路径点
    let minDist = Infinity
    for (let i = 0; i < routePoints.length; i++) {
      const dist = Math.pow(routePoints[i].lng - startPoint.lng, 2) + Math.pow(routePoints[i].lat - startPoint.lat, 2)
      if (dist < minDist) {
        minDist = dist
        startIdx = i
      }
    }
  }
  
  const initAngle = calcRotation(routePoints[startIdx], routePoints[Math.min(startIdx + 1, routePoints.length - 1)])
  createVehicleMarker('plane', routePoints[startIdx], initAngle)
  startAnimation(true, startIdx)
}

const drawFlightPathStatic = (origin, dest) => {
  const { BMapGL } = window
  const midLng = (origin.lng + dest.lng) / 2
  const midLat = (origin.lat + dest.lat) / 2
  const distance = Math.sqrt(Math.pow(dest.lng - origin.lng, 2) + Math.pow(dest.lat - origin.lat, 2))
  const controlLat = midLat + distance * 0.3
  const points = []
  for (let i = 0; i <= 100; i++) {
    const t = i / 100
    const lng = Math.pow(1 - t, 2) * origin.lng + 2 * (1 - t) * t * midLng + Math.pow(t, 2) * dest.lng
    const lat = Math.pow(1 - t, 2) * origin.lat + 2 * (1 - t) * t * controlLat + Math.pow(t, 2) * dest.lat
    points.push(new BMapGL.Point(lng, lat))
  }
  map.addOverlay(new BMapGL.Polyline(points, { strokeColor: '#67C23A', strokeWeight: 3, strokeOpacity: 0.8 }))
}

const drawDrivingPath = (origin, dest, startPoint = null) => {
  return new Promise((resolve) => {
    const { BMapGL } = window
    const driving = new BMapGL.DrivingRoute(map, {
      renderOptions: { map, autoViewport: false },
      onSearchComplete: (results) => {
        if (driving.getStatus() === 0) {
          routePoints = results.getPlan(0).getRoute(0).getPath()
          
          // 找到起始索引
          let startIdx = 0
          if (startPoint) {
            let minDist = Infinity
            for (let i = 0; i < routePoints.length; i++) {
              const dist = Math.pow(routePoints[i].lng - startPoint.lng, 2) + Math.pow(routePoints[i].lat - startPoint.lat, 2)
              if (dist < minDist) {
                minDist = dist
                startIdx = i
              }
            }
          }
          
          // 计算初始角度
          const nextIdx = Math.min(startIdx + 1, routePoints.length - 1)
          const initRotation = calcTruckRotation(routePoints[startIdx], routePoints[nextIdx])
          createTruckMarker(routePoints[startIdx], initRotation)
          
          // 添加发货记录
          if (startIdx === 0) {
            addTrackingRecord(origin, 'start')
          }
          
          startAnimation(false, startIdx)
        }
        resolve()
      }
    })
    driving.search(origin, dest)
  })
}

const drawDrivingPathStatic = (origin, dest) => {
  return new Promise((resolve) => {
    const { BMapGL } = window
    const driving = new BMapGL.DrivingRoute(map, {
      renderOptions: { map, autoViewport: false },
      onSearchComplete: () => resolve()
    })
    driving.search(origin, dest)
  })
}

const initMap = async (logistics) => {
  const { BMapGL } = window
  const origin = new BMapGL.Point(parseFloat(logistics.originLng), parseFloat(logistics.originLat))
  const dest = new BMapGL.Point(parseFloat(logistics.destLng), parseFloat(logistics.destLat))
  
  // 获取当前位置（如果有）
  let startPoint = null
  if (logistics.currentLat && logistics.currentLng) {
    startPoint = new BMapGL.Point(parseFloat(logistics.currentLng), parseFloat(logistics.currentLat))
  }
  
  map = new BMapGL.Map(mapContainer.value)
  map.enableScrollWheelZoom(true)
  map.setTilt(45)
  map.centerAndZoom(new BMapGL.Point((origin.lng + dest.lng) / 2, (origin.lat + dest.lat) / 2), 6)
  
  // 发货点 - 使用百度地图原生红色水滴Marker
  const originMarker = new BMapGL.Marker(origin)
  map.addOverlay(originMarker)
  const originLabel = new BMapGL.Label('发货点', { position: origin, offset: new BMapGL.Size(20, -10) })
  originLabel.setStyle({ border: 'none', background: 'rgba(255,255,255,0.9)', padding: '4px 8px', borderRadius: '4px', fontSize: '12px', color: '#67C23A' })
  map.addOverlay(originLabel)
  
  // 收货点 - 使用百度地图原生红色水滴Marker
  const destMarker = new BMapGL.Marker(dest)
  map.addOverlay(destMarker)
  const destLabel = new BMapGL.Label('收货点', { position: dest, offset: new BMapGL.Size(20, -10) })
  destLabel.setStyle({ border: 'none', background: 'rgba(255,255,255,0.9)', padding: '4px 8px', borderRadius: '4px', fontSize: '12px', color: '#409EFF' })
  map.addOverlay(destLabel)
  
  const currentStatus = props.orderStatus || logistics.orderStatus
  if (currentStatus === 4) {
    isArrived.value = true
    if (logistics.transportType === 1) {
      drawFlightPathStatic(origin, dest)
      const finalAngle = calcRotation(origin, dest)
      createVehicleMarker('plane', dest, finalAngle)
    } else {
      await drawDrivingPathStatic(origin, dest)
      const finalTruckAngle = calcTruckRotation(origin, dest)
      createTruckMarker(dest, finalTruckAngle)
    }
  } else {
    if (logistics.transportType === 1) {
      drawFlightPath(origin, dest, startPoint)
    } else {
      await drawDrivingPath(origin, dest, startPoint)
    }
  }
}

const init = async () => {
  loading.value = true
  try {
    await loadBaiduMapScript()
    const logistics = await fetchLogistics()
    if (logistics) await initMap(logistics)
  } catch (error) {
    console.error('初始化失败:', error)
    ElMessage.error('地图加载失败')
  } finally {
    loading.value = false
  }
}

watch(() => props.visible, async (val) => { 
  if (val) {
    init()
  } else {
    await cleanup()
  }
})
onMounted(() => { if (props.visible) init() })
onUnmounted(async () => { await cleanup() })
</script>

<template>
  <div class="logistics-wrapper">
    <div class="logistics-map-container">
      <div v-if="loading" class="loading-overlay">
        <div class="loading-content">
          <el-icon class="loading-icon"><Loading /></el-icon>
          <span>加载物流信息中...</span>
        </div>
      </div>
      <div ref="mapContainer" class="map-container"></div>
      <div v-if="isArrived" class="arrived-banner">
        <el-icon><CircleCheck /></el-icon>
        <span>货物已到达</span>
      </div>
    </div>
    
    <!-- 物流进度信息 -->
    <div v-if="logisticsInfo" class="logistics-info">
      <div class="info-row">
        <div class="info-item">
          <el-icon class="info-icon"><Van /></el-icon>
          <span class="info-label">运输方式</span>
          <span class="info-value">{{ transportText }}</span>
        </div>
        <div class="info-item">
          <el-icon class="info-icon"><Clock /></el-icon>
          <span class="info-label">运输状态</span>
          <span class="info-value" :class="{ arrived: isArrived }">{{ statusText }}</span>
        </div>
        <div class="info-item progress-item">
          <el-icon class="info-icon"><Location /></el-icon>
          <span class="info-label">运输进度</span>
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: progress + '%' }"></div>
          </div>
          <span class="progress-text">{{ progress }}%</span>
        </div>
      </div>
    </div>
    
    <!-- 物流轨迹时间轴（仅陆运） -->
    <div v-if="logisticsInfo?.transportType === 2 && trackingHistory.length > 0" class="tracking-timeline">
      <div class="timeline-title">物流轨迹</div>
      <div class="timeline-list">
        <div 
          v-for="(item, index) in trackingHistory" 
          :key="index" 
          class="timeline-item"
          :class="{ 'is-first': index === 0, 'is-arrived': item.status === 'arrived' }"
        >
          <div class="timeline-dot"></div>
          <div class="timeline-content">
            <div class="timeline-time">{{ item.time }}</div>
            <div class="timeline-desc">{{ item.description }}</div>
            <div v-if="item.province" class="timeline-location">
              {{ item.province }} {{ item.city }} {{ item.district }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.logistics-wrapper { display: flex; flex-direction: column; gap: 16px; }
.logistics-map-container { position: relative; width: 100%; height: 450px; border-radius: 12px; overflow: hidden; }
.map-container { width: 100%; height: 100%; }
.loading-overlay { position: absolute; top: 0; left: 0; right: 0; bottom: 0; background: rgba(255,255,255,0.9); display: flex; align-items: center; justify-content: center; z-index: 10; }
.loading-content { display: flex; flex-direction: column; align-items: center; gap: 12px; color: #909399; }
.loading-icon { font-size: 32px; animation: spin 1s linear infinite; }
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
.arrived-banner { position: absolute; top: 16px; right: 16px; display: flex; align-items: center; gap: 6px; background: rgba(255,255,255,0.95); color: #67C23A; padding: 8px 14px; border-radius: 6px; font-size: 13px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); z-index: 10; }
.arrived-banner .el-icon { font-size: 16px; }

/* 物流进度信息 */
.logistics-info { background: #fafafa; border-radius: 8px; padding: 16px 20px; }
.info-row { display: flex; align-items: center; gap: 32px; flex-wrap: wrap; }
.info-item { display: flex; align-items: center; gap: 8px; }
.info-icon { font-size: 16px; color: #909399; }
.info-label { font-size: 13px; color: #909399; }
.info-value { font-size: 13px; color: #303133; font-weight: 500; }
.info-value.arrived { color: #67C23A; }
.progress-item { flex: 1; min-width: 200px; }
.progress-bar { flex: 1; height: 6px; background: #e4e7ed; border-radius: 3px; overflow: hidden; margin: 0 8px; min-width: 100px; }
.progress-fill { height: 100%; background: linear-gradient(90deg, #ff8a5b, #ff6b3d); border-radius: 3px; transition: width 0.3s ease; }
.progress-text { font-size: 13px; color: #ff6b3d; font-weight: 500; min-width: 36px; }

/* 物流轨迹时间轴 */
.tracking-timeline { background: #fafafa; border-radius: 8px; padding: 16px 20px; max-height: 200px; overflow-y: auto; }
.timeline-title { font-size: 14px; font-weight: 500; color: #303133; margin-bottom: 12px; }
.timeline-list { position: relative; }
.timeline-item { display: flex; gap: 12px; padding-bottom: 16px; position: relative; }
.timeline-item:last-child { padding-bottom: 0; }
.timeline-item::before { content: ''; position: absolute; left: 5px; top: 12px; bottom: 0; width: 1px; background: #e4e7ed; }
.timeline-item:last-child::before { display: none; }
.timeline-dot { width: 11px; height: 11px; border-radius: 50%; background: #e4e7ed; flex-shrink: 0; margin-top: 4px; position: relative; z-index: 1; }
.timeline-item.is-first .timeline-dot { background: #ff6b3d; box-shadow: 0 0 0 3px rgba(255, 107, 61, 0.2); }
.timeline-item.is-arrived .timeline-dot { background: #67C23A; box-shadow: 0 0 0 3px rgba(103, 194, 58, 0.2); }
.timeline-content { flex: 1; min-width: 0; }
.timeline-time { font-size: 12px; color: #909399; margin-bottom: 2px; }
.timeline-desc { font-size: 13px; color: #303133; }
.timeline-item.is-first .timeline-desc { color: #ff6b3d; font-weight: 500; }
.timeline-item.is-arrived .timeline-desc { color: #67C23A; font-weight: 500; }
.timeline-location { font-size: 12px; color: #909399; margin-top: 2px; }
</style>
