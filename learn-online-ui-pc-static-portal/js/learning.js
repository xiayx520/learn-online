var url = "/open";
const requestGetCourseInfo = (courseId) => {
    return  requestGet(url+"/content/course/whole/"+courseId,{});
}
const requestGetMeidaInfo = (mediaId,teachplanId,courseId) => {
    if(url=="/open"){
        return  requestGet(url+"/media/preview/"+mediaId,{});
    }else{
        return  requestGet(url+"/learning/open/learn/getvideo/"+courseId+"/"+teachplanId+"/"+mediaId,{});
    }
    
}

// 记录学习进度
const saveLearnRecord = (courseId, teachplanId, learnLength, teachplanName) => {
    if(url == "/open") {
        console.log("预览模式不记录学习进度");
        return Promise.resolve();
    }
    
    const data = {
        courseId: courseId,
        teachplanId: teachplanId,
        learnLength: learnLength,
        teachplanName: teachplanName
    };
    
    return requestPost(url+"/learning/learnrecord/save", data);
}

var location_url = String(window.location);
if(location_url.indexOf("/preview/")<0){
    url = "/api"
}