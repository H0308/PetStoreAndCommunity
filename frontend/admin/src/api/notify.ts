import request from '@/utils/http'

export interface NotifyEmailParams {
  userId: number
  email: string
  subject: string
  content: string
}

export function fetchSendNotifyEmail(data: NotifyEmailParams) {
  return request.post<boolean>({
    url: '/api/admin/notify/send',
    data
  })
}
