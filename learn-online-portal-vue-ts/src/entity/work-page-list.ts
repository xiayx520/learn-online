/**
 * 分页结果集
 */
export interface IWorkPageList {
  /**
   * 总记录数
   */
  total: number
  /**
   * 数据列表
   */
  records: IWorkDTO[]
  /**
   * 当前页码
   */
  pageNo: number
  /**
   * 每页大小
   */
  pageSize: number
}

/**
 * 作业DTO
 */
export interface IWorkDTO {
  /**
   * 作业ID
   */
  id: number
  /**
   * 标题
   */
  title: string
  /**
   * 描述
   */
  description: string
  /**
   * 状态：draft-草稿，published-已发布，archived-已归档
   */
  status: string
  /**
   * 创建人ID
   */
  createUser?: number
  /**
   * 创建时间
   */
  createDate: string
  /**
   * 修改时间
   */
  changeDate: string
  /**
   * 绑定课程名称列表
   */
  bindCourses?: string[]
  /**
   * 答题人数
   */
  userNum?: number
}

/**
 * 作业VO（新增/编辑请求）
 */
export interface IWorkVO {
  /**
   * 作业ID（编辑时需要）
   */
  id?: number
  /**
   * 标题
   */
  title: string
  /**
   * 描述
   */
  description: string
}
