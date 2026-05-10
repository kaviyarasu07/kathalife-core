package com.kathalife.core.stt.controller;

import com.kathalife.core.stt.dto.SttTranscriptResponse;
import com.kathalife.core.stt.service.SttService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/v1/stt")
public class SttController {

    private final SttService sttService;

    public SttController(SttService sttService) {
        this.sttService = sttService;
    }

    @PostMapping(value = "/transcribe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SttTranscriptResponse> transcribe(
            @RequestPart("audio") MultipartFile audioFile,
            @RequestParam(value = "language", defaultValue = "ta-IN") String languageCode
    ) {
        log.info("STT request received: language={}, fileSize={}",
                languageCode, audioFile.getSize());

        if (audioFile.isEmpty()) {
            throw new IllegalArgumentException("Audio file must not be empty");
        }

        Set<String> supported = Set.of("ta-IN", "hi-IN", "en-IN");
        if (!supported.contains(languageCode)) {
            throw new IllegalArgumentException(
                    "Unsupported language code: " + languageCode +
                            ". Supported: ta-IN, hi-IN, en-IN");
        }

        String transcript = sttService.transcribe(audioFile, languageCode);
        return ResponseEntity.ok(new SttTranscriptResponse(transcript, languageCode));
    }
}
