package com.hioas.demo.mapper;

import com.hioas.demo.entity.Merchant;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MerchantMapper extends BaseMapper<Merchant> {
}
