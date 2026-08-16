package com.hioas.demo.dto;

import lombok.Data;
import java.util.List;

@Data
public class ChannelSelectRequest {

    private List<String> channelCodes;
}
