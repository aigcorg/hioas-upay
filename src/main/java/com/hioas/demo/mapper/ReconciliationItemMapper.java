package com.hioas.demo.mapper;

import com.hioas.demo.entity.ReconciliationItem;
import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ReconciliationItemMapper extends BaseMapper<ReconciliationItem> {

    default List<ReconciliationItem> selectByTaskId(Long taskId) {
        return selectListByQuery(QueryWrapper.create().where("task_id = ?", taskId));
    }

    default List<ReconciliationItem> selectByTaskIdAndDiff(Long taskId) {
        return selectListByQuery(QueryWrapper.create()
                .where("task_id = ?", taskId)
                .where("match_result != 0"));
    }
}
