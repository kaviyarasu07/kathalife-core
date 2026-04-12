package com.kathalife.core.user.entity;

import com.kathalife.core.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "bio_profiles")
@Getter
@Setter
public class BioProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "hometown")
    private String hometown;

    @Column(name = "occupation")
    private String occupation;

    @Column(name = "family_notes")
    private String familyNotes;

    @Column(name = "profile_pic_url")
    private String profilePicUrl;
}
