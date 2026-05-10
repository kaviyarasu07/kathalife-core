package com.kathalife.core.stt.service;

import org.springframework.web.multipart.MultipartFile;

public interface SttService {
    String transcribe(MultipartFile audioFile, String languageCode);
}
