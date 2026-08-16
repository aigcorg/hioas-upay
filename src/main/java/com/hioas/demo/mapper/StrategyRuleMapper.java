package com.hioas.demo.mapper;

import com.hioas.demo.entity.StrategyRule;
import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Comparator;
import java.util.List;

@Mapper
public interface StrategyRuleMapper extends BaseMapper<StrategyRule> {

    default List<StrategyRule> selectByStrategyIdOrderByPriorityAsc(Long strategyId) {
        List<StrategyRule> rules = selectListByQuery(QueryWrapper.create().where("strategy_id = ?", strategyId));
        rules.sort(Comparator.comparing(StrategyRule::getPriority, Comparator.nullsLast(Comparator.naturalOrder())));
        return rules;
    }
}
