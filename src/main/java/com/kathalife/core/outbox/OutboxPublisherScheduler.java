package com.kathalife.core.outbox;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OutboxPublisherScheduler {

    private static final int BATCH_SIZE = 50;
    private static final String TOPIC = "journal.entry.saved";

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxPublisherScheduler(OutboxEventRepository outboxEventRepository,
                                     KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> batch = outboxEventRepository.findUnpublishedBatch(BATCH_SIZE);
        for (OutboxEvent event : batch) {
            try {
                kafkaTemplate.send(TOPIC, event.getAggregateId().toString(), event.getPayload())
                    .get(); // synchronous send within this batch's transaction
                outboxEventRepository.markPublished(event.getId(), LocalDateTime.now());
            } catch (Exception e) {
                log.error("Failed to publish outbox event id={}, will retry next poll",
                          event.getId(), e);
            }
        }
    }
}
