package com.kathalife.core.journal.repository;

import com.kathalife.core.journal.entity.JournalActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JournalActivityRepository extends JpaRepository<JournalActivity, UUID> {
}

