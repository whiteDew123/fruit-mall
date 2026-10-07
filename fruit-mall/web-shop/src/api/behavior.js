import request from '../utils/request'
import { useUserStore } from '../store/user'

/**
 * 行为埋点上报。
 * 埋点是推荐系统的数据源，失败不影响前端流程，因此这里吞掉异常。
 */
export const reportBehavior = (behavior, targetType, targetId, context) => {
  const userStore = useUserStore()
  return request
    .post('/shop/behavior/report', {
      behavior,
      targetType,
      targetId,
      sessionId: userStore.ensureAnonymousId(),
      anonymousId: userStore.anonymousId,
      contextJson: context ? JSON.stringify(context) : null
    })
    .catch(() => null)
}
