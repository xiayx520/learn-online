package com.xia.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xia.media.model.po.MediaProcess;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface MediaProcessMapper extends BaseMapper<MediaProcess> {

    /**
     * 根据分片参数查询任务
     * @param shardIndex
     * @param shardTotal
     * @param count
     * @return
     */
    @Select("select id, file_id, filename, bucket, file_path, status, create_date, finish_date, fail_count, url, errormsg " +
            "from media_process t " +
            "where t.id % #{shardTotal} = #{shardIndex} " +
            "and (t.status = '1' or t.status = '3') " +
            "and t.fail_count < 3 " +
            "limit #{count}")
    List<MediaProcess> selectListByShardIndex(@Param("shardIndex") int shardIndex, @Param("shardTotal") int shardTotal, @Param("count") int count);

    /**
     * 更新任务状态
     * @param taskId
     * @return
     */
    @Update("update media_process t set t.status = '4' " +
            "where (t.status = '1' or t.status = '3') " +
            "and t.fail_count < 3 " +
            "and t.id = #{taskId}")
    int updateTaskId(@Param("taskId") long taskId);
}
