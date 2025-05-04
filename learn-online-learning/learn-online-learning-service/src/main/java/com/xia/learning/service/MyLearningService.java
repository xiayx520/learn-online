package com.xia.learning.service;

import com.xia.base.model.RestResponse;

public interface MyLearningService {
    /**
     * 获取视频
     * @param userId
     * @param courseId
     * @param teachplanId
     * @param mediaId
     * @return
     */
    RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId);
}
