/*创建订单*/
const createOrder = courseId => {
     let params = {
         courseId:courseId
     }
    return requestPost('/api/order/create',params);
}
/*查询课程*/
const queryCourse = courseId => {

    return requestGet('/openapi/portalview/course/get/'+courseId);
}
/*查询公司的统计信息*/
const queryCompanyStat = (companyId) => {
    return requestGet('/stat/company/company_stat_'+companyId+'.json');
}
/*查询课程的统计信息*/
const queryCourseStat = (courseId) => {
    return requestGet('/stat/course/course_stat_'+courseId+'.json');
}
/*查询 选课状态*/
const queryLearnstatus = courseId => {
    // 获取JWT令牌
    var jwt = getJwt();
    // 直接使用axios，不使用requestPost，因为后端返回格式与预期不同
    return axios.post('/api/learning/choosecourse/learnstatus/'+courseId, null, {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': jwt ? 'Bearer ' + jwt : ''
        }
    }).then(response => {
        // 直接返回响应数据，不再检查code字段
        return response.data;
    }).catch(error => {
        console.error("请求异常:", error);
        return Promise.reject(error);
    });
}
/*添加选课*/
const addChoosecourse = courseId => {
    // 获取JWT令牌
    var jwt = getJwt();
    // 直接使用axios，不使用requestPost，因为后端返回格式与预期不同
    return axios.post('/api/learning/choosecourse/'+courseId, null, {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': jwt ? 'Bearer ' + jwt : ''
        }
    }).then(response => {
        // 直接返回响应数据，不再检查code字段
        return response.data;
    }).catch(error => {
        console.error("请求异常:", error);
        return Promise.reject(error);
    });
}
/*生成支付二维码*/
const generatepaycode = (params) => {
    // 获取JWT令牌
    var jwt = getJwt();
    // 直接使用axios，不使用requestPost，因为后端返回格式与预期不同
    return axios.post('/api/orders/generatepaycode', params, {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': jwt ? 'Bearer ' + jwt : ''
        }
    }).then(response => {
        // 直接返回响应数据，不再检查code字段
        return response.data;
    }).catch(error => {
        console.error("请求异常:", error);
        return Promise.reject(error);
    });
}
/*查询支付结果*/
const querypayresult = (payNo) => {
    // 获取JWT令牌
    var jwt = getJwt();
    // 直接使用axios，不使用requestGet，因为后端返回格式与预期不同
    return axios.get('/api/orders/payresult?payNo='+payNo, {
        headers: {
            'Authorization': jwt ? 'Bearer ' + jwt : ''
        }
    }).then(response => {
        // 直接返回响应数据，不再检查code字段
        return response.data;
    }).catch(error => {
        console.error("请求异常:", error);
        return Promise.reject(error);
    });
}