package com.hioas.demo.mapper;

import com.hioas.demo.entity.PaymentStrategy;
import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PaymentStrategyMapper extends BaseMapper<PaymentStrategy> {

    default List<PaymentStrategy> selectPublishedByAppId(Long appId) {
        return selectListByQuery(QueryWrapper.create().where("app_id = ?", appId).where("status = 1"));
    }
}
