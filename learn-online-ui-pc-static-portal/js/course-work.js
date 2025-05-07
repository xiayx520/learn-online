/**
 * 课程作业相关功能
 */
$(function() {
    // 获取当前课程ID - 支持两种参数名
    const courseId = getQueryVariable('courseId') || getQueryVariable('id');
    
    // 初始化
    if (courseId) {
        initCourseWork(courseId);
    }

    // 事件绑定
    $(document).on('click', '.btn-submit-work', handleSubmitWorkClick);
    $(document).on('click', '.btn-view-work', handleViewWorkClick);
    $(document).on('click', '.btn-cancel, .btn-close', closeModal);
    $(document).on('click', '.modal-backdrop', closeModal);
    $(document).on('click', '.work-submit-form .btn-submit', submitWorkContent);
});

/**
 * 初始化课程作业
 * @param {string} courseId 课程ID
 */
function initCourseWork(courseId) {
    // 获取课程作业数据
    getCourseWorkData(courseId)
        .then(data => {
            renderCourseWorkList(data);
        })
        .catch(error => {
            console.error('获取课程作业失败:', error);
            showNoDataMessage();
        });
}

/**
 * 获取课程作业数据
 * @param {string} courseId 课程ID
 * @returns {Promise} 返回Promise对象
 */
function getCourseWorkData(courseId) {
    return new Promise((resolve, reject) => {
        $.ajax({
            url: `http://www.51xuecheng.cn/api/content/course-work/${courseId}`,
            type: 'GET',
            success: function(response) {
                resolve(response);
            },
            error: function(xhr, status, error) {
                reject(error);
            }
        });
    });
}

/**
 * 渲染课程作业列表
 * @param {Array} data 课程计划数据
 */
function renderCourseWorkList(data) {
    const $container = $('.work-list-container');
    $container.empty();
    
    // 处理数据，提取所有具有作业的课程计划
    let hasWorks = false;
    const workItems = extractWorkItems(data);
    
    if (workItems.length > 0) {
        hasWorks = true;
        // 渲染每个作业项
        workItems.forEach(item => {
            const workTemplate = getWorkItemHtml(item);
            $container.append(workTemplate);
        });
    }
    
    // 显示或隐藏无数据提示
    if (hasWorks) {
        $('.no-data').hide();
    } else {
        showNoDataMessage();
    }
}

/**
 * 从课程计划树中提取作业信息
 * @param {Array} teachplans 课程计划树
 * @returns {Array} 作业列表
 */
function extractWorkItems(teachplans) {
    let workItems = [];
    
    function extractFromNode(node, parentName = '') {
        // 如果是小节且有关联作业
        if (node.grade === 2 && node.teachplanWork) {
            workItems.push({
                workId: node.teachplanWork.workId,
                teachplanId: node.id,
                workTitle: node.teachplanWork.workTitle,
                chapterName: `${parentName} > ${node.pname}`,
                description: node.teachplanWork.workInfo ? node.teachplanWork.workInfo.description : '',
                status: node.teachplanWork.workInfo ? node.teachplanWork.workInfo.status : 'draft'
            });
        }
        
        // 处理子节点
        if (node.teachPlanTreeNodes && node.teachPlanTreeNodes.length > 0) {
            const currentParentName = node.grade === 1 ? node.pname : parentName;
            node.teachPlanTreeNodes.forEach(child => {
                extractFromNode(child, currentParentName);
            });
        }
    }
    
    // 遍历课程计划树
    teachplans.forEach(node => {
        extractFromNode(node);
    });
    
    return workItems;
}

/**
 * 获取作业项HTML
 * @param {Object} item 作业项数据
 * @returns {string} HTML字符串
 */
function getWorkItemHtml(item) {
    const statusText = getStatusText(item.status);
    let template = $('#work-item-template').html();
    
    template = template.replace(/\{\{workId\}\}/g, item.workId)
        .replace(/\{\{teachplanId\}\}/g, item.teachplanId)
        .replace(/\{\{workTitle\}\}/g, item.workTitle)
        .replace(/\{\{chapterName\}\}/g, item.chapterName)
        .replace(/\{\{description\}\}/g, item.description)
        .replace(/\{\{status\}\}/g, item.status)
        .replace(/\{\{statusText\}\}/g, statusText);
    
    return template;
}

/**
 * 获取状态显示文本
 * @param {string} status 状态码
 * @returns {string} 状态文本
 */
