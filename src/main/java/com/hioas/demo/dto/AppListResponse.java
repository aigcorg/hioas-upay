package com.hioas.demo.dto;

import com.hioas.demo.entity.App;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppListResponse {

    private List<App> apps;
    private Long total;
    private Integer page;
    private Integer size;
    private Integer totalPage;
}
