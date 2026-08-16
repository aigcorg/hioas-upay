package com.hioas.demo.mapper;

import com.hioas.demo.entity.ChannelInstance;
import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ChannelInstanceMapper extends BaseMapper<ChannelInstance> {

    default List<ChannelInstance> selectByAppIdAndStatus(Long appId, Integer status) {
        QueryWrapper query = QueryWrapper.create();
        if (appId != null) query.where("app_id = ?", appId);
        if (status != null) query.where("status = ?", status);
        return selectListByQuery(query);
    }
}
