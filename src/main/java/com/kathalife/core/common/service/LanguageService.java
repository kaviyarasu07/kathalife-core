package com.kathalife.core.common.service;

import com.kathalife.core.common.response.LanguageResponse;

import java.util.List;

public interface LanguageService {
    List<LanguageResponse> getAllActiveLanguages();
    boolean isValidLanguageCode(String code);
}
