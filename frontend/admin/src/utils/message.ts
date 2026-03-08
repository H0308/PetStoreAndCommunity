/**
 * ElMessage 全局去重
 * 在 main.ts 中调用 setupMessageDedup() 即可
 */
const DEDUP_INTERVAL = 3000
const messageTimestamps = new Map<string, number>()

function createDedupFn(originalFn: Function) {
  return (options: any) => {
    const msg = typeof options === 'string' ? options : options?.message || ''
    const now = Date.now()
    const last = messageTimestamps.get(msg)
    if (last && now - last < DEDUP_INTERVAL) return
    messageTimestamps.set(msg, now)
    return originalFn(options)
  }
}

export function setupMessageDedup() {
  ElMessage.error = createDedupFn(ElMessage.error.bind(ElMessage))
  ElMessage.warning = createDedupFn(ElMessage.warning.bind(ElMessage))
  ElMessage.success = createDedupFn(ElMessage.success.bind(ElMessage))
  ElMessage.info = createDedupFn(ElMessage.info.bind(ElMessage))
}
