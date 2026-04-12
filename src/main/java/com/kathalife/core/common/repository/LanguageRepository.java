package com.kathalife.core.common.repository;

import com.kathalife.core.common.entity.Language;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LanguageRepository extends JpaRepository<Language, UUID> {

    List<Language> findByIsActiveTrueOrderByDisplayOrderAsc();

    Optional<Language> findByCode(String code);

    boolean existsByCode(String code);
}
