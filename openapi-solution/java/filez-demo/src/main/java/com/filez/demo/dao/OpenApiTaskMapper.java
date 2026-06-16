package com.filez.demo.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.filez.demo.entity.OpenApiTaskEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * OpenAPI 通用任务 Mapper 接口
 */
@Mapper
public interface OpenApiTaskMapper extends BaseMapper<OpenApiTaskEntity> {

    /**
     * 根据 taskId 查询任务
     */
    OpenApiTaskEntity selectByTaskId(@Param("taskId") String taskId);

    /**
     * 分页查询任务列表
     */
    List<OpenApiTaskEntity> selectPageByUserId(@Param("userId") String userId,
                                                @Param("offset") int offset,
                                                @Param("pageSize") int pageSize);

    /**
     * 统计用户任务总数
     */
    int countByUserId(@Param("userId") String userId);

    /**
     * 根据 taskId 删除任务
     */
    int deleteByTaskId(@Param("taskId") String taskId);

    /**
     * 批量删除任务
     */
    int batchDeleteByTaskIds(@Param("taskIds") List<String> taskIds);
}
