-- 创建作业基本信息表
CREATE TABLE work_info (
    id bigint auto_increment comment '主键' primary key,
    title varchar(255) not null comment '作业标题',
    description text comment '作业描述',
    status varchar(10) default 'draft' comment '作业状态：draft-草稿，published-已发布，archived-已归档',
    create_user bigint comment '创建人',
    create_date datetime comment '创建时间',
    change_date datetime comment '修改时间'
) comment '作业基本信息表' charset = utf8mb3
                       row_format = DYNAMIC;

-- 创建作业记录表
CREATE TABLE work_record (
    id bigint auto_increment comment '主键' primary key,
    work_id bigint not null comment '作业ID',
    user_id bigint not null comment '学生ID',
    status varchar(10) default 'pending' comment '作业状态：pending-待完成，submitted-已提交，graded-已评分',
    submit_content text comment '提交内容',
    submit_date datetime comment '提交时间',
    create_date datetime comment '创建时间',
    change_date datetime comment '修改时间'
) comment '作业记录表' charset = utf8mb3
                       row_format = DYNAMIC;

-- 创建作业评分表
CREATE TABLE work_grade (
    id bigint auto_increment comment '主键' primary key,
    work_record_id bigint not null comment '作业记录ID',
    grade decimal(5,2) comment '分数',
    feedback text comment '评语',
    grader_id bigint comment '评分人ID',
    create_date datetime comment '创建时间',
    change_date datetime comment '修改时间'
) comment '作业评分表' charset = utf8mb3
                       row_format = DYNAMIC;