package com.wealthpro.productcatalog.controller;

import com.wealthpro.productcatalog.controller.ResearchNoteController;
import com.wealthpro.productcatalog.dto.request.ResearchNoteRequest;
import com.wealthpro.productcatalog.dto.response.ResearchNoteResponse;
import com.wealthpro.productcatalog.enums.ResearchRating;
import com.wealthpro.productcatalog.service.ResearchNoteService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/research-notes")
public class    ResearchNoteControllerImpl implements ResearchNoteController {

    private final ResearchNoteService researchNoteService;

    public ResearchNoteControllerImpl(ResearchNoteService researchNoteService) {
        this.researchNoteService = researchNoteService;
    }

    @Override
    @PostMapping
    public ResponseEntity<ResearchNoteResponse> createResearchNote(
            @Valid @RequestBody ResearchNoteRequest request) {
        ResearchNoteResponse response = researchNoteService.createResearchNote(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    @GetMapping("/{noteId}")
    public ResponseEntity<ResearchNoteResponse> getResearchNoteById(
            @PathVariable Long noteId) {
        ResearchNoteResponse response = researchNoteService.getResearchNoteById(noteId);
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping
    public ResponseEntity<List<ResearchNoteResponse>> getAllResearchNotes() {
        List<ResearchNoteResponse> responses = researchNoteService.getAllResearchNotes();
        return ResponseEntity.ok(responses);
    }

    @Override
    @GetMapping("/security/{securityId}")
    public ResponseEntity<List<ResearchNoteResponse>> getResearchNotesBySecurityId(
            @PathVariable Long securityId) {
        List<ResearchNoteResponse> responses = researchNoteService.getResearchNotesBySecurityId(securityId);
        return ResponseEntity.ok(responses);
    }

    @Override
    @GetMapping("/rating/{rating}")
    public ResponseEntity<List<ResearchNoteResponse>> getResearchNotesByRating(
            @PathVariable ResearchRating rating) {
        List<ResearchNoteResponse> responses = researchNoteService.getResearchNotesByRating(rating);
        return ResponseEntity.ok(responses);
    }

    @Override
    @GetMapping("/security/{securityId}/rating/{rating}")
    public ResponseEntity<List<ResearchNoteResponse>> getResearchNotesBySecurityIdAndRating(
            @PathVariable Long securityId,
            @PathVariable ResearchRating rating) {
        List<ResearchNoteResponse> responses =
                researchNoteService.getResearchNotesBySecurityIdAndRating(securityId, rating);
        return ResponseEntity.ok(responses);
    }

    @Override
    @GetMapping("/date-range")
    public ResponseEntity<List<ResearchNoteResponse>> getResearchNotesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<ResearchNoteResponse> responses = researchNoteService.getResearchNotesByDateRange(from, to);
        return ResponseEntity.ok(responses);
    }

    @Override
    @GetMapping("/search")
    public ResponseEntity<List<ResearchNoteResponse>> searchResearchNotesByTitle(
            @RequestParam String keyword) {
        List<ResearchNoteResponse> responses = researchNoteService.searchResearchNotesByTitle(keyword);
        return ResponseEntity.ok(responses);
    }

    @Override
    @PutMapping("/{noteId}")
    public ResponseEntity<ResearchNoteResponse> updateResearchNote(
            @PathVariable Long noteId,
            @Valid @RequestBody ResearchNoteRequest request) {
        ResearchNoteResponse response = researchNoteService.updateResearchNote(noteId, request);
        return ResponseEntity.ok(response);
    }

    @Override
    @DeleteMapping("/{noteId}")
    public ResponseEntity deleteResearchNote(@PathVariable Long noteId) {
        researchNoteService.deleteResearchNote(noteId);
        //return ResponseEntity.noContent().build();
        return ResponseEntity.ok("Research note deleted successfully");
    }
}