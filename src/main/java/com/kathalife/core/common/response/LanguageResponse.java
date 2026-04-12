package com.kathalife.core.common.response;

public record LanguageResponse(
    String code,
    String name,
    String nativeName,
    Boolean ttsSupported,
    Boolean sttSupported
) {}
