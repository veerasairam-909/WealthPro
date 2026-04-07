package com.wealthpro.productcatalog.service;

import com.wealthpro.productcatalog.dto.request.ResearchNoteRequest;
import com.wealthpro.productcatalog.dto.response.ResearchNoteResponse;
import com.wealthpro.productcatalog.entity.ResearchNote;
import com.wealthpro.productcatalog.entity.Security;
import com.wealthpro.productcatalog.enums.AssetClass;
import com.wealthpro.productcatalog.enums.ResearchRating;
import com.wealthpro.productcatalog.enums.SecurityStatus;
import com.wealthpro.productcatalog.exception.ResourceNotFoundException;
import com.wealthpro.productcatalog.repository.ResearchNoteRepository;
import com.wealthpro.productcatalog.repository.SecurityRepository;
import com.wealthpro.productcatalog.service.ResearchNoteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResearchNoteServiceTest {

    @Mock
    private ResearchNoteRepository researchNoteRepository;

    @Mock
    private SecurityRepository securityRepository;

    @InjectMocks
    private ResearchNoteServiceImpl researchNoteService;

    private Security security;
    private ResearchNote researchNote;
    private ResearchNoteRequest request;

    @BeforeEach
    void setUp() {
        security = new Security();
        security.setSecurityId(1L);
        security.setSymbol("AAPL");
        security.setAssetClass(AssetClass.EQUITY);
        security.setCurrency("USD");
        security.setCountry("USA");
        security.setStatus(SecurityStatus.ACTIVE);

        researchNote = new ResearchNote();
        researchNote.setNoteId(1L);
        researchNote.setSecurity(security);
        researchNote.setTitle("Apple Strong Buy");
        researchNote.setRating(ResearchRating.BUY);
        researchNote.setPublishedDate(LocalDate.of(2024, 1, 15));
        researchNote.setContentUri("https://research.example.com/aapl");

        request = new ResearchNoteRequest();
        request.setSecurityId(1L);
        request.setTitle("Apple Strong Buy");
        request.setRating(ResearchRating.BUY);
        request.setPublishedDate(LocalDate.of(2024, 1, 15));
        request.setContentUri("https://research.example.com/aapl");
    }

    @Test
    void shouldCreateResearchNoteSuccessfully() {
        when(securityRepository.findById(1L)).thenReturn(Optional.of(security));
        when(researchNoteRepository.save(any(ResearchNote.class))).thenReturn(researchNote);

        ResearchNoteResponse result = researchNoteService.createResearchNote(request);

        assertNotNull(result);
        assertEquals(1L, result.getNoteId());
        assertEquals("Apple Strong Buy", result.getTitle());
        assertEquals(ResearchRating.BUY, result.getRating());
        assertEquals("AAPL", result.getSecuritySymbol());
        verify(researchNoteRepository).save(any(ResearchNote.class));
    }

    @Test
    void shouldThrowNotFoundWhenSecurityNotFoundOnCreate() {
        when(securityRepository.findById(99L)).thenReturn(Optional.empty());
        request.setSecurityId(99L);

        assertThrows(ResourceNotFoundException.class,
                () -> researchNoteService.createResearchNote(request));

        verify(researchNoteRepository, never()).save(any());
    }

    @Test
    void shouldGetResearchNoteByIdSuccessfully() {
        when(researchNoteRepository.findById(1L)).thenReturn(Optional.of(researchNote));

        ResearchNoteResponse result = researchNoteService.getResearchNoteById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getNoteId());
        assertEquals("Apple Strong Buy", result.getTitle());
    }

    @Test
    void shouldThrowNotFoundWhenNoteIdNotFound() {
        when(researchNoteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> researchNoteService.getResearchNoteById(99L));
    }

    @Test
    void shouldGetAllResearchNotes() {
        when(researchNoteRepository.findAll()).thenReturn(List.of(researchNote));

        List<ResearchNoteResponse> result = researchNoteService.getAllResearchNotes();

        assertEquals(1, result.size());
        assertEquals("Apple Strong Buy", result.get(0).getTitle());
    }

    @Test
    void shouldGetResearchNotesBySecurityId() {
        when(securityRepository.existsById(1L)).thenReturn(true);
        when(researchNoteRepository.findBySecuritySecurityId(1L)).thenReturn(List.of(researchNote));

        List<ResearchNoteResponse> result = researchNoteService.getResearchNotesBySecurityId(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getSecurityId());
    }

    @Test
    void shouldThrowNotFoundWhenSecurityNotFoundOnGetNotesBySecurityId() {
        when(securityRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> researchNoteService.getResearchNotesBySecurityId(99L));
    }

    @Test
    void shouldGetResearchNotesByRating() {
        when(researchNoteRepository.findByRating(ResearchRating.BUY)).thenReturn(List.of(researchNote));

        List<ResearchNoteResponse> result = researchNoteService.getResearchNotesByRating(ResearchRating.BUY);

        assertEquals(1, result.size());
        assertEquals(ResearchRating.BUY, result.get(0).getRating());
    }

    @Test
    void shouldGetResearchNotesBySecurityIdAndRating() {
        when(securityRepository.existsById(1L)).thenReturn(true);
        when(researchNoteRepository.findBySecuritySecurityIdAndRating(1L, ResearchRating.BUY))
                .thenReturn(List.of(researchNote));

        List<ResearchNoteResponse> result =
                researchNoteService.getResearchNotesBySecurityIdAndRating(1L, ResearchRating.BUY);

        assertEquals(1, result.size());
    }

    @Test
    void shouldSearchResearchNotesByTitle() {
        when(researchNoteRepository.findByTitleContainingIgnoreCase("apple"))
                .thenReturn(List.of(researchNote));

        List<ResearchNoteResponse> result = researchNoteService.searchResearchNotesByTitle("apple");

        assertEquals(1, result.size());
        assertTrue(result.get(0).getTitle().toLowerCase().contains("apple"));
    }

    @Test
    void shouldGetResearchNotesByDateRange() {
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 3, 31);

        when(researchNoteRepository.findByPublishedDateBetween(from, to))
                .thenReturn(List.of(researchNote));

        List<ResearchNoteResponse> result =
                researchNoteService.getResearchNotesByDateRange(from, to);

        assertEquals(1, result.size());
    }

    @Test
    void shouldUpdateResearchNoteSuccessfully() {
        when(researchNoteRepository.findById(1L)).thenReturn(Optional.of(researchNote));
        when(securityRepository.findById(1L)).thenReturn(Optional.of(security));
        when(researchNoteRepository.save(any(ResearchNote.class))).thenReturn(researchNote);

        ResearchNoteResponse result = researchNoteService.updateResearchNote(1L, request);

        assertNotNull(result);
        verify(researchNoteRepository).save(any(ResearchNote.class));
    }

    @Test
    void shouldThrowNotFoundWhenUpdatingNonExistentNote() {
        when(researchNoteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> researchNoteService.updateResearchNote(99L, request));
    }

    @Test
    void shouldDeleteResearchNoteSuccessfully() {
        when(researchNoteRepository.existsById(1L)).thenReturn(true);

        researchNoteService.deleteResearchNote(1L);

        verify(researchNoteRepository).deleteById(1L);
    }

    @Test
    void shouldThrowNotFoundWhenDeletingNonExistentNote() {
        when(researchNoteRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> researchNoteService.deleteResearchNote(99L));

        verify(researchNoteRepository, never()).deleteById(any());
    }
}