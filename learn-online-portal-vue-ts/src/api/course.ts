import { createAPI } from '@/utils/request'

interface PageParams {
  pageNo: number;
  pageSize: number;
}

interface QueryCourseParamsDto {
  auditStatus?: string;
  courseName?: string;
  publishStatus?: string;
}

/**
 * 获取课程列表(POST请求)
 * @param pageParams 分页参数
 * @param queryCourseParamsDto 查询条件
 */
export async function getCourseList(pageParams: PageParams, queryCourseParamsDto: QueryCourseParamsDto = {}) {
  const { data } = await createAPI('/content/course/list', 'post', pageParams, queryCourseParamsDto)
  return data
}

/**
 * 获取课程计划树形结构
 * @param courseId 课程ID
 */
export async function getTeachplanTree(courseId: number) {
  const { data } = await createAPI(`/content/teachplan/${courseId}/tree-nodes`, 'get')
  return data
}

/**
 * 获取课程详情
 */
export function getCourseDetail(courseId: string) {
  return createAPI(`/content/course/${courseId}`, 'get');
} 