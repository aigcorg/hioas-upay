package com.hioas.demo.dto;

import com.hioas.demo.entity.Merchant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MerchantListResponse {

    private List<Merchant> merchants;
    private Long total;
    private Integer page;
    private Integer size;
    private Integer totalPage;
}
