package com.kathalife.core.journal.entity;

import com.kathalife.core.common.entity.BaseEntity;
import com.kathalife.core.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "journal_activities")
@Getter
@Setter
public class JournalActivity extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "activity_date", nullable = false)
    private LocalDate activityDate;

    @Column(name = "audio_file_path")
    private String audioFilePath;

    @Column(name = "stt_text", columnDefinition = "TEXT")
    private String sttText;

    @Column(name = "stt_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private SttStatus sttStatus;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public enum SttStatus {
        NONE, PENDING, PROCESSING, DONE, FAILED
    }
}
