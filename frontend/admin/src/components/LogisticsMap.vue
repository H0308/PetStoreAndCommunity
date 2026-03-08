<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchLogistics, checkDelivered, type TransportVO } from '@/api/order'

const props = defineProps<{
  orderId: number
  userId: number
  productType: number
  orderStatus: number
  visible: boolean
}>()

const emit = defineEmits<{
  (e: 'arrived'): void
  (e: 'close'): void
}>()

const loading = ref(true)
const mapContainer = ref<HTMLElement | null>(null)
const logisticsInfo = ref<TransportVO | null>(null)
const isArrived = ref(false)

// 物流轨迹时间轴（仅陆运）
const trackingHistory = ref<Array<{
  time: string
  province: string
  city: string
  district: string
  status: string
  description: string
}>>([])
let lastCity = ''
let lastGeocoderTime = 0
const geocoderQueue: Array<{ point: any; status: string }> = []
let geocoderProcessing = false

let map: any = null
let vehicleMarker: any = null
let routePoints: any[] = []
const currentIndexRef = ref(0)
const totalPointsRef = ref(0)
let currentIndex = 0
let animationTimer: number | null = null
let currentPosition: any = null

const BAIDU_MAP_AK = 'ORPbAYGBKIeGKBGAr9n4UF9BRwbSHA8z'

const progress = computed(() => {
  if (isArrived.value) return 100
  if (totalPointsRef.value === 0) return 0
  return Math.round((currentIndexRef.value / Math.max(totalPointsRef.value - 1, 1)) * 100)
})

const transportText = computed(() => logisticsInfo.value?.transportType === 1 ? '空运' : '陆运')
const statusText = computed(() => isArrived.value ? '已到达' : '运输中')

