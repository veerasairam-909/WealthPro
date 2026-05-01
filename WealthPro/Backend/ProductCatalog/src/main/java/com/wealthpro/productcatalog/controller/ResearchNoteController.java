package com.wealthpro.productcatalog.controller;

import com.wealthpro.productcatalog.dto.request.ResearchNoteRequest;
import com.wealthpro.productcatalog.dto.response.ResearchNoteResponse;
import com.wealthpro.productcatalog.enums.ResearchRating;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

public interface ResearchNoteController {

    @PostMapping
    ResponseEntity<ResearchNoteResponse> createResearchNote(@Valid @RequestBody ResearchNoteRequest request);

    @GetMapping("/{noteId}")
    ResponseEntity<ResearchNoteResponse> getResearchNoteById(@PathVariable Long noteId);

    @GetMapping
    ResponseEntity<List<ResearchNoteResponse>> getAllResearchNotes();

    @GetMapping("/security/{securityId}")
    ResponseEntity<List<ResearchNoteResponse>> getResearchNotesBySecurityId(@PathVariable Long securityId);

    @GetMapping("/rating/{rating}")
    ResponseEntity<List<ResearchNoteResponse>> getResearchNotesByRating(@PathVariable ResearchRating rating);

    @GetMapping("/security/{securityId}/rating/{rating}")
    ResponseEntity<List<ResearchNoteResponse>> getResearchNotesBySecurityIdAndRating(
            @PathVariable Long securityId,
            @PathVariable ResearchRating rating);

    @GetMapping("/date-range")
    ResponseEntity<List<ResearchNoteResponse>> getResearchNotesByDateRange(
            @RequestParam LocalDate from,
            @RequestParam LocalDate to);

    @GetMapping("/search")
    ResponseEntity<List<ResearchNoteResponse>> searchResearchNotesByTitle(@RequestParam String keyword);

    @PutMapping("/{noteId}")
    ResponseEntity<ResearchNoteResponse> updateResearchNote(
            @PathVariable Long noteId,
            @Valid @RequestBody ResearchNoteRequest request);

    @DeleteMapping("/{noteId}")
    ResponseEntity deleteResearchNote(@PathVariable Long noteId);
}