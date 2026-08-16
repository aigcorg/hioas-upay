package com.hioas.demo.mapper;

import com.hioas.demo.entity.UserAuth;
import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserAuthMapper extends BaseMapper<UserAuth> {

    default List<UserAuth> selectByUserAndApp(Long userId, Long appId) {
        return selectListByQuery(QueryWrapper.create()
                .where("user_id = ?", userId)
                .where("app_id = ?", appId));
    }

    default List<UserAuth> selectByUserAndAppAndChannelCodes(Long userId, Long appId, String channelCodes) {
        return selectListByQuery(QueryWrapper.create()
                .where("user_id = ?", userId)
                .where("app_id = ?", appId)
                .where("JSON_CONTAINS(?, JSON_QUOTE(channel_code))", channelCodes));
    }

    default int updateStatusBatch(Map<String, Object> params) {
        UserAuth auth = new UserAuth();
        auth.setAuthStatus((Integer) params.get("authStatus"));
        return updateByQuery(auth, QueryWrapper.create()
                .where("user_id = ?", params.get("userId"))
                .where("app_id = ?", params.get("appId")));
    }
}
