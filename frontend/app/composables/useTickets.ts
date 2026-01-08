import { api } from '~/utils/api'

export const useTickets = async () => {
  const { data } = await api.get('/tickets')  // 呼叫 Java 後端 API
  return data
}
