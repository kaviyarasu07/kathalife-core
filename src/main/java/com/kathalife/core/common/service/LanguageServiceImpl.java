package com.kathalife.core.common.service;

import com.kathalife.core.common.repository.LanguageRepository;
import com.kathalife.core.common.response.LanguageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LanguageServiceImpl implements LanguageService {

    private final LanguageRepository languageRepository;

    @Override
    public List<LanguageResponse> getAllActiveLanguages() {
        return languageRepository
                .findByIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(lang -> new LanguageResponse(
                        lang.getCode(),
                        lang.getName(),
                        lang.getNativeName(),
                        lang.getTtsSupported(),
                        lang.getSttSupported()
                ))
                .toList();
    }

    @Override
    public boolean isValidLanguageCode(String code) {
        if (code == null || code.isBlank()) return false;
        return languageRepository.existsByCode(code);
    }
}