function getStatusText(status) {
    switch(status) {
        case 'draft': return '草稿';
        case 'published': return '已发布';
        case 'archived': return '已归档';
        default: return '未知';
    }
}

/**
 * 显示无数据提示
 */
function showNoDataMessage() {
    $('.work-list-container').empty();
    $('.no-data').show();
}

/**
 * 处理提交作业按钮点击事件
 */
function handleSubmitWorkClick() {
    const $workItem = $(this).closest('.work-item');
    const workId = $workItem.data('work-id');
    const teachplanId = $workItem.data('teachplan-id');
    const workTitle = $workItem.find('.work-title h4').text();
    
    // 创建模态框
    createSubmitWorkModal(workId, teachplanId, workTitle);
}

/**
 * 处理查看作业按钮点击事件
 */
function handleViewWorkClick() {
    const $workItem = $(this).closest('.work-item');
    const workId = $workItem.data('work-id');
    const workTitle = $workItem.find('.work-title h4').text();
    
    // 获取作业提交记录
    getWorkRecord(workId)
        .then(data => {
            createViewWorkModal(data, workTitle);
        })
        .catch(error => {
            console.error('获取作业记录失败:', error);
            // 显示空记录
            createViewWorkModal(null, workTitle);
        });
}

/**
 * 创建提交作业的模态框
 * @param {string} workId 作业ID
 * @param {string} teachplanId 课程计划ID
 * @param {string} workTitle 作业标题
 */
function createSubmitWorkModal(workId, teachplanId, workTitle) {
    // 如果已存在模态框，先移除
    removeExistingModals();
    
    // 获取课程ID
    const courseId = getQueryVariable('courseId') || getQueryVariable('id');
    
    // 创建模态框
    let template = $('#work-submit-template').html();
    template = template.replace(/\{\{workTitle\}\}/g, workTitle);
    
    const $modal = $(template);
    $modal.data('work-id', workId);
    $modal.data('teachplan-id', teachplanId);
    $modal.data('course-id', courseId);
    
    // 创建模态框背景
    const $backdrop = $('<div class="modal-backdrop"></div>');
    
    // 添加到页面
    $('body').append($backdrop).append($modal);
}

/**
 * 获取作业提交记录
 * @param {string} workId 作业ID
 * @returns {Promise} 返回Promise对象
 */
function getWorkRecord(workId) {
    return new Promise((resolve, reject) => {
        $.ajax({
            url: `http://www.51xuecheng.cn/api/content/work-record/${workId}`,
            type: 'GET',
            success: function(response) {
                // 如果状态是已批改，则需要获取成绩和评语
                if (response && response.status === 'graded') {
                    // 获取作业评分信息
                    getWorkGradeInfo(response.id)
                        .then(gradeInfo => {
                            // 合并评分信息到作业记录中
                            if (gradeInfo) {
                                response.grade = gradeInfo.grade;
                                response.comment = gradeInfo.feedback;
                            }
                            resolve(response);
                        })
                        .catch(error => {
                            console.error('获取作业评分信息失败:', error);
                            resolve(response); // 即使获取评分失败，也返回原始作业记录
                        });
                } else {
                    resolve(response);
                }
            },
            error: function(xhr, status, error) {
                reject(error);
            }
        });
    });
}

/**
 * 获取作业评分信息
 * @param {string} recordId 作业记录ID
 * @returns {Promise} 返回Promise对象
 */
function getWorkGradeInfo(recordId) {
    return new Promise((resolve, reject) => {
        $.ajax({
            url: `http://www.51xuecheng.cn/api/content/work-grade/${recordId}`,
            type: 'GET',
            success: function(response) {
                resolve(response);
            },
            error: function(xhr, status, error) {
                reject(error);
            }
        });
    });
}

/**
 * 创建查看作业的模态框
 * @param {Object} record 作业记录
 * @param {string} workTitle 作业标题
 */
