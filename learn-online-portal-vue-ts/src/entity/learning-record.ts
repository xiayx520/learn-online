/**
 * 学习记录请求参数
 */
export interface ILearnRecordDTO {
  /**
   * 课程id
   */
  courseId: number;
  /**
   * 章节id
   */
  teachplanId: number;
  /**
   * 学习时长(秒)
   */
  learnLength: number;
  /**
   * 学习开始时间
   */
  startTime?: string;
  /**
   * 学习结束时间
   */
  endTime?: string;
}

/**
 * 学习记录查询条件
 */
export interface ILearnRecordQueryDTO {
  /**
   * 课程id
   */
  courseId?: number;
  /**
   * 课程名称
   */
  courseName?: string;
  /**
   * 用户id
   */
  userId?: string;
  /**
   * 开始日期
   */
  startDate?: string;
  /**
   * 结束日期
   */
  endDate?: string;
  /**
   * 当前页码
   */
  pageNo?: number;
  /**
   * 每页记录数
   */
  pageSize?: number;
}

/**
 * 学习记录视图对象
 */
export interface ILearnRecordVO {
  /**
   * 记录ID
   */
  id?: number;
  /**
   * 课程id
   */
  courseId?: number;
  /**
   * 课程名称
   */
  courseName?: string;
  /**
   * 用户ID
   */
  userId?: string;
  /**
   * 章节id
   */
  teachplanId?: number;
  /**
   * 章节名称
   */
  teachplanName?: string;
  /**
   * 最近学习时间
   */
  learnDate?: string;
  /**
   * 学习时长(秒)
   */
  learnLength?: number;
  /**
   * 学习进度(百分比)
   */
  learnProgress?: number;
}

/**
 * 学习统计视图对象
 */
export interface ILearnStatisticsVO {
  /**
   * 课程id
   */
  courseId?: number;
  /**
   * 课程名称
   */
  courseName?: string;
  /**
   * 总学习时长(秒)
   */
  totalLearnLength?: number;
  /**
   * 总章节数
   */
  totalChapters?: number;
  /**
   * 已完成章节数
   */
  completedChapters?: number;
  /**
   * 完成率(百分比)
   */
  completionRate?: number;
}

/**
 * 学习排行榜项
 */
export interface ILearnRankingItem {
  /**
   * 用户ID
   */
  userId?: string;
  /**
   * 用户名称
   */
  userName?: string;
  /**
   * 完成率/学习时长
   */
  value?: number;
  /**
   * 排名
   */
  rank?: number;
} 