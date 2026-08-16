package com.hioas.demo.mapper;

import com.hioas.demo.entity.UserChannelBinding;
import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserChannelBindingMapper extends BaseMapper<UserChannelBinding> {

    default List<UserChannelBinding> selectByUserAndApp(Long userId, Long appId) {
        return selectListByQuery(QueryWrapper.create()
                .where("user_id = ?", userId)
                .where("app_id = ?", appId));
    }

    default List<UserChannelBinding> selectByUserAndAppAndChannel(Long userId, Long appId, String channelCode) {
        return selectListByQuery(QueryWrapper.create()
                .where("user_id = ?", userId)
                .where("app_id = ?", appId)
                .where("channel_code = ?", channelCode));
    }

    /**
     * 删除用户对应用下指定通道的绑定关系
     */
    @Delete("delete from user_channel_binding where user_id = #{userId} and app_id = #{appId} and channel_code = #{channelCode}")
    int deleteByUserAndAppAndChannel(@Param("userId") Long userId, @Param("appId") Long appId, @Param("channelCode") String channelCode);
}
