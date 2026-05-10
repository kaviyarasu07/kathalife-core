package com.kathalife.core.common.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "sarvam")
public record SarvamProperties(
        @NotNull Api api,
        @NotNull Stt stt
) {
    public record Api(
            @NotBlank String key,
            @NotBlank String baseUrl
    ) {}

    public record Stt(
            @NotBlank String endpoint,
            @NotBlank String model
    ) {}
}
