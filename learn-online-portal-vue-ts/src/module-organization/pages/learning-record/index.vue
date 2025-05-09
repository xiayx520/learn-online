/* eslint-disable */
<template>
  <div class="learn-record-manage">
    <el-form :model="queryParams" ref="searchForm" :inline="true" class="search-form-inline">
      <el-form-item label="课程名称" prop="courseName">
        <el-input v-model="queryParams.courseName" placeholder="请输入课程名称" clearable />
      </el-form-item>
      <el-form-item label="用户ID" prop="userId">
        <el-input v-model="queryParams.userId" placeholder="请输入用户ID" clearable />
      </el-form-item>
      <el-form-item label="学习时间" prop="dateRange">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="yyyy-MM-dd"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="20">
      <el-col :span="16">
        <el-table
          v-loading="loading"
          :data="recordList"
          border
          style="width: 100%"
        >
          <el-table-column label="课程名称" prop="courseName" />
          <el-table-column label="用户ID" prop="userId" width="120" />
          <el-table-column label="章节名称" prop="teachplanName" />
          <el-table-column label="学习日期" prop="learnDate" width="180" />
          <el-table-column label="学习时长" width="120">
            <template slot-scope="scope">
              <span>{{ formatTime(parseInt(scope.row.learnLength)) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="完成进度" width="120">
            <template slot-scope="scope">
              <span>{{ scope.row.learnProgress }}%</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="center">
            <template slot-scope="scope">
              <el-button
                size="mini"
                type="text"
                @click="viewStatistics(scope.row)"
              >
                查看统计
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-container">
          <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="queryParams.pageNo"
            :page-sizes="[10, 20, 50]"
            :page-size="queryParams.pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
          />
        </div>
      </el-col>

      <el-col :span="8">
        <el-tabs type="border-card">
          <el-tab-pane label="学习时长排行">
            <div class="rank-filter">
              <el-select v-model="rankFilterCourseId" placeholder="选择课程" clearable @change="handleRankFilter">
                <el-option
                  v-for="course in courseOptions"
                  :key="course.id"
                  :label="course.name"
                  :value="course.id"
                ></el-option>
              </el-select>
            </div>
            <div v-loading="rankLengthLoading" class="rank-list">
              <div 
                v-for="(item, index) in learnLengthRanking" 
                :key="'length_' + index"
                class="rank-item"
              >
                <div :class="['rank-number', index < 3 ? 'top-three' : '']">
                  {{ index + 1 }}
                </div>
                <div class="rank-info">
                  <div class="rank-name">{{ item.userName }}</div>
                  <div>{{ formatTime(item.value) }}</div>
                </div>
              </div>
              <div v-if="learnLengthRanking.length === 0" class="no-data">暂无排行数据</div>
            </div>
          </el-tab-pane>
          <el-tab-pane label="完成率排行">
            <div class="rank-filter">
              <el-select v-model="rankFilterCourseId" placeholder="选择课程" clearable @change="handleRankFilter">
                <el-option
                  v-for="course in courseOptions"
                  :key="course.id"
                  :label="course.name"
                  :value="course.id"
                ></el-option>
              </el-select>
            </div>
            <div v-loading="rankCompletionLoading" class="rank-list">
              <div 
                v-for="(item, index) in completionRanking" 
                :key="'completion_' + index"
                class="rank-item"
              >
                <div :class="['rank-number', index < 3 ? 'top-three' : '']">
                  {{ index + 1 }}
                </div>
                <div class="rank-info">
                  <div class="rank-name">{{ item.userName }}</div>
                  <div>{{ item.value.toFixed(1) }}%</div>
                </div>
              </div>
              <div v-if="completionRanking.length === 0" class="no-data">暂无排行数据</div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-col>
    </el-row>
  </div>
</template>
/* eslint-enable */

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import { 
  adminQueryLearnRecords, 
  getCompletionRateRanking, 
  getLearnLengthRanking 
} from '@/api/learning-record';
import { getCourseList } from '@/api/course';

// 为slot-scope添加类型声明以解决TypeScript错误
declare module 'vue/types/vue' {
  interface Vue {
    // eslint-disable-next-line
    scope: any;
  }
}

interface QueryParams {
  courseName?: string;
  userId?: string;
  startDate?: string;
  endDate?: string;
  pageNo: number;
  pageSize: number;
}

// 定义学习记录类型接口
interface RecordItem {
  id: string;
  courseId: string;
  courseName: string;
  userId: string;
  teachplanId: string;
  teachplanName: string;
  learnDate: string;
  learnLength: string;
  learnProgress: number;
}

// 定义排行榜项目类型接口
interface RankItem {
  userId: string;
  userName: string;
  value: number;
}

// 定义课程选项类型接口
interface CourseOption {
  id: string;
  name: string;
}

@Component({
  name: 'LearningRecordManage'
})
export default class LearningRecordManage extends Vue {
  private loading = false;
  private rankLengthLoading = false;
  private rankCompletionLoading = false;
  private recordList: RecordItem[] = [];
  private total = 0;
  private dateRange: string[] = [];
  private learnLengthRanking: RankItem[] = [];
  private completionRanking: RankItem[] = [];
  private rankFilterCourseId: string | null = null;
  private courseOptions: CourseOption[] = [];

  private queryParams: QueryParams = {
    courseName: undefined,
    userId: undefined,
    startDate: undefined,
    endDate: undefined,
    pageNo: 1,
    pageSize: 10
  };

  created() {
    this.getList();
    this.getRankings();
    this.getCourseOptions();
  }

  private async getList() {
    this.loading = true;
    try {
      const res = await adminQueryLearnRecords(this.queryParams);
      if (res.data && res.data.result && res.data.result.items) {
        this.recordList = res.data.result.items;
        this.total = parseInt(res.data.result.counts) || 0;
      } else {
        this.useMockRecordData();
      }
    } catch (error) {
      console.error('获取学习记录列表失败:', error);
      this.$message.error('获取学习记录列表失败，使用模拟数据');
      this.useMockRecordData();
    } finally {
      this.loading = false;
    }
  }

  private useMockRecordData() {
    // 模拟学习记录数据
    const mockRecords: RecordItem[] = [];
    const courseNames = ['Web前端开发', 'Java编程基础', 'Python数据分析', '人工智能入门', '大数据技术'];
    
    for (let i = 1; i <= 10; i++) {
      mockRecords.push({
        id: i.toString(),
        courseId: i.toString(),
        courseName: courseNames[Math.floor(Math.random() * courseNames.length)],
        userId: i.toString(),
        teachplanId: (i * 10).toString(),
        teachplanName: `第${i}章 内容标题`,
        learnDate: '2023-06-01 10:00:00',
        learnLength: (Math.floor(Math.random() * 7200)).toString(),
        learnProgress: Math.floor(Math.random() * 100)
      });
    }
    
    this.recordList = mockRecords;
    this.total = mockRecords.length;
  }

  private async getRankings() {
    this.rankLengthLoading = true;
    this.rankCompletionLoading = true;
    
    try {
      // 获取学习时长排行
      const lengthRes = await getLearnLengthRanking(this.rankFilterCourseId ? Number(this.rankFilterCourseId) : undefined);
      if (lengthRes.data && lengthRes.data.result) {
        this.learnLengthRanking = lengthRes.data.result;
      } else {
        this.useMockRankingData();
      }
      
      // 获取完成率排行
      const completionRes = await getCompletionRateRanking(this.rankFilterCourseId ? Number(this.rankFilterCourseId) : undefined);
      if (completionRes.data && completionRes.data.result) {
        this.completionRanking = completionRes.data.result;
      } else {
        this.useMockRankingData();
      }
    } catch (error) {
      console.error('获取排行榜数据失败:', error);
      this.$message.error('获取排行榜数据失败，使用模拟数据');
      this.useMockRankingData();
    } finally {
      this.rankLengthLoading = false;
      this.rankCompletionLoading = false;
    }
  }

  private useMockRankingData() {
    // 模拟学习时长排行榜数据
    const mockLearnLengthRanking: RankItem[] = [];
    // 模拟完成率排行榜数据
    const mockCompletionRanking: RankItem[] = [];
    
    const userNames = ['张三', '李四', '王五', '赵六', '钱七', '孙八', '周九', '吴十'];
    
    for (let i = 0; i < 8; i++) {
      // 学习时长排行
      mockLearnLengthRanking.push({
        userId: 'user' + (i + 1),
        userName: userNames[i],
        value: 36000 - i * 3600 + Math.random() * 1800 // 递减的学习时长
      });
      
      // 完成率排行
      mockCompletionRanking.push({
        userId: 'user' + (i + 1),
        userName: userNames[i],
        value: 100 - i * 5 - Math.random() * 5 // 递减的完成率
      });
    }
    
    this.learnLengthRanking = mockLearnLengthRanking;
    this.completionRanking = mockCompletionRanking;
  }

  private async getCourseOptions() {
    try {
      // 准备分页参数
      const pageParams = {
        pageNo: 1,
        pageSize: 100
      };
      
      // 准备查询条件，设置publishStatus为"已发布"
      const queryCourseParamsDto = {
        publishStatus: '203002' // 课程发布状态码：203002表示已发布
      };
      
      // 调用POST方法的API，分离页面参数和查询条件
      const res = await getCourseList(pageParams, queryCourseParamsDto);
      console.log('课程API返回数据:', res);
      
      if (res && res.items && res.items.length > 0) {
        // 将API返回的课程数据转换为选项格式
        this.courseOptions = res.items.map((item: any) => ({
          id: item.id,
          name: item.name
        }));
        console.log('解析后的课程选项:', this.courseOptions);
      } else {
        // 使用默认数据
        this.useDefaultCourseOptions();
      }
    } catch (error) {
      console.error('获取课程列表失败:', error);
      this.useDefaultCourseOptions();
    }
  }

  private useDefaultCourseOptions() {
    this.courseOptions = [
      { id: '1', name: 'Web前端开发' },
      { id: '2', name: 'Java编程基础' },
      { id: '3', name: 'Python数据分析' },
      { id: '4', name: '人工智能入门' },
      { id: '5', name: '大数据技术' }
    ];
  }

  private handleQuery() {
    this.queryParams.pageNo = 1;
    if (this.dateRange && this.dateRange.length === 2) {
      // 转换日期格式为ISO 8601格式
      const startDate = this.dateRange[0] + 'T00:00:00';
      const endDate = this.dateRange[1] + 'T23:59:59';
      this.queryParams.startDate = startDate;
      this.queryParams.endDate = endDate;
    } else {
      this.queryParams.startDate = undefined;
      this.queryParams.endDate = undefined;
    }
    this.getList();
  }

  private resetQuery() {
    (this.$refs.searchForm as any).resetFields();
    this.dateRange = [];
    this.queryParams = {
      courseName: undefined,
      userId: undefined,
      startDate: undefined,
      endDate: undefined,
      pageNo: 1,
      pageSize: 10
    };
    this.getList();
  }

  private handleSizeChange(size: number) {
    this.queryParams.pageSize = size;
    this.getList();
  }

  private handleCurrentChange(page: number) {
    this.queryParams.pageNo = page;
    this.getList();
  }

  private viewStatistics(row: RecordItem) {
    this.$router.push({
      path: '/organization/learning-statistics',
      query: {
        courseId: row.courseId,
        userId: row.userId
      }
    });
  }

  private handleRankFilter() {
    // 根据课程筛选排行榜数据
    this.getRankings();
  }

  private formatTime(seconds: number): string {
    if (!seconds) return '0小时';
    
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    
    if (hours > 0) {
      return `${hours}小时${minutes > 0 ? minutes + '分钟' : ''}`;
    } else if (minutes > 0) {
      return `${minutes}分钟`;
    } else {
      return `${seconds}秒`;
    }
  }
}
</script>

<style lang="scss" scoped>
.learn-record-manage {
  .search-form-inline {
    margin-bottom: 20px;
  }
  
  .pagination-container {
    margin-top: 20px;
    text-align: right;
  }
  
  .chart-container {
    height: 400px;
  }

  .rank-list {
    .rank-item {
      display: flex;
      align-items: center;
      margin-bottom: 15px;
      
      .rank-number {
        width: 24px;
        height: 24px;
        line-height: 24px;
        text-align: center;
        border-radius: 50%;
        background-color: #eee;
        margin-right: 10px;
        font-weight: bold;
        
        &.top-three {
          background-color: #409EFF;
          color: white;
        }
      }
      
      .rank-info {
        flex: 1;
        
        .rank-name {
          margin-bottom: 5px;
          font-weight: bold;
        }
      }
    }
  }

  .rank-filter {
    margin-bottom: 10px;
  }

  .no-data {
    text-align: center;
    color: #909399;
    padding: 10px;
  }
}
</style> 