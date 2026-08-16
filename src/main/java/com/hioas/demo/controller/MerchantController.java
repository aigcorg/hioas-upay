package com.hioas.demo.controller;

import com.hioas.demo.dto.*;
import com.hioas.demo.entity.Merchant;
import com.hioas.demo.service.MerchantService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/merchant")
public class MerchantController {

    private final MerchantService merchantService;

    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @PostMapping("/register")
    public ApiResponse<Merchant> register(@Valid @RequestBody MerchantRegisterRequest request) {
        Merchant merchant = merchantService.register(request);
        return ApiResponse.success(merchant);
    }

    @PostMapping("/{merchantId}/certification")
    public ApiResponse<Merchant> submitCertification(@PathVariable Long merchantId,
                                                       @RequestBody MerchantCertificationRequest request) {
        Merchant merchant = merchantService.submitCertification(merchantId, request);
        return ApiResponse.success(merchant);
    }

    @GetMapping("/{merchantId}")
    public ApiResponse<Merchant> getMerchant(@PathVariable Long merchantId) {
        Merchant merchant = merchantService.getMerchantById(merchantId);
        return ApiResponse.success(merchant);
    }

    @GetMapping
    public ApiResponse<MerchantListResponse> getMerchantList(@RequestParam(required = false) Integer status,
                                                               @RequestParam(required = false) Integer page,
                                                               @RequestParam(required = false) Integer size) {
        List<Merchant> merchants = merchantService.getMerchantList(status, page, size);
        long total = merchants.size();
        int pageSize = size != null ? size : 20;
        int pageNum = page != null ? page : 1;
        return ApiResponse.success(new MerchantListResponse(merchants, total, pageNum, pageSize, 
                (int)Math.ceil(total / (double)pageSize)));
    }
}
