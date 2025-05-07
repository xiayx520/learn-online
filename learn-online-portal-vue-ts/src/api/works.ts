import { createAPI } from '@/utils/request'
import { IWorkPageList, IWorkDTO, IWorkVO } from '@/entity/work-page-list'
import { PageParams, QueryWorkParamsDto } from '@/entity/page-params'

export const defaultWork: IWorkVO = {
  title: '',
  description: ''
}

/**
 * 保存作业（新增/编辑）
 * @param data 作业信息
 */
export async function saveWork(data: IWorkVO): Promise<IWorkDTO> {
  const { data: responseData } = await createAPI('/content/work/save', 'post', null, data)
  return responseData
}

/**
 * 新增/编辑作业
 * @param data 作业信息
 */
export async function addWork(data: IWorkVO): Promise<IWorkDTO> {
  const { data: responseData } = await createAPI('/content/work/save', 'post', null, data)
  return responseData
}

/**
 * 分页条件查询作业列表
 * @param params 查询参数
 */
export async function getWorkPageList(
  params: PageParams & QueryWorkParamsDto
): Promise<IWorkPageList> {
  const { data: responseData } = await createAPI('/content/work/list', 'post', null, params)
  return {
    records: responseData.items || [],
    total: parseInt(responseData.counts) || 0,
    pageNo: parseInt(responseData.page) || 1,
    pageSize: parseInt(responseData.pageSize) || 10
  }
}

/**
 * 删除作业
 * @param workId 作业ID
 */
export async function deleteWork(workId: number): Promise<IWorkDTO> {
  const { data } = await createAPI(`/content/work/delete/${workId}`, 'delete')
  return data
}

/**
 * 发布作业
 * @param workId 作业ID
 */
export async function publishWork(workId: number): Promise<IWorkDTO> {
  const { data } = await createAPI(`/content/work/publish/${workId}`, 'post')
  return data
}

/**
 * 归档作业
 * @param workId 作业ID
 */
export async function archiveWork(workId: number): Promise<IWorkDTO> {
  const { data } = await createAPI(`/content/work/archive/${workId}`, 'post')
  return data
}

/**
 * 提交作业
 * @param data 提交信息
 */
export async function submitWork(data: any): Promise<any> {
  const { data: responseData } = await createAPI('/content/work/submit', 'post', null, data)
  return responseData
}

/**
 * 查询作业提交记录
 * @param params 查询参数
 */
export async function getWorkRecordList(params: any): Promise<any> {
  const { data } = await createAPI('/content/work/record/list', 'post', null, params)
  return data
}

/**
 * 评分
 * @param data 评分信息
 */
export async function gradeWork(data: any): Promise<any> {
  const { data: responseData } = await createAPI('/content/work/grade', 'post', null, data)
  return responseData
}

/**
 * 下线作业（将发布状态转为草稿状态）
 * @param workId 作业ID
 */
export async function unpublishWork(workId: number): Promise<IWorkDTO> {
  const { data } = await createAPI(`/content/work/unpublish/${workId}`, 'post')
  return data
}

/**
 * 取消归档（将归档状态转为发布状态）
 * @param workId 作业ID
 */
export async function unarchiveWork(workId: number): Promise<IWorkDTO> {
  const { data } = await createAPI(`/content/work/unarchive/${workId}`, 'post')
  return data
}

/**
 * 绑定课程计划
 * @param workId 作业ID
 * @param teachplanIds 课程计划ID列表
 */
export async function bindTeachplan(workId: number, teachplanIds: number[]): Promise<void> {
  await createAPI(`/content/work/bind-teachplan/${workId}`, 'post', null, teachplanIds)
}

/**
 * 解绑课程计划
 * @param workId 作业ID
 * @param teachplanId 课程计划ID
 */
export async function unbindTeachplan(workId: number, teachplanId: number): Promise<void> {
  await createAPI(`/content/work/unbind-teachplan/${workId}/${teachplanId}`, 'delete')
}

/**
 * 获取作业绑定的课程计划
 * @param workId 作业ID
 */
export async function getTeachplans(workId: number): Promise<any[]> {
  const { data } = await createAPI(`/content/work/teachplans/${workId}`, 'get')
  return data
}
