package com.kathalife.core.user.entity;

import com.kathalife.core.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_life_summaries")
@Getter
@Setter
public class UserLifeSummary extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "summary_text", columnDefinition = "TEXT")
    private String summaryText;
}
