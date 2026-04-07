package com.wealthpro.productcatalog.repository;

import com.wealthpro.productcatalog.entity.ResearchNote;
import com.wealthpro.productcatalog.entity.Security;
import com.wealthpro.productcatalog.enums.AssetClass;
import com.wealthpro.productcatalog.enums.ResearchRating;
import com.wealthpro.productcatalog.enums.SecurityStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ResearchNoteRepositoryTest {

    @Autowired
    private ResearchNoteRepository researchNoteRepository;

    @Autowired
    private SecurityRepository securityRepository;

    private Security security;
    private ResearchNote buyNote;
    private ResearchNote sellNote;

    @BeforeEach
    void setUp() {
        researchNoteRepository.deleteAll();
        securityRepository.deleteAll();

        security = new Security();
        security.setSymbol("AAPL");
        security.setAssetClass(AssetClass.EQUITY);
        security.setCurrency("USD");
        security.setCountry("USA");
        security.setStatus(SecurityStatus.ACTIVE);
        securityRepository.save(security);

        buyNote = new ResearchNote();
        buyNote.setSecurity(security);
        buyNote.setTitle("Apple Q1 Strong Earnings Outlook");
        buyNote.setRating(ResearchRating.BUY);
        buyNote.setPublishedDate(LocalDate.of(2024, 1, 15));
        buyNote.setContentUri("https://research.example.com/aapl-q1");
        researchNoteRepository.save(buyNote);

        sellNote = new ResearchNote();
        sellNote.setSecurity(security);
        sellNote.setTitle("Apple Valuation Concerns Rising");
        sellNote.setRating(ResearchRating.SELL);
        sellNote.setPublishedDate(LocalDate.of(2024, 6, 20));
        sellNote.setContentUri("https://research.example.com/aapl-sell");
        researchNoteRepository.save(sellNote);
    }

    @Test
    void shouldSaveAndFindById() {
        Optional<ResearchNote> found = researchNoteRepository.findById(buyNote.getNoteId());

        assertTrue(found.isPresent());
        assertEquals("Apple Q1 Strong Earnings Outlook", found.get().getTitle());
    }

    @Test
    void shouldFindBySecurityId() {
        List<ResearchNote> notes = researchNoteRepository
                .findBySecuritySecurityId(security.getSecurityId());

        assertEquals(2, notes.size());
    }

    @Test
    void shouldFindByRating() {
        List<ResearchNote> buyNotes = researchNoteRepository.findByRating(ResearchRating.BUY);

        assertEquals(1, buyNotes.size());
        assertEquals("Apple Q1 Strong Earnings Outlook", buyNotes.get(0).getTitle());
    }

    @Test
    void shouldFindBySecurityIdAndRating() {
        List<ResearchNote> notes = researchNoteRepository
                .findBySecuritySecurityIdAndRating(security.getSecurityId(), ResearchRating.SELL);

        assertEquals(1, notes.size());
        assertEquals(ResearchRating.SELL, notes.get(0).getRating());
    }

    @Test
    void shouldFindByPublishedDateBetween() {
        List<ResearchNote> notes = researchNoteRepository.findByPublishedDateBetween(
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 3, 31));

        assertEquals(1, notes.size());
        assertEquals(buyNote.getNoteId(), notes.get(0).getNoteId());
    }

    @Test
    void shouldFindByTitleContainingIgnoreCase() {
        List<ResearchNote> notes = researchNoteRepository
                .findByTitleContainingIgnoreCase("apple");

        assertEquals(2, notes.size());
    }

    @Test
    void shouldFindByTitleContainingIgnoreCasePartialMatch() {
        List<ResearchNote> notes = researchNoteRepository
                .findByTitleContainingIgnoreCase("earnings");

        assertEquals(1, notes.size());
        assertEquals(buyNote.getNoteId(), notes.get(0).getNoteId());
    }

    @Test
    void shouldReturnEmptyWhenNoMatchForRating() {
        List<ResearchNote> holdNotes = researchNoteRepository.findByRating(ResearchRating.HOLD);

        assertTrue(holdNotes.isEmpty());
    }

    @Test
    void shouldFindAllNotes() {
        List<ResearchNote> all = researchNoteRepository.findAll();

        assertEquals(2, all.size());
    }

    @Test
    void shouldDeleteById() {
        researchNoteRepository.deleteById(buyNote.getNoteId());

        assertFalse(researchNoteRepository.existsById(buyNote.getNoteId()));
    }
}