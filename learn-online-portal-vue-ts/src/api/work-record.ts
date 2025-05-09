import { createAPI } from '@/utils/request'
import {
  IWorkRecordPageVO,
  IWorkRecOverallDTO,
  IWorkRecordDTO
} from '@/entity/work-record-page-list'

export const defaultWorkRecord: IWorkRecordDTO = {
  coursePubId: 0,
  teachplanId: 0,
  teachplanName: '',
  workId: 0,
  workRecordId: 0
}

// 作业提交记录[按照课程分组]
export async function getWorkRecordPageList(
  params: any,
  body: any
): Promise<IWorkRecordPageVO> {
  const { data } = await createAPI(
    '/teaching/work-record/list',
    'post',
    params,
    body
  )
  return data
}

// 作业批阅详情
export async function getWorkRecordReadOverAll(
  workId: number
): Promise<IWorkRecOverallDTO> {
  const { data } = await createAPI(
    `/content/work-record/read-over-all/${workId}`,
    'get'
  )
  return data
}

/**
 * 批改作业
 * @param data 评分信息
 */
export async function correctionWorkRecord(data: any) {
  const { data: responseData } = await createAPI(
    '/content/work-record/correction',
    'put',
    null,
    {
      workRecordId: data.workRecordId,
      grade: data.grade,
      feedback: data.feedback
    }
  )
  return responseData
}
