package com.wealthpro.productcatalog.service;

import com.wealthpro.productcatalog.dto.request.ResearchNoteRequest;
import com.wealthpro.productcatalog.dto.response.ResearchNoteResponse;
import com.wealthpro.productcatalog.entity.ResearchNote;
import com.wealthpro.productcatalog.entity.Security;
import com.wealthpro.productcatalog.enums.ResearchRating;
import com.wealthpro.productcatalog.exception.ResourceNotFoundException;
import com.wealthpro.productcatalog.repository.ResearchNoteRepository;
import com.wealthpro.productcatalog.repository.SecurityRepository;
import com.wealthpro.productcatalog.service.ResearchNoteService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ResearchNoteServiceImpl implements ResearchNoteService {

    private final ResearchNoteRepository researchNoteRepository;
    private final SecurityRepository securityRepository;

    public ResearchNoteServiceImpl(ResearchNoteRepository researchNoteRepository,
                                   SecurityRepository securityRepository) {
        this.researchNoteRepository = researchNoteRepository;
        this.securityRepository = securityRepository;
    }

    private Security fetchSecurity(Long securityId) {
        return securityRepository.findById(securityId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Security not found with id: " + securityId));
    }

    private ResearchNoteResponse toResponse(ResearchNote note) {
        ResearchNoteResponse response = new ResearchNoteResponse();
        response.setNoteId(note.getNoteId());
        response.setSecurityId(note.getSecurity().getSecurityId());
        response.setSecuritySymbol(note.getSecurity().getSymbol());
        response.setTitle(note.getTitle());
        response.setRating(note.getRating());
        response.setPublishedDate(note.getPublishedDate());
        response.setContentUri(note.getContentUri());
        response.setAnalyst(note.getAnalyst());
        response.setContent(note.getContent());
        return response;
    }

    @Override
    public ResearchNoteResponse createResearchNote(ResearchNoteRequest request) {
        Security security = fetchSecurity(request.getSecurityId());
        ResearchNote note = new ResearchNote();
        note.setSecurity(security);
        note.setTitle(request.getTitle());
        note.setRating(request.getRating());
        note.setPublishedDate(request.getPublishedDate());
        note.setContentUri(request.getContentUri());
        note.setAnalyst(request.getAnalyst());
        note.setContent(request.getContent());
        ResearchNote saved = researchNoteRepository.save(note);
        return toResponse(saved);
    }

    @Override
    public ResearchNoteResponse getResearchNoteById(Long noteId) {
        ResearchNote note = researchNoteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ResearchNote not found with id: " + noteId));
        return toResponse(note);
    }

    @Override
    public List<ResearchNoteResponse> getAllResearchNotes() {
        List<ResearchNote> notes = researchNoteRepository.findAll();
        List<ResearchNoteResponse> responses = new ArrayList<>();
        for (ResearchNote note : notes) {
            responses.add(toResponse(note));
        }
        return responses;
    }

    @Override
    public List<ResearchNoteResponse> getResearchNotesBySecurityId(Long securityId) {
        if (!securityRepository.existsById(securityId)) {
            throw new ResourceNotFoundException(
                    "Security not found with id: " + securityId);
        }
        List<ResearchNote> notes = researchNoteRepository.findBySecuritySecurityId(securityId);
        List<ResearchNoteResponse> responses = new ArrayList<>();
        for (ResearchNote note : notes) {
            responses.add(toResponse(note));
        }
        return responses;
    }

    @Override
    public List<ResearchNoteResponse> getResearchNotesByRating(ResearchRating rating) {
        List<ResearchNote> notes = researchNoteRepository.findByRating(rating);
        List<ResearchNoteResponse> responses = new ArrayList<>();
        for (ResearchNote note : notes) {
            responses.add(toResponse(note));
        }
        return responses;
    }

    @Override
    public List<ResearchNoteResponse> getResearchNotesBySecurityIdAndRating(Long securityId, ResearchRating rating) {
        if (!securityRepository.existsById(securityId)) {
            throw new ResourceNotFoundException(
                    "Security not found with id: " + securityId);
        }
        List<ResearchNote> notes = researchNoteRepository.findBySecuritySecurityIdAndRating(securityId, rating);
        List<ResearchNoteResponse> responses = new ArrayList<>();
        for (ResearchNote note : notes) {
            responses.add(toResponse(note));
        }
        return responses;
    }

    @Override
    public List<ResearchNoteResponse> getResearchNotesByDateRange(LocalDate from, LocalDate to) {
        List<ResearchNote> notes = researchNoteRepository.findByPublishedDateBetween(from, to);
        List<ResearchNoteResponse> responses = new ArrayList<>();
        for (ResearchNote note : notes) {
            responses.add(toResponse(note));
        }
        return responses;
    }

    @Override
    public List<ResearchNoteResponse> searchResearchNotesByTitle(String keyword) {
        List<ResearchNote> notes = researchNoteRepository.findByTitleContainingIgnoreCase(keyword);
        List<ResearchNoteResponse> responses = new ArrayList<>();
        for (ResearchNote note : notes) {
            responses.add(toResponse(note));
        }
        return responses;
    }

    @Override
    public ResearchNoteResponse updateResearchNote(Long noteId, ResearchNoteRequest request) {
        ResearchNote existing = researchNoteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ResearchNote not found with id: " + noteId));
        Security security = fetchSecurity(request.getSecurityId());
        existing.setSecurity(security);
        existing.setTitle(request.getTitle());
        existing.setRating(request.getRating());
        existing.setPublishedDate(request.getPublishedDate());
        existing.setContentUri(request.getContentUri());
        existing.setAnalyst(request.getAnalyst());
        existing.setContent(request.getContent());
        ResearchNote updated = researchNoteRepository.save(existing);
        return toResponse(updated);
    }

    @Override
    public void deleteResearchNote(Long noteId) {
        if (!researchNoteRepository.existsById(noteId)) {
            throw new ResourceNotFoundException(
                    "ResearchNote not found with id: " + noteId);
        }
        researchNoteRepository.deleteById(noteId);
    }
}