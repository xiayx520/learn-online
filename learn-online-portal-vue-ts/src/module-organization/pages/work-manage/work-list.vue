<template>
  <div class="course-list portal-content">
    <div class="workspace">
      <div class="banner">
        <span class="primary-title">作业管理</span>
        <el-button
          type="primary"
          size="medium"
          class="btn-add el-button"
          @click="handleAdd"
        >+新增作业</el-button>
      </div>

      <!-- 搜索栏 -->
      <div class="searcher">
        <el-select v-model="queryParams.status" placeholder="作业状态" clearable style="width: 120px; margin-right: 10px">
          <el-option label="草稿" value="draft" />
          <el-option label="已发布" value="published" />
          <el-option label="已归档" value="archived" />
        </el-select>
        <el-button type="primary" @click="getList" style="margin-left: 10px">查询</el-button>
      </div>

      <!-- 数据列表 -->
      <el-table
        class="dataList"
        v-loading="loading"
        :data="list"
        border
        style="width: 100%"
        :header-cell-style="{ textAlign: 'center' }"
      >
        <el-table-column prop="title" label="作业标题" width="200" align="center"></el-table-column>

        <el-table-column prop="description" label="作业描述" width="300" align="center">
          <template slot-scope="scope">
            <el-tooltip class="item" effect="dark" :content="scope.row.description" placement="top-start">
              <span>{{ formatDescription(scope.row.description) }}</span>
            </el-tooltip>
          </template>
        </el-table-column>

        <el-table-column prop="userNum" label="答题人数" width="100" align="center"></el-table-column>

        <el-table-column label="状态" width="100" align="center">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="创建时间" align="center">
          <template slot-scope="scope">{{ scope.row.createDate }}</template>
        </el-table-column>

        <el-table-column label="操作" width="300" align="center">
          <template slot-scope="scope">
            <!-- 草稿状态：可编辑、发布、删除 -->
            <template v-if="scope.row.status === 'draft'">
              <el-button type="text" size="mini" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button type="text" size="mini" @click="handleBindTeachplan(scope.row.id)">绑定课程计划</el-button>
              <el-button type="text" size="mini" @click="handlePublish(scope.row.id)">发布</el-button>
              <el-button type="text" size="mini" @click="handleOpenDeleteWorkConfirm(scope.row.id)">移除</el-button>
            </template>
            <!-- 发布状态：可下线、归档，可批改 -->
            <template v-else-if="scope.row.status === 'published'">
              <el-button type="text" size="mini" @click="handleGrade(scope.row.id)">批改</el-button>
              <el-button type="text" size="mini" @click="handleUnpublish(scope.row.id)">下线</el-button>
              <el-button type="text" size="mini" @click="handleArchive(scope.row.id)">归档</el-button>
            </template>
            <!-- 归档状态：可取消归档，可查看批改 -->
            <template v-else-if="scope.row.status === 'archived'">
              <el-button type="text" size="mini" @click="handleGrade(scope.row.id)">查看批改</el-button>
              <el-button type="text" size="mini" @click="handleUnarchive(scope.row.id)">取消归档</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <!-- 翻页控制 -->
      <div class="dataList-pagination">
        <el-pagination
          v-if="total > 0"
          :current-page.sync="queryParams.pageNo"
          :page-size="queryParams.pageSize"
          :total="total"
          layout="total, prev, pager, next"
          style="margin-top: 20px; text-align: right"
          @current-change="getList"
        />
      </div>
    </div>

    <!-- 上传资料对话框 -->
    <work-add-dialog
      :dialogVisible.sync="dialogVisible"
      :work="work"
      @refreshList="getList"
    ></work-add-dialog>

    <!-- 绑定课程计划对话框 -->
    <bind-teachplan-dialog
      :dialogVisible.sync="bindTeachplanDialogVisible"
      :workId="currentWorkId"
      @success="getList"
    ></bind-teachplan-dialog>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Watch } from 'vue-property-decorator'
import Pagination from '@/components/pagination/index.vue'
import WorkAddDialog from './components/work-add-dialog.vue'
import BindTeachplanDialog from './components/bind-teachplan-dialog.vue'
import { IWorkPageList, IWorkDTO, IWorkVO } from '@/entity/work-page-list'
import { getWorkPageList, deleteWork, defaultWork, publishWork, archiveWork, unpublishWork, unarchiveWork } from '@/api/works'

@Component({
  components: {
    Pagination,
    WorkAddDialog,
    BindTeachplanDialog
  },
  filters: {
    bindCoursesFormat: (value: string[]) => {
      if (value == null || value.length == 0) {
        return ''
      }
      return '《' + value.join('》，《') + '》'
    },
    descriptionFormat: (value: string) => {
      if (!value) return ''
      return value.length > 50 ? value.substring(0, 50) + '...' : value
    },
    statusFormat: (value: string) => {
      const statusMap: { [key: string]: string } = {
        draft: '草稿',
        published: '已发布',
        archived: '已归档'
      }
      return statusMap[value] || value
    },
    statusTypeFormat: (value: string) => {
      const typeMap: { [key: string]: string } = {
        draft: '',
        published: 'success',
        archived: 'info'
      }
      return typeMap[value] || ''
    }
  }
})
export default class WorkList extends Vue {
  // 是否载入中
  private loading: boolean = false
  // 请求参数Query
  private queryParams = {
    pageNo: 1,
    pageSize: 10,
    status: ''
  }
  // 作业列表
  private list: IWorkDTO[] = []
  // 新增作业对话框
  private dialogVisible: boolean = false
  // 单条作业
  private work: IWorkVO = Object.assign({}, defaultWork)
  // 总记录数
  private total: number = 0
  private bindTeachplanDialogVisible = false
  private currentWorkId: number | null = null

