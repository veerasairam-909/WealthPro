package com.wealthpro.productcatalog.repository;

import com.wealthpro.productcatalog.entity.ResearchNote;
import com.wealthpro.productcatalog.enums.ResearchRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ResearchNoteRepository extends JpaRepository<ResearchNote, Long> {

    List<ResearchNote> findBySecuritySecurityId(Long securityId);

    List<ResearchNote> findByRating(ResearchRating rating);

    List<ResearchNote> findBySecuritySecurityIdAndRating(Long securityId, ResearchRating rating);

    List<ResearchNote> findByPublishedDateBetween(LocalDate from, LocalDate to);

    List<ResearchNote> findByTitleContainingIgnoreCase(String keyword);
}