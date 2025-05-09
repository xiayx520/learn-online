/**
 * 分页参数
 */
export interface PageParams {
  /**
   * 当前页码
   */
  pageNo: number
  /**
   * 每页记录数
   */
  pageSize: number
}

/**
 * 作业查询参数
 */
export interface QueryWorkParamsDto {
  /**
   * 作业状态
   */
  status?: string
}

/**
 * 作业提交记录查询参数
 */
export interface QueryWorkRecordParamsDto {
  workId: number
  status?: number | null
}