  created() {
    this.getList()
  }

  /**
   * 作业列表
   */
  private async getList() {
    try {
      this.loading = true
      // 构造分页参数
      const pageParams = {
        pageNo: this.queryParams.pageNo,
        pageSize: this.queryParams.pageSize
      }
      // 构造查询参数
      const queryParams = {
        status: this.queryParams.status || undefined
      }
      const result = await getWorkPageList(pageParams, queryParams)
      this.list = result.records
      this.total = result.total
      this.queryParams.pageNo = result.pageNo
      this.queryParams.pageSize = result.pageSize
    } catch (error) {
      console.error('获取作业列表失败:', error)
      this.$message.error('获取作业列表失败')
    } finally {
      this.loading = false
    }
  }

  /**
   * 新增作业
   */
  private handleAdd() {
    this.work = Object.assign({}, defaultWork)
    this.dialogVisible = true
  }

  /**
   * 编辑作业
   */
  private handleEdit(row: IWorkDTO) {
    this.work = {
      id: row.id,
      title: row.title,
      description: row.description
    }
    this.dialogVisible = true
  }

  /**
   * 发布作业
   */
  private async handlePublish(workId: number) {
    try {
      await this.$confirm('确定要发布该作业吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await publishWork(workId)
      this.$message({
        type: 'success',
        message: '发布成功!'
      })
      this.getList()
    } catch (error) {
      if (error !== 'cancel') {
        this.$message.error('发布失败，请重试')
      }
    }
  }

  /**
   * 归档作业
   */
  private async handleArchive(workId: number) {
    try {
      await this.$confirm('确定要归档该作业吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await archiveWork(workId)
      this.$message({
        type: 'success',
        message: '归档成功!'
      })
      this.getList()
    } catch (error) {
      if (error !== 'cancel') {
        this.$message.error('归档失败，请重试')
      }
    }
  }

  /**
   * 移除作业
   */
  private handleOpenDeleteWorkConfirm(workId: number) {
    this.$confirm('此操作将永久移除该作业, 是否继续?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(async () => {
        await deleteWork(workId)
        this.getList()
        this.$message({
          type: 'success',
          message: '移除成功!'
        })
      })
      .catch(() => {
        this.$message({
          type: 'info',
          message: '已取消移除'
        })
      })
  }

  /**
   * 下线作业
   */
  private async handleUnpublish(workId: number) {
    try {
      await this.$confirm('确定要下线该作业吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await unpublishWork(workId)
      this.$message({
        type: 'success',
        message: '下线成功!'
      })
      this.getList()
    } catch (error) {
      if (error !== 'cancel') {
        this.$message.error('下线失败，请重试')
      }
    }
  }

  /**
   * 取消归档
   */
  private async handleUnarchive(workId: number) {
    try {
      await this.$confirm('确定要取消归档该作业吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await unarchiveWork(workId)
      this.$message({
        type: 'success',
        message: '取消归档成功!'
      })
      this.getList()
    } catch (error) {
      if (error !== 'cancel') {
        this.$message.error('取消归档失败，请重试')
      }
    }
  }

  /**
   * 格式化描述
   */
  private formatDescription(value: string): string {
    if (!value) return ''
    return value.length > 50 ? value.substring(0, 50) + '...' : value
  }

  /**
   * 格式化绑定课程
   */
  private formatBindCourses(value?: string[]): string {
    if (!value || value.length === 0) return ''
    return '《' + value.join('》，《') + '》'
  }

  /**
   * 获取状态类型
   */
  private getStatusType(status: string): string {
    const map: { [key: string]: string } = {
      draft: 'info',
      published: 'success',
      archived: 'warning'
    }
    return map[status] || 'info'
  }

  /**
   * 获取状态文本
   */
  private getStatusText(status: string): string {
    const map: { [key: string]: string } = {
      draft: '草稿',
      published: '已发布',
      archived: '已归档'
    }
    return map[status] || status
  }

  // 监控 watch
  // 搜索栏 - 状态筛选
  @Watch('queryParams.status')
  private watchStatus() {
    this.getList()
  }

  // 翻页 pageSize
  @Watch('queryParams.pageSize')
  private watchListQueryPageSize(newVal: number) {
    this.queryParams.pageNo = 1
  }

  bindCoursesFormat(bindCourses: string): string {
    if (!bindCourses) return '未绑定';
    try {
      const courses = JSON.parse(bindCourses);
      return courses.map((course: any) => course.name).join('、');
    } catch (e) {
      return '格式错误';
    }
  }

  /**
   * 绑定课程计划
   */
  private handleBindTeachplan(workId: number) {
    this.currentWorkId = workId
    this.bindTeachplanDialogVisible = true
  }

  /**
   * 跳转到批改页面
   */
  private handleGrade(workId: number) {
    this.$router.push({
      path: `/organization/work-record-overall`,
      query: { workId: workId.toString() }
    })
  }
}
</script>

<style lang="scss" scoped>
.course-list {
  .nav-bar {
    margin-top: 16px;
  }

  .workspace .banner .btn-add {
    float: right;
  }

  .searcher {
    margin-top: 16px;
    display: flex;
    align-items: center;

    .status-select {
      width: 120px;
      margin-right: 10px;
    }
  }

  .dataList {
    margin-top: 16px;

    .el-tag {
      min-width: 60px;
    }
  }

  .dataList-pagination {
    margin-top: 16px;
    text-align: center;
    width: 100%;
  }
}
</style>
