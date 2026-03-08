<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as THREE from 'three'
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js'

import sceneGltf from '@/assets/scene.gltf?url'

const canvasRef = ref(null)
const loading = ref(true)
const error = ref(null)

let renderer, scene, camera, model, animFrameId
let mouseX = 0, mouseY = 0
let currentRotY = 0, currentRotX = 0

onMounted(() => {
  const canvas = canvasRef.value
  const w = canvas.clientWidth || 600
  const h = canvas.clientHeight || 600

  // Renderer
  renderer = new THREE.WebGLRenderer({ canvas, alpha: true, antialias: true })
  renderer.setSize(w, h)
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.shadowMap.enabled = true
  renderer.shadowMap.type = THREE.PCFSoftShadowMap
  renderer.toneMapping = THREE.ACESFilmicToneMapping
  renderer.toneMappingExposure = 1.3
  renderer.outputColorSpace = THREE.SRGBColorSpace

  // Scene
  scene = new THREE.Scene()

  // Camera
  camera = new THREE.PerspectiveCamera(40, w / h, 0.1, 100)
  camera.position.set(0, 0, 5)

  // Lights
  const ambient = new THREE.AmbientLight(0xffffff, 1.2)
  scene.add(ambient)

  const keyLight = new THREE.DirectionalLight(0xfff5e0, 2.5)
  keyLight.position.set(3, 5, 4)
  keyLight.castShadow = true
  scene.add(keyLight)

  const fillLight = new THREE.DirectionalLight(0xd0e8ff, 1.2)
  fillLight.position.set(-4, 2, 3)
  scene.add(fillLight)

  const rimLight = new THREE.DirectionalLight(0xffe0d0, 0.8)
  rimLight.position.set(0, -2, -3)
  scene.add(rimLight)

  // Load GLTF
  const loader = new GLTFLoader()
  loader.load(
    sceneGltf,
    (gltf) => {
      model = gltf.scene

      // 自动居中 + 缩放适配
      const box = new THREE.Box3().setFromObject(model)
      const center = box.getCenter(new THREE.Vector3())
      const size = box.getSize(new THREE.Vector3())
      const maxDim = Math.max(size.x, size.y, size.z)
      const scale = 2.1 / maxDim
      model.scale.setScalar(scale)
      // 水平居中，垂直下移让模型在画面中央偏下
      model.position.x = -center.x * scale
      model.position.y = -center.y * scale - size.y * scale * 0.18
      model.position.z = -center.z * scale
      model.userData.baseY = model.position.y
      // 模型默认朝后，旋转180°朝向正面
      model.rotation.y = Math.PI

      // 开启阴影，清除法线/凹凸/粗糙度贴图，保留颜色贴图
      model.traverse(child => {
        if (child.isMesh) {
          child.castShadow = true
          child.receiveShadow = true
          const mats = Array.isArray(child.material) ? child.material : [child.material]
          mats.forEach(mat => {
            mat.normalMap = null
            mat.bumpMap = null
            mat.roughnessMap = null
            mat.metalnessMap = null
            mat.roughness = 0.5
            mat.metalness = 0.0
            mat.needsUpdate = true
          })
        }
      })

      scene.add(model)
      loading.value = false

      // 启动动画
      startAnimate()
    },
    undefined,
    (err) => {
      console.error('GLTF load error:', err)
      error.value = '模型加载失败'
      loading.value = false
    }
  )

  // 鼠标跟随
  const onMouseMove = (e) => {
    mouseX = (e.clientX / window.innerWidth - 0.5) * 2
    mouseY = -(e.clientY / window.innerHeight - 0.5) * 2
  }
  window.addEventListener('mousemove', onMouseMove)

  // Resize
  const onResize = () => {
    const w = canvas.clientWidth
    const h = canvas.clientHeight
    if (!w || !h) return
    renderer.setSize(w, h)
    camera.aspect = w / h
    camera.updateProjectionMatrix()
  }
  window.addEventListener('resize', onResize)

  onUnmounted(() => {
    cancelAnimationFrame(animFrameId)
    window.removeEventListener('mousemove', onMouseMove)
    window.removeEventListener('resize', onResize)
    renderer.dispose()
  })
})

let t = 0
function startAnimate() {
  const animate = () => {
    animFrameId = requestAnimationFrame(animate)
    t += 0.016

    if (model) {
      // 漂浮（在初始位置基础上上下浮动）
      model.position.y = model.userData.baseY + Math.sin(t * 0.7) * 0.08

      // 鼠标视差跟随
      currentRotY += (mouseX * 0.4 - currentRotY) * 0.05
      currentRotX += (mouseY * 0.15 - currentRotX) * 0.05
      model.rotation.y = currentRotY
      model.rotation.x = currentRotX
    }

    renderer.render(scene, camera)
  }
  animate()
}
</script>

<template>
  <div class="cat-wrapper">
    <canvas ref="canvasRef" class="cat-canvas" />
    <div v-if="loading" class="cat-loading">
      <div class="loading-spinner" />
    </div>
    <div v-if="error" class="cat-error">{{ error }}</div>
  </div>
</template>

<style scoped>
.cat-wrapper {
  width: 100%;
  height: 100%;
  position: relative;
}

.cat-canvas {
  width: 100%;
  height: 100%;
  display: block;
}

.cat-loading {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-top-color: rgba(255, 255, 255, 0.8);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.cat-error {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
}
</style>
