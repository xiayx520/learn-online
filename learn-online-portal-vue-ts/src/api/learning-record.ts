import { createAPI } from '@/utils/request';

// 查询学习记录列表
export function queryLearnRecords(params: any) {
  return createAPI('/learning/learnrecord/list', 'get', params);
}

// 获取课程学习统计
export function getLearnStatistics(courseId: number) {
  return createAPI(`/learning/learnrecord/statistics/${courseId}`, 'get');
}

// 获取用户总学习时长
export function getTotalLearnLength() {
  return createAPI('/learning/learnrecord/totaltime', 'get');
}

// 保存学习记录
export function saveLearnRecord(data: any) {
  return createAPI('/learning/learnrecord/save', 'post', undefined, data);
}

// 管理员查询学习记录列表
export function adminQueryLearnRecords(params: any) {
  return createAPI('/learning/learnrecord/admin/list', 'get', params);
}

// 管理员获取学习统计数据
export function adminGetLearnStatistics(courseId: number, userId: string) {
  return createAPI(`/learning/learnrecord/admin/statistics/${courseId}`, 'get', { userId });
}

// 获取学习完成率排行榜
export function getCompletionRateRanking(courseId?: number) {
  return createAPI('/learning/learnrecord/admin/ranking/completion', 'get', { courseId });
}

// 获取学习时长排行榜
export function getLearnLengthRanking(courseId?: number) {
  return createAPI('/learning/learnrecord/admin/ranking/learnlength', 'get', { courseId });
} 