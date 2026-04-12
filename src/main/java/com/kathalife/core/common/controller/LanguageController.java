package com.kathalife.core.common.controller;

import com.kathalife.core.common.response.ApiResponse;
import com.kathalife.core.common.response.LanguageResponse;
import com.kathalife.core.common.service.LanguageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/languages")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Languages", description = "Supported languages API")
public class LanguageController {

    private final LanguageService languageService;

    @GetMapping
    @Operation(summary = "Get all supported languages")
    public ResponseEntity<ApiResponse<List<LanguageResponse>>> getAllLanguages() {

        List<LanguageResponse> languages = languageService.getAllActiveLanguages();
        return ResponseEntity.ok(ApiResponse.success(languages));
    }
}
