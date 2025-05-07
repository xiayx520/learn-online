<template>
  <el-dialog
    title="绑定课程计划"
    :visible.sync="dialogVisible"
    width="50%"
    @close="handleClose"
  >
    <div class="bind-teachplan-dialog">
      <!-- 课程选择 -->
      <el-form :model="form" label-width="100px">
        <el-form-item label="选择课程">
          <el-select v-model="form.courseId" placeholder="请选择已发布的课程" @change="handleCourseChange">
            <el-option
              v-for="item in courseList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
          <div class="tip-text">注：只能选择已发布状态的课程</div>
        </el-form-item>
      </el-form>

      <!-- 课程计划树 -->
      <div class="teachplan-tree" v-if="form.courseId">
        <el-tree
          ref="tree"
          :data="teachplanList"
          node-key="id"
          show-checkbox
          :props="defaultProps"
          :default-checked-keys="boundTeachplanIds"
          :check-strictly="true"
          :default-expand-all="true"
          @check="handleCheck"
        >
          <template #default="{ node, data }">
            <span class="custom-tree-node">
              <span>{{ data.pname }}</span>
              <span>
                <el-button
                  v-if="isNodeBound(data.id)"
                  type="text"
                  size="mini"
                  @click.stop="handleUnbind(data.id)"
                >
                  解除绑定
                </el-button>
              </span>
            </span>
          </template>
        </el-tree>
      </div>
    </div>

    <span slot="footer" class="dialog-footer">
      <el-button @click="handleClose">取 消</el-button>
      <el-button type="primary" @click="handleConfirm">确 定</el-button>
    </span>
  </el-dialog>
</template>

<script lang="ts">
import { Component, Prop, Vue, Watch } from 'vue-property-decorator'
import { bindTeachplan, unbindTeachplan, getTeachplans } from '@/api/works'
import { getCourseList, getTeachplanTree } from '@/api/course'

interface ICourseInfo {
  id: number
  name: string
}

interface ITeachplanNode {
  id: number
  pname: string
  grade: number
  parentid: string
  courseId: number
  isPreview: string
  orderby: number
  teachplanMedia?: {
    id?: number
    mediaId?: string
    teachplanId: number
    courseId: number
    mediaFilename?: string
  }
  teachPlanTreeNodes?: ITeachplanNode[]
  disabled?: boolean
}

@Component
export default class BindTeachplanDialog extends Vue {
  @Prop({ type: Boolean, default: false })
  dialogVisible!: boolean

  @Prop({ type: Number, required: true })
  workId!: number

  private form = {
    courseId: null as number | null
  }

  private courseList: ICourseInfo[] = [] // 课程列表
  private teachplanList: ITeachplanNode[] = [] // 课程计划树
  private selectedTeachplanIds: number[] = [] // 选中的课程计划ID
  private boundTeachplanIds: number[] = [] // 已绑定的课程计划ID

  private defaultProps = {
    children: 'teachPlanTreeNodes',
    label: 'pname',
    disabled: (data: ITeachplanNode) => data.parentid === "0" // parentid为"0"的大章节不可选
  }

  // 监听对话框显示状态
  @Watch('dialogVisible')
  async onDialogVisibleChange(val: boolean) {
    if (val) {
      await this.loadCourseList()
      await this.loadBoundTeachplans()
    }
  }

  // 加载课程列表
  private async loadCourseList() {
    try {
      const pageParams = {
        pageNo: 1,
        pageSize: 100
      }
      // 只传递查询条件作为请求体
      // 只传递查询条件作为请求体
      const queryCourseParamsDto = {
        publishStatus: '203002' // 已发布状态
      }
      const result = await getCourseList(pageParams, queryCourseParamsDto)
      this.courseList = result.items || []
    } catch (error) {
      this.$message.error('获取课程列表失败')
    }
  }

  // 加载已绑定的课程计划
  private async loadBoundTeachplans() {
    try {
      const result = await getTeachplans(this.workId)
      this.boundTeachplanIds = result.map(item => item.teachplanId)
    } catch (error) {
      this.$message.error('获取已绑定课程计划失败')
    }
  }

  // 课程选择改变
  private async handleCourseChange(courseId: number) {
    try {
      const result = await getTeachplanTree(courseId)
      // 确保每个节点都有children属性
      this.teachplanList = this.processTeachplanList(result || [])
      console.log('课程计划树:', this.teachplanList) // 添加日志以便调试
    } catch (error) {
      this.$message.error('获取课程计划失败')
    }
  }

  // 处理课程计划列表，设置节点是否可选
  private processTeachplanList(list: ITeachplanNode[]): ITeachplanNode[] {
    return list.map(node => {
      const processedNode = {
        ...node,
        disabled: node.parentid === "0", // parentid为"0"的大章节不可选
        teachPlanTreeNodes: node.teachPlanTreeNodes ? this.processTeachplanList(node.teachPlanTreeNodes) : []
      }
      return processedNode
    })
  }

  // 判断节点是否已绑定
  private isNodeBound(nodeId: number): boolean {
    return this.boundTeachplanIds.includes(nodeId)
  }

  // 解除绑定
  private async handleUnbind(teachplanId: number) {
    try {
      await this.$confirm('确定要解除此课程计划的绑定吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      
      await unbindTeachplan(this.workId, teachplanId)
      this.$message.success('解除绑定成功')
      
      // 更新已绑定的课程计划列表
      await this.loadBoundTeachplans()
      
      // 更新树的选中状态
      const tree = this.$refs.tree as any
      if (tree) {
        tree.setCheckedKeys(this.boundTeachplanIds)
      }
      
      // 通知父组件刷新
      this.$emit('success')
    } catch (error) {
      if (error !== 'cancel') {
        console.error('解除绑定失败:', error)
        this.$message.error('解除绑定失败，请重试')
      }
    }
  }

  // 选中节点改变
  private handleCheck(node: ITeachplanNode, checkedNodes: any) {
    // 只允许选择非大章节（parentid !== "0"）
    const checkedKeys = checkedNodes.checkedKeys.filter((id: number) => {
      const node = this.findNode(this.teachplanList, id)
      return node && node.parentid !== "0"
    })
    this.selectedTeachplanIds = checkedKeys
  }

  // 在树中查找节点
  private findNode(nodes: ITeachplanNode[], id: number): ITeachplanNode | null {
    for (const node of nodes) {
      if (node.id === id) {
        return node
      }
      if (node.teachPlanTreeNodes) {
        const found = this.findNode(node.teachPlanTreeNodes, id)
        if (found) {
          return found
        }
      }
    }
    return null
  }

  // 确认绑定
  private async handleConfirm() {
    if (this.selectedTeachplanIds.length === 0) {
      this.$message.warning('请选择要绑定的课程计划')
      return
    }

    try {
      await bindTeachplan(this.workId, this.selectedTeachplanIds)
      this.$message.success('绑定成功')
      this.$emit('update:dialogVisible', false)
      this.$emit('success')
    } catch (error) {
      this.$message.error('绑定失败')
    }
  }

  // 关闭对话框
  private handleClose() {
    this.form.courseId = null
    this.teachplanList = []
    this.selectedTeachplanIds = []
    this.$emit('update:dialogVisible', false)
  }
}
</script>

<style lang="scss" scoped>
.bind-teachplan-dialog {
  .teachplan-tree {
    margin-top: 20px;
    max-height: 400px;
    overflow-y: auto;
  }

  .custom-tree-node {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
    padding-right: 8px;
  }

  .tip-text {
    font-size: 12px;
    color: #909399;
    margin-top: 5px;
  }
}
</style> 