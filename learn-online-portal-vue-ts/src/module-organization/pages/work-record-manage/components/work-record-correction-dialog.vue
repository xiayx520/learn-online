<template>
  <el-dialog :title="'题目：' + question" :visible.sync="syncDialogVisible" width="60%">
    <el-form ref="form" :model="workRecord" :rules="rules">
      <el-form-item label="学生答案">
        <div class="answer-content">{{ answer }}</div>
      </el-form-item>
      <el-form-item label="分数" prop="grade">
        <el-input-number
          v-model="workRecord.grade"
          :min="0"
          :max="100"
          :precision="1"
          :step="0.5"
          placeholder="请输入分数"
        ></el-input-number>
      </el-form-item>
      <el-form-item label="评语">
        <el-input
          type="textarea"
          :rows="5"
          v-model="workRecord.correctComment"
          placeholder="请输入作业点评（选填）"
          maxlength="500"
          show-word-limit
        ></el-input>
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button @click="syncDialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="handleCorrectionWorkRecord">提 交</el-button>
    </div>
  </el-dialog>
</template>

<script lang="ts">
import { Component, Vue, Prop, PropSync, Emit } from 'vue-property-decorator'
import { Form as ElForm } from 'element-ui'
import { IWorkRecordDTO } from '@/entity/work-record-page-list'
import { correctionWorkRecord } from '@/api/work-record'

@Component
export default class WorkRecordCorrectionDialog extends Vue {
  @PropSync('dialogVisible', { type: Boolean, required: true, default: false })
  syncDialogVisible!: boolean
  @Prop({ type: String, required: true, default: '' })
  readonly question!: string
  @Prop({ type: String, required: true, default: '' }) readonly answer!: string
  @Prop({ type: Object, required: true }) readonly workRecord!: IWorkRecordDTO

  // 表单验证规则
  private rules = {
    grade: [
      { required: true, message: '请输入分数', trigger: 'blur' },
      { type: 'number', message: '分数必须为数字', trigger: 'blur' }
    ]
  }

  /**
   * 批改作业
   */
  private async handleCorrectionWorkRecord() {
    // 表单验证
    const form = this.$refs.form as ElForm
    try {
      await form.validate()
    } catch (error) {
      return
    }

    console.log('提交的作业记录：', this.workRecord)

    let correction = {
      workRecordId: this.workRecord.id,
      grade: this.workRecord.grade,
      feedback: this.workRecord.correctComment || ''
    }

    console.log('提交的评分数据：', correction)

    try {
      // 提交表单
      await correctionWorkRecord(correction)

      // 用户提示
      this.$message.success('批改作业成功')
      this.syncDialogVisible = false

      // 刷新列表
      this.refreshList()
    } catch (error: any) {
      this.$message.error('批改作业失败：' + (error.message || '未知错误'))
    }
  }

  /**
   * 刷新列表
   */
  @Emit('refreshList')
  private refreshList() {}
}
</script>

<style lang="scss" scoped>
.el-form-item {
  margin-bottom: 20px;
}

.answer-content {
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 4px;
  min-height: 60px;
  white-space: pre-wrap;
  word-break: break-all;
}

.el-dialog {
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  
  .el-dialog__body {
    overflow-y: auto;
    flex: 1;
  }
}
</style>
