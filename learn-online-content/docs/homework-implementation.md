# 作业管理功能实现步骤

## 1. 数据库设计

### 1.1 作业基本信息表 (work_info)
```sql
create table work_info (
    id bigint auto_increment comment '主键' primary key,
    title varchar(255) not null comment '作业标题',
    description text comment '作业描述',
    bind_courses text comment '绑定课程，JSON数组格式',
    user_num int default 0 comment '答题人数',
    status varchar(10) default 'draft' comment '作业状态：draft-草稿，published-已发布，archived-已归档',
    create_user bigint comment '创建人',
    create_date datetime comment '创建时间',
    change_date datetime comment '修改时间'
) comment '作业基本信息表' collate = utf8mb4_general_ci;
```

### 1.2 作业记录表 (work_record)
```sql
create table work_record (
    id bigint auto_increment comment '主键' primary key,
    work_id bigint not null comment '作业ID',
    user_id bigint not null comment '学生ID',
    status varchar(10) default 'pending' comment '作业状态：pending-待完成，submitted-已提交，graded-已评分',
    submit_content text comment '提交内容',
    submit_date datetime comment '提交时间',
    create_date datetime comment '创建时间',
    change_date datetime comment '修改时间'
) comment '作业记录表' collate = utf8mb4_general_ci;
```

### 1.3 作业评分表 (work_grade)
```sql
create table work_grade (
    id bigint auto_increment comment '主键' primary key,
    work_record_id bigint not null comment '作业记录ID',
    grade decimal(5,2) comment '分数',
    feedback text comment '评语',
    grader_id bigint comment '评分人ID',
    create_date datetime comment '创建时间',
    change_date datetime comment '修改时间'
) comment '作业评分表' collate = utf8mb4_general_ci;
```

## 2. 接口设计

### 2.1 作业管理接口

#### 2.1.1 创建/更新作业
- 请求方法：POST
- 接口路径：/content/work/save
- 请求参数：
```json
{
    "id": "作业ID（更新时需要）",
    "title": "作业标题",
    "description": "作业描述"
}
```
- 返回参数：WorkInfo对象

#### 2.1.2 发布作业
- 请求方法：POST
- 接口路径：/content/work/publish/{workId}
- 路径参数：workId (作业ID)
- 返回参数：无

#### 2.1.3 归档作业
- 请求方法：POST
- 接口路径：/content/work/archive/{workId}
- 路径参数：workId (作业ID)
- 返回参数：无

#### 2.1.4 查询作业列表
- 请求方法：POST
- 接口路径：/content/work/list
- 请求参数：
  - pageNo: 页码
  - pageSize: 每页大小
  - status: 作业状态（可选）
- 返回参数：PageResult<WorkInfo>

### 2.2 作业提交接口

#### 2.2.1 提交作业
- 请求方法：POST
- 接口路径：/content/work/submit
- 请求参数：
```json
{
    "workId": "作业ID",
    "content": "提交内容"
}
```
- 返回参数：WorkRecord对象

#### 2.2.2 查询作业提交记录
- 请求方法：POST
- 接口路径：/content/work/record/list
- 请求参数：
  - pageNo: 页码
  - pageSize: 每页大小
  - workId: 作业ID（可选）
  - userId: 学生ID（可选）
  - status: 提交状态（可选）
- 返回参数：PageResult<WorkRecord>

### 2.3 作业评分接口

#### 2.3.1 评分
- 请求方法：POST
- 接口路径：/content/work/grade
- 请求参数：
```json
{
    "workRecordId": "作业记录ID",
    "grade": "分数",
    "feedback": "评语"
}
```
- 返回参数：无

## 3. 实现步骤

1. 创建数据库表
2. 创建实体类和DTO对象
3. 实现作业管理相关接口
4. 实现作业提交相关接口
5. 实现作业评分相关接口
6. 前端页面开发
   - 作业管理列表页面
   - 作业创建/编辑页面
   - 作业提交页面
   - 作业评分页面
7. 接口联调和测试
8. 功能验收和上线

## 4. 项目结构

### 4.1 模块划分

- **包路径**: com.xia.content

- **learn-online-content-api**: 提供作业管理相关API接口
- **learn-online-content-model**: 定义作业管理相关实体类和数据模型
- **learn-online-content-service**: 实现作业管理核心业务逻辑

### 4.2 依赖管理

项目依赖管理文件位于项目根目录：pom.xml

### 4.3 数据库脚本

作业管理相关数据库脚本位于：sql/create_homework_tables.sql

## 5. 前端实现步骤

### 5.1 作业列表页面（work-list.vue）改造

1. 添加作业状态筛选功能
   - 添加状态选择下拉框组件
   - 状态选项：草稿、已发布、已归档
   - 实现状态筛选查询功能

2. 完善作业信息展示
   - 添加作业状态列
   - 添加作业描述列
   - 优化作业信息展示样式

3. 实现作业状态管理
   - 添加发布按钮（草稿状态可用）
   - 添加归档按钮（已发布状态可用）
   - 实现状态切换功能

4. 优化作业提交和评分入口
   - 添加查看提交记录按钮
   - 添加批改作业按钮（已提交状态可用）
   - 实现跳转到相应功能页面

### 5.2 作业新增/编辑对话框（work-add-dialog.vue）改造

1. 完善作业表单
   - 添加作业描述输入框
   - 添加课程绑定选择框
   - 实现表单验证

2. 优化保存逻辑
   - 实现作业保存接口对接
   - 添加保存成功提示
   - 刷新作业列表

### 5.3 作业提交记录页面（work-record-list.vue）改造

1. 完善提交记录列表
   - 添加学生信息列
   - 添加提交时间列
   - 添加提交状态列

2. 实现评分功能
   - 添加评分对话框
   - 实现评分和反馈功能
   - 更新提交记录状态