<template>
  <div class="learning-statistics">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>课程学习统计</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="goBack">返回</el-button>
      </div>
      
      <el-form :inline="true" :model="queryParams" class="search-form-inline" ref="searchForm">
        <el-form-item label="课程">
          <el-select v-model="queryParams.courseId" placeholder="请选择课程" style="width: 250px">
            <el-option
              v-for="item in courseOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="getStatistics">查询</el-button>
        </el-form-item>
      </el-form>
      
      <div v-if="statistics" class="statistics-info">
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="stat-card">
              <div class="stat-title">总学习时长</div>
              <div class="stat-value">{{ formatTime(statistics.totalLearnLength || 0) }}</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="stat-card">
              <div class="stat-title">完成率</div>
              <div class="stat-value">
                {{ statistics.completionRate || 0 }}%
                <el-progress :percentage="statistics.completionRate || 0"></el-progress>
              </div>
            </div>
          </el-col>
        </el-row>
        
        <el-row :gutter="20" style="margin-top: 20px">
          <el-col :span="12">
            <div class="stat-card">
              <div class="stat-title">已完成章节数</div>
              <div class="stat-value">{{ statistics.completedChapters || 0 }} / {{ statistics.totalChapters || 0 }}</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="stat-card">
              <div class="stat-title">课程名称</div>
              <div class="stat-value">{{ statistics.courseName }}</div>
            </div>
          </el-col>
        </el-row>
      </div>
      
      <div v-else class="empty-data">
        <el-empty description="暂无统计数据"></el-empty>
      </div>
    </el-card>
    
    <el-card class="box-card" style="margin-top: 20px">
      <div slot="header" class="clearfix">
        <span>章节学习情况</span>
      </div>
      
      <el-table
        v-loading="loading"
        :data="recordList"
        border
        style="width: 100%"
      >
        <el-table-column type="index" width="50" label="序号" />
        <el-table-column prop="teachplanName" label="章节名称" min-width="200" />
        <el-table-column prop="learnLength" label="学习时长" width="150">
          <template v-slot:default="{ row }">
            {{ formatTime(row.learnLength || 0) }}
          </template>
        </el-table-column>
        <el-table-column prop="learnDate" label="学习时间" width="180" />
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="queryParams.pageNo"
          :page-sizes="[10, 20, 30, 50]"
          :page-size="queryParams.pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
        />
      </div>
    </el-card>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import { adminGetLearnStatistics, adminQueryLearnRecords } from '@/api/learning-record';

interface QueryParams {
  courseId?: number;
  userId?: string;
  pageNo: number;
  pageSize: number;
}

@Component({
  name: 'LearningStatistics'
})
export default class LearningStatistics extends Vue {
  private loading = false;
  private recordList: any[] = [];
  private statistics: any = null;
  private total = 0;
  private courseOptions = [
    { value: 1, label: '示例课程1' },
    { value: 2, label: '示例课程2' }
  ];
  
  private queryParams: QueryParams = {
    courseId: undefined,
    userId: this.$route.query.userId as string,
    pageNo: 1,
    pageSize: 10
  };
  
  created() {
    if (this.$route.query.courseId) {
      this.queryParams.courseId = Number(this.$route.query.courseId);
    }
    
    this.getList();
    if (this.queryParams.courseId) {
      this.getStatistics();
    }
  }
  
  private async getList() {
    if (!this.queryParams.courseId || !this.queryParams.userId) {
      return;
    }
    
    this.loading = true;
    try {
      const res = await adminQueryLearnRecords(this.queryParams);
      if (res.data && res.data.items) {
        this.recordList = res.data.items;
        this.total = res.data.counts;
      }
    } catch (error) {
      console.error('获取学习记录列表失败:', error);
      this.$message.error('获取学习记录列表失败');
    } finally {
      this.loading = false;
    }
  }
  
  private async getStatistics() {
    if (!this.queryParams.courseId || !this.queryParams.userId) {
      this.$message.warning('请选择课程');
      return;
    }
    
    try {
      const res = await adminGetLearnStatistics(
        this.queryParams.courseId,
        this.queryParams.userId
      );
      if (res.data) {
        this.statistics = res.data;
      }
      
      // 更新课程学习记录
      this.getList();
    } catch (error) {
      console.error('获取学习统计数据失败:', error);
      this.$message.error('获取学习统计数据失败');
    }
  }
  
  private handleSizeChange(size: number) {
    this.queryParams.pageSize = size;
    this.getList();
  }

  private handleCurrentChange(page: number) {
    this.queryParams.pageNo = page;
    this.getList();
  }
  
  private goBack() {
    this.$router.back();
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
.learning-statistics {
  .search-form-inline {
    margin-bottom: 20px;
  }
  
  .pagination-container {
    margin-top: 20px;
    text-align: right;
  }
  
  .statistics-info {
    margin: 20px 0;
    
    .stat-card {
      background-color: #f9f9f9;
      border-radius: 4px;
      padding: 20px;
      height: 120px;
      display: flex;
      flex-direction: column;
      justify-content: center;
      
      .stat-title {
        font-size: 16px;
        color: #606266;
        margin-bottom: 10px;
      }
      
      .stat-value {
        font-size: 28px;
        color: #303133;
        font-weight: bold;
      }
    }
  }
  
  .empty-data {
    padding: 40px 0;
  }
}
</style> 