function createViewWorkModal(record, workTitle) {
    // 如果已存在模态框，先移除
    removeExistingModals();
    
    // 模板替换
    let template = $('#work-view-template').html();
    
    // 根据后端返回的数据设置内容和状态
    const content = record && record.submitContent ? record.submitContent : '暂无提交记录';
    
    // 评分相关信息处理
    let showGrade = 'display:none';
    let grade = '';
    let comment = '';
    
    if (record && record.status === 'graded') {
        showGrade = '';
        grade = record.grade || '暂无评分';
        comment = record.comment || '暂无评语';
    }
    
    // 设置状态文本
    let statusText = '未提交';
    let statusClass = 'status-pending';
    
    if (record) {
        if (record.status === 'submitted') {
            statusText = '已提交';
            statusClass = 'status-submitted';
        } else if (record.status === 'graded') {
            statusText = '已批改';
            statusClass = 'status-graded';
        }
    }
    
    // 添加状态到模板
    template = template.replace(/\{\{workTitle\}\}/g, workTitle)
        .replace(/\{\{content\}\}/g, content)
        .replace(/\{\{showGrade\}\}/g, showGrade)
        .replace(/\{\{grade\}\}/g, grade)
        .replace(/\{\{comment\}\}/g, comment)
        .replace(/\{\{statusText\}\}/g, statusText)
        .replace(/\{\{statusClass\}\}/g, statusClass);
    
    // 创建模态框背景
    const $backdrop = $('<div class="modal-backdrop"></div>');
    
    // 添加到页面
    $('body').append($backdrop).append($(template));
}

/**
 * 提交作业内容
 */
function submitWorkContent() {
    const $form = $(this).closest('.work-submit-form');
    const workId = $form.data('work-id');
    const teachplanId = $form.data('teachplan-id');
    const courseId = $form.data('course-id') || getQueryVariable('courseId') || getQueryVariable('id');
    const content = $form.find('.work-content').val();
    
    // 前端验证：内容不能为空
    if (!content || !content.trim()) {
        // 在表单下方显示错误信息
        const $errorMsg = $form.find('.error-message');
        if ($errorMsg.length === 0) {
            $form.append('<div class="error-message" style="color: red; margin-top: 10px;">作业内容不能为空</div>');
        } else {
            $errorMsg.text('作业内容不能为空').show();
        }
        return;
    }
    
    // 移除错误信息
    $form.find('.error-message').hide();
    
    // 提交作业
    $.ajax({
        url: 'http://www.51xuecheng.cn/api/content/work/submit',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            workId: workId,
            teachplanId: teachplanId,
            courseId: courseId, // 添加课程ID
            content: content
        }),
        success: function(response) {
            // 直接关闭模态框，不显示提示
            closeModal();
            
            // 更新作业状态显示
            const $workItem = $(`.work-item[data-work-id="${workId}"]`);
            if ($workItem.length > 0) {
                $workItem.find('.work-status').html('已提交').addClass('submitted');
                // 更新按钮文本
                $workItem.find('.btn-submit-work').text('查看作业').removeClass('btn-submit-work').addClass('btn-view-work');
            }
        },
        error: function(xhr, status, error) {
            console.error('提交作业失败:', error);
            
            // 获取并显示后端返回的错误信息
            let errorMessage = '提交作业失败，请重试';
            try {
                // 从响应中解析错误信息
                const response = xhr.responseJSON || (xhr.responseText ? JSON.parse(xhr.responseText) : null);
                
                if (response) {
                    // 优先使用errMessage字段（从截图看到的实际错误字段）
                    if (response.errMessage) {
                        errorMessage = response.errMessage;
                    }
                    // 如果没有errMessage字段但有message字段
                    else if (response.message) {
                        errorMessage = response.message;
                    }
                }
            } catch (e) {
                console.error('解析错误信息失败:', e);
            }
            
            // 在表单中显示错误信息
            const $errorMsg = $form.find('.error-message');
            if ($errorMsg.length === 0) {
                $form.append(`<div class="error-message" style="color: red; margin-top: 10px;">${errorMessage}</div>`);
            } else {
                $errorMsg.text(errorMessage).show();
            }
        }
    });
}

/**
 * 移除已存在的模态框
 */
function removeExistingModals() {
    $('.work-submit-form, .work-view, .modal-backdrop').remove();
}

/**
 * 关闭模态框
 */
function closeModal() {
    removeExistingModals();
}

/**
 * 获取URL查询参数
 * @param {string} variable 参数名
 * @returns {string|null} 参数值
 */
function getQueryVariable(variable) {
    const query = window.location.search.substring(1);
    const vars = query.split('&');
    for (let i = 0; i < vars.length; i++) {
        const pair = vars[i].split('=');
        if (pair[0] === variable) {
            return pair[1];
        }
    }
    return null;
} 