// 逆地理编码获取地址信息（带节流）
const getAddressFromPoint = (point: any): Promise<{ province: string; city: string; district: string; full: string } | null> => {
  return new Promise((resolve) => {
    const now = Date.now()
    const minInterval = 350
    const waitTime = Math.max(0, minInterval - (now - lastGeocoderTime))
    
    setTimeout(() => {
      lastGeocoderTime = Date.now()
      const { BMapGL } = window as any
      const geocoder = new BMapGL.Geocoder()
      geocoder.getLocation(point, (result: any) => {
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

const addTrackingRecord = (point: any, status = 'transit') => {
  if (!logisticsInfo.value || logisticsInfo.value.transportType === 1) return
  geocoderQueue.push({ point, status })
  processGeocoderQueue()
}

const processGeocoderQueue = async () => {
  if (geocoderProcessing || geocoderQueue.length === 0) return
  geocoderProcessing = true
  
  while (geocoderQueue.length > 0) {
    const item = geocoderQueue.shift()
    if (!item) continue
    const { point, status } = item
    
    const addr = await getAddressFromPoint(point)
    if (!addr) continue
    
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
    
    if (trackingHistory.value.length > 10) {
      trackingHistory.value.pop()
    }
  }
  
  geocoderProcessing = false
}

const loadBaiduMapScript = (): Promise<void> => {
  return new Promise((resolve, reject) => {
    if ((window as any).BMapGL) { resolve(); return }
    ;(window as any).initBaiduMap = () => resolve()
    const script = document.createElement('script')
    script.src = `https://api.map.baidu.com/api?v=1.0&type=webgl&ak=${BAIDU_MAP_AK}&callback=initBaiduMap`
    script.onerror = () => reject(new Error('加载百度地图失败'))
    document.head.appendChild(script)
  })
}

const fetchLogisticsData = async () => {
  try {
    const result = await fetchLogistics(props.orderId, props.userId, props.productType)
    logisticsInfo.value = result
    return result
  } catch (error) {
    console.error('获取物流信息失败:', error)
    ElMessage.error('获取物流信息失败')
    return null
  }
}

const createVehicleMarker = (position: any, rotation = 0) => {
  const { BMapGL } = window as any
  vehicleMarker = new BMapGL.Label('', { position, offset: new BMapGL.Size(-15, -15) })
  vehicleMarker.setStyle({ border: 'none', background: 'transparent', overflow: 'visible' })
  vehicleMarker.setContent(`<div style="font-size:30px;transform:rotate(${rotation}deg);transform-origin:center;">✈️</div>`)
  map.addOverlay(vehicleMarker)
}

const calcRotation = (fromPoint: any, toPoint: any) => {
  const dx = toPoint.lng - fromPoint.lng
  const dy = toPoint.lat - fromPoint.lat
  const angle = Math.atan2(dy, dx) * 180 / Math.PI
  return -(angle - 45)
}

const updateVehicleRotation = (fromPoint: any, toPoint: any) => {
  if (!vehicleMarker) return
  const rotation = calcRotation(fromPoint, toPoint)
  vehicleMarker.setContent(`<div style="font-size:30px;transform:rotate(${rotation}deg);transform-origin:center;">✈️</div>`)
}

const createTruckMarker = (position: any, rotation = 0) => {
  const { BMapGL } = window as any
  vehicleMarker = new BMapGL.Label('', { position, offset: new BMapGL.Size(-12, -18) })
  vehicleMarker.setStyle({ border: 'none', background: 'transparent', overflow: 'visible' })
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
  map.addOverlay(vehicleMarker)
}

const calcTruckRotation = (fromPoint: any, toPoint: any) => {
  const dx = toPoint.lng - fromPoint.lng
  const dy = toPoint.lat - fromPoint.lat
  const angle = Math.atan2(dy, dx) * 180 / Math.PI
  return -(angle - 90)
}

const updateTruckRotation = (fromPoint: any, toPoint: any) => {
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

const reportPosition = async (point: any) => {
  if (!logisticsInfo.value) return
  try {
    const result = await checkDelivered({
      logisticsId: logisticsInfo.value.logisticsId,
      currentLat: point.lat.toString(),
      currentLng: point.lng.toString()
    })
    if (result?.isArrived) {
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
}

const saveCurrentPosition = async () => {
  if (!logisticsInfo.value || !currentPosition || isArrived.value) return
  try {
    await checkDelivered({
      logisticsId: logisticsInfo.value.logisticsId,
      currentLat: currentPosition.lat.toString(),
      currentLng: currentPosition.lng.toString()
    })
  } catch (error) {
    console.error('保存当前位置失败:', error)
  }
}

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
  
  const distances = [0]
  let totalDistance = 0
  for (let i = 1; i < routePoints.length; i++) {
    const dx = routePoints[i].lng - routePoints[i-1].lng
    const dy = routePoints[i].lat - routePoints[i-1].lat
    totalDistance += Math.sqrt(dx * dx + dy * dy)
    distances.push(totalDistance)
  }
  
  const startDistance = distances[startIdx]
  const speed = 0.00015
  const startTime = performance.now()
  
  const animate = (timestamp: number) => {
    if (!animationTimer) return
    
    const elapsed = timestamp - startTime
    const traveledDistance = startDistance + elapsed * speed
    
    if (traveledDistance >= totalDistance) {
      currentIndex = routePoints.length - 1
      currentIndexRef.value = currentIndex
      currentPosition = routePoints[currentIndex]
      if (vehicleMarker) vehicleMarker.setPosition(currentPosition)
      if (!isAir) addTrackingRecord(currentPosition, 'arrived')
      reportPosition(currentPosition)
      return
    }
    
    let fromIdx = 0
    for (let i = 1; i < distances.length; i++) {
      if (distances[i] > traveledDistance) {
        fromIdx = i - 1
        break
      }
      fromIdx = i
    }
    const toIdx = Math.min(fromIdx + 1, routePoints.length - 1)
    
    const segmentStart = distances[fromIdx]
    const segmentEnd = distances[toIdx]
    const segmentLength = segmentEnd - segmentStart
    const segmentProgress = segmentLength > 0 ? (traveledDistance - segmentStart) / segmentLength : 0
    
    const from = routePoints[fromIdx]
    const to = routePoints[toIdx]
    const { BMapGL } = window as any
    
    const lng = from.lng + (to.lng - from.lng) * segmentProgress
    const lat = from.lat + (to.lat - from.lat) * segmentProgress
    const interpolated = new BMapGL.Point(lng, lat)
    
    if (vehicleMarker) {
      vehicleMarker.setPosition(interpolated)
      if (isAir) updateVehicleRotation(from, to)
      else updateTruckRotation(from, to)
    }
    
    currentPosition = interpolated
    
    if (fromIdx !== currentIndex) {
      currentIndex = fromIdx
      currentIndexRef.value = currentIndex
      if (currentIndex % 10 === 0) {
        reportPosition(interpolated)
        if (!isAir && currentIndex % 50 === 0) addTrackingRecord(interpolated, 'transit')
      }
    }
    
    animationTimer = requestAnimationFrame(animate)
  }
  
  animationTimer = requestAnimationFrame(animate)
}

const drawFlightPath = (origin: any, dest: any, startPoint: any = null) => {
  const { BMapGL } = window as any
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
  
  let startIdx = 0
  if (startPoint) {
    let minDist = Infinity
    for (let i = 0; i < routePoints.length; i++) {
      const dist = Math.pow(routePoints[i].lng - startPoint.lng, 2) + Math.pow(routePoints[i].lat - startPoint.lat, 2)
      if (dist < minDist) { minDist = dist; startIdx = i }
    }
  }
  
  const initAngle = calcRotation(routePoints[startIdx], routePoints[Math.min(startIdx + 1, routePoints.length - 1)])
  createVehicleMarker(routePoints[startIdx], initAngle)
  startAnimation(true, startIdx)
}

const drawFlightPathStatic = (origin: any, dest: any) => {
  const { BMapGL } = window as any
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

const drawDrivingPath = (origin: any, dest: any, startPoint: any = null): Promise<void> => {
  return new Promise((resolve) => {
    const { BMapGL } = window as any
    const driving = new BMapGL.DrivingRoute(map, {
      renderOptions: { map, autoViewport: false },
      onSearchComplete: (results: any) => {
        if (driving.getStatus() === 0) {
          routePoints = results.getPlan(0).getRoute(0).getPath()
          let startIdx = 0
          if (startPoint) {
            let minDist = Infinity
            for (let i = 0; i < routePoints.length; i++) {
              const dist = Math.pow(routePoints[i].lng - startPoint.lng, 2) + Math.pow(routePoints[i].lat - startPoint.lat, 2)
              if (dist < minDist) { minDist = dist; startIdx = i }
            }
          }
          const nextIdx = Math.min(startIdx + 1, routePoints.length - 1)
          const initRotation = calcTruckRotation(routePoints[startIdx], routePoints[nextIdx])
          createTruckMarker(routePoints[startIdx], initRotation)
          if (startIdx === 0) addTrackingRecord(origin, 'start')
          startAnimation(false, startIdx)
        }
        resolve()
      }
    })
    driving.search(origin, dest)
  })
}

const drawDrivingPathStatic = (origin: any, dest: any): Promise<void> => {
  return new Promise((resolve) => {
    const { BMapGL } = window as any
    const driving = new BMapGL.DrivingRoute(map, {
      renderOptions: { map, autoViewport: false },
      onSearchComplete: () => resolve()
    })
    driving.search(origin, dest)
  })
}

const initMap = async (logistics: TransportVO) => {
  const { BMapGL } = window as any
  const origin = new BMapGL.Point(parseFloat(logistics.originLng), parseFloat(logistics.originLat))
  const dest = new BMapGL.Point(parseFloat(logistics.destLng), parseFloat(logistics.destLat))
  
  let startPoint = null
  if (logistics.currentLat && logistics.currentLng) {
    startPoint = new BMapGL.Point(parseFloat(logistics.currentLng), parseFloat(logistics.currentLat))
  }
  
  map = new BMapGL.Map(mapContainer.value)
  map.enableScrollWheelZoom(true)
  map.setTilt(45)
  map.centerAndZoom(new BMapGL.Point((origin.lng + dest.lng) / 2, (origin.lat + dest.lat) / 2), 6)
  
  const originMarker = new BMapGL.Marker(origin)
  map.addOverlay(originMarker)
  const originLabel = new BMapGL.Label('发货点', { position: origin, offset: new BMapGL.Size(20, -10) })
  originLabel.setStyle({ border: 'none', background: 'rgba(255,255,255,0.9)', padding: '4px 8px', borderRadius: '4px', fontSize: '12px', color: '#67C23A' })
  map.addOverlay(originLabel)
  
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
      createVehicleMarker(dest, finalAngle)
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
    const logistics = await fetchLogisticsData()
    if (logistics) await initMap(logistics)
  } catch (error) {
    console.error('初始化失败:', error)
    ElMessage.error('地图加载失败')
  } finally {
    loading.value = false
  }
}

watch(() => props.visible, async (val) => { 
  if (val) init()
  else await cleanup()
})
onMounted(() => { if (props.visible) init() })
onUnmounted(async () => { await cleanup() })
</script>

<template>
  <div class="logistics-wrapper">
    <div class="logistics-map-container">
      <div v-if="loading" class="loading-overlay">
        <div class="loading-content">
          <el-icon class="loading-icon"><i-ep-loading /></el-icon>
          <span>加载物流信息中...</span>
        </div>
      </div>
      <div ref="mapContainer" class="map-container"></div>
      <div v-if="isArrived" class="arrived-banner">
        <el-icon><i-ep-circle-check /></el-icon>
        <span>货物已到达</span>
      </div>
    </div>
    
    <div v-if="logisticsInfo" class="logistics-info">
      <div class="info-row">
        <div class="info-item">
          <el-icon class="info-icon"><i-ep-van /></el-icon>
          <span class="info-label">运输方式</span>
          <span class="info-value">{{ transportText }}</span>
        </div>
        <div class="info-item">
          <el-icon class="info-icon"><i-ep-clock /></el-icon>
          <span class="info-label">运输状态</span>
          <span class="info-value" :class="{ arrived: isArrived }">{{ statusText }}</span>
        </div>
        <div class="info-item progress-item">
          <el-icon class="info-icon"><i-ep-location /></el-icon>
          <span class="info-label">运输进度</span>
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: progress + '%' }"></div>
          </div>
          <span class="progress-text">{{ progress }}%</span>
        </div>
      </div>
    </div>
    
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
            <div v-if="item.province" class="timeline-location">{{ item.province }} {{ item.city }} {{ item.district }}</div>
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
.logistics-info { background: #fafafa; border-radius: 8px; padding: 16px 20px; }
.info-row { display: flex; align-items: center; gap: 32px; flex-wrap: wrap; }
.info-item { display: flex; align-items: center; gap: 8px; }
.info-icon { font-size: 16px; color: #909399; }
.info-label { font-size: 13px; color: #909399; }
.info-value { font-size: 13px; color: #303133; font-weight: 500; }
.info-value.arrived { color: #67C23A; }
.progress-item { flex: 1; min-width: 200px; }
.progress-bar { flex: 1; height: 6px; background: #e4e7ed; border-radius: 3px; overflow: hidden; margin: 0 8px; min-width: 100px; }
.progress-fill { height: 100%; background: linear-gradient(90deg, #409eff, #67c23a); border-radius: 3px; transition: width 0.3s ease; }
.progress-text { font-size: 13px; color: #409eff; font-weight: 500; min-width: 36px; }
.tracking-timeline { background: #fafafa; border-radius: 8px; padding: 16px 20px; max-height: 200px; overflow-y: auto; }
.timeline-title { font-size: 14px; font-weight: 500; color: #303133; margin-bottom: 12px; }
.timeline-list { position: relative; }
.timeline-item { display: flex; gap: 12px; padding-bottom: 16px; position: relative; }
.timeline-item:last-child { padding-bottom: 0; }
.timeline-item::before { content: ''; position: absolute; left: 5px; top: 12px; bottom: 0; width: 1px; background: #e4e7ed; }
.timeline-item:last-child::before { display: none; }
.timeline-dot { width: 11px; height: 11px; border-radius: 50%; background: #e4e7ed; flex-shrink: 0; margin-top: 4px; position: relative; z-index: 1; }
.timeline-item.is-first .timeline-dot { background: #409eff; box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.2); }
.timeline-item.is-arrived .timeline-dot { background: #67C23A; box-shadow: 0 0 0 3px rgba(103, 194, 58, 0.2); }
.timeline-content { flex: 1; min-width: 0; }
.timeline-time { font-size: 12px; color: #909399; margin-bottom: 2px; }
.timeline-desc { font-size: 13px; color: #303133; }
.timeline-item.is-first .timeline-desc { color: #409eff; font-weight: 500; }
.timeline-item.is-arrived .timeline-desc { color: #67C23A; font-weight: 500; }
.timeline-location { font-size: 12px; color: #909399; margin-top: 2px; }
</style>
