package com.kathalife.core.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    @Query(value = """
        SELECT * FROM outbox_events
        WHERE published_at IS NULL
        ORDER BY created_at ASC
        LIMIT :batchSize
        FOR UPDATE SKIP LOCKED
        """, nativeQuery = true)
    List<OutboxEvent> findUnpublishedBatch(@Param("batchSize") int batchSize);

    @Modifying
    @Query("UPDATE OutboxEvent o SET o.publishedAt = :publishedAt WHERE o.id = :id")
    void markPublished(@Param("id") UUID id, @Param("publishedAt") LocalDateTime publishedAt);
}
