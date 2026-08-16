package com.hioas.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAppResponse {

    private Long appId;
    private String appid;
    private String signSecretKey;
    private Integer status;

    /**
     * Jackson 会把 getAppId() 与 getAppid() 的 bean 属性视为同一属性(仅大小写不同)并互相覆盖,
     * 这里用 @JsonProperty 显式区分, 保证 appId 与 appid 都能序列化进响应。
     */
    @JsonProperty("appId")
    public Long getAppId() {
        return appId;
    }

    @JsonProperty("appid")
    public String getAppid() {
        return appid;
    }
}