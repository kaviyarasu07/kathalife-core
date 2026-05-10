package com.kathalife.core.stt.service;


import com.kathalife.core.common.config.SarvamProperties;
import com.kathalife.core.stt.MultipartInputStreamFileResource;
import com.kathalife.core.stt.dto.SarvamSttResponse;
import com.kathalife.core.stt.exception.SttTranscriptionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@Primary
public class SarvamSttService implements SttService {

    private final WebClient sarvamWebClient;
    private final com.kathalife.core.common.config.SarvamProperties sarvamProperties;

    public SarvamSttService(WebClient sarvamWebClient, SarvamProperties sarvamProperties) {
        this.sarvamWebClient = sarvamWebClient;
        this.sarvamProperties = sarvamProperties;
    }

    @Override
    public String transcribe(MultipartFile audioFile, String languageCode) {
        log.info("STT transcribe start: language={}, fileSize={} bytes",
                languageCode, audioFile.getSize());
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new MultipartInputStreamFileResource(
                    audioFile.getInputStream(),
                    audioFile.getOriginalFilename()
            ));
            body.add("model", sarvamProperties.stt().model());
            body.add("language_code", languageCode);
            body.add("with_disfluencies", "false");

            SarvamSttResponse sarvamResponse = sarvamWebClient.post()
                    .uri(sarvamProperties.stt().endpoint())
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .bodyValue(body)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .map(errorBody ->
                                            new SttTranscriptionException(
                                                    "Sarvam API error: " + errorBody)))
                    .bodyToMono(SarvamSttResponse.class)
                    .block();

            if (sarvamResponse == null || sarvamResponse.transcript() == null) {
                throw new SttTranscriptionException(
                        "Empty transcript returned from Sarvam");
            }

            log.info("STT transcribe success: transcriptLength={}",
                    sarvamResponse.transcript().length());

            return sarvamResponse.transcript().trim();

        } catch (SttTranscriptionException e) {
            throw e;
        } catch (Exception e) {
            log.error("STT transcription failed unexpectedly", e);
            throw new SttTranscriptionException(
                    "Transcription failed: " + e.getMessage(), e);
        }
    }
}