
package com.wealthpro.productcatalog.service;

import com.wealthpro.productcatalog.dto.request.ResearchNoteRequest;
import com.wealthpro.productcatalog.dto.response.ResearchNoteResponse;
import com.wealthpro.productcatalog.enums.ResearchRating;

import java.time.LocalDate;
import java.util.List;

public interface ResearchNoteService {

    ResearchNoteResponse createResearchNote(ResearchNoteRequest request);

    ResearchNoteResponse getResearchNoteById(Long noteId);

    List<ResearchNoteResponse> getAllResearchNotes();

    List<ResearchNoteResponse> getResearchNotesBySecurityId(Long securityId);

    List<ResearchNoteResponse> getResearchNotesByRating(ResearchRating rating);

    List<ResearchNoteResponse> getResearchNotesBySecurityIdAndRating(Long securityId, ResearchRating rating);

    List<ResearchNoteResponse> getResearchNotesByDateRange(LocalDate from, LocalDate to);

    List<ResearchNoteResponse> searchResearchNotesByTitle(String keyword);

    ResearchNoteResponse updateResearchNote(Long noteId, ResearchNoteRequest request);

    void deleteResearchNote(Long noteId);
}
