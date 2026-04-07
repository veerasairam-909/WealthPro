package com.wealthpro.productcatalog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wealthpro.productcatalog.controller.ResearchNoteControllerImpl;
import com.wealthpro.productcatalog.dto.request.ResearchNoteRequest;
import com.wealthpro.productcatalog.dto.response.ResearchNoteResponse;
import com.wealthpro.productcatalog.enums.ResearchRating;
import com.wealthpro.productcatalog.exception.GlobalExceptionHandler;
import com.wealthpro.productcatalog.exception.ResourceNotFoundException;
import com.wealthpro.productcatalog.service.ResearchNoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ResearchNoteControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ResearchNoteService researchNoteService;

    @InjectMocks
    private ResearchNoteControllerImpl researchNoteController;

    private ResearchNoteRequest request;
    private ResearchNoteResponse response;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(researchNoteController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        request = new ResearchNoteRequest();
        request.setSecurityId(1L);
        request.setTitle("Apple Strong Buy");
        request.setRating(ResearchRating.BUY);
        request.setPublishedDate(LocalDate.of(2024, 1, 15));
        request.setContentUri("https://research.example.com/aapl");

        response = new ResearchNoteResponse();
        response.setNoteId(1L);
        response.setSecurityId(1L);
        response.setSecuritySymbol("AAPL");
        response.setTitle("Apple Strong Buy");
        response.setRating(ResearchRating.BUY);
        response.setPublishedDate(LocalDate.of(2024, 1, 15));
        response.setContentUri("https://research.example.com/aapl");
    }

    @Test
    void shouldCreateResearchNoteAndReturn201() throws Exception {
        when(researchNoteService.createResearchNote(any(ResearchNoteRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/research-notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.noteId").value(1))
                .andExpect(jsonPath("$.title").value("Apple Strong Buy"))
                .andExpect(jsonPath("$.rating").value("BUY"))
                .andExpect(jsonPath("$.securitySymbol").value("AAPL"));
    }

    @Test
    void shouldReturn400WhenRequestBodyInvalid() throws Exception {
        ResearchNoteRequest invalid = new ResearchNoteRequest();

        mockMvc.perform(post("/api/research-notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetResearchNoteByIdAndReturn200() throws Exception {
        when(researchNoteService.getResearchNoteById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/research-notes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.noteId").value(1))
                .andExpect(jsonPath("$.title").value("Apple Strong Buy"));
    }

    @Test
    void shouldReturn404WhenNoteIdNotFound() throws Exception {
        when(researchNoteService.getResearchNoteById(99L))
                .thenThrow(new ResourceNotFoundException("ResearchNote not found with id: 99"));

        mockMvc.perform(get("/api/research-notes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldGetAllResearchNotesAndReturn200() throws Exception {
        when(researchNoteService.getAllResearchNotes()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/research-notes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldGetResearchNotesBySecurityId() throws Exception {
        when(researchNoteService.getResearchNotesBySecurityId(1L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/research-notes/security/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldGetResearchNotesByRating() throws Exception {
        when(researchNoteService.getResearchNotesByRating(ResearchRating.BUY))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/research-notes/rating/BUY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].rating").value("BUY"));
    }

    @Test
    void shouldGetResearchNotesBySecurityIdAndRating() throws Exception {
        when(researchNoteService.getResearchNotesBySecurityIdAndRating(1L, ResearchRating.BUY))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/research-notes/security/1/rating/BUY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldSearchResearchNotesByTitle() throws Exception {
        when(researchNoteService.searchResearchNotesByTitle("apple")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/research-notes/search")
                        .param("keyword", "apple"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldGetResearchNotesByDateRange() throws Exception {
        when(researchNoteService.getResearchNotesByDateRange(
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 3, 31)))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/research-notes/date-range")
                        .param("from", "2024-01-01")
                        .param("to", "2024-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldUpdateResearchNoteAndReturn200() throws Exception {
        when(researchNoteService.updateResearchNote(eq(1L), any(ResearchNoteRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/research-notes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.noteId").value(1));
    }

    @Test
    void shouldDeleteResearchNoteAndReturn204() throws Exception {
        doNothing().when(researchNoteService).deleteResearchNote(1L);

        mockMvc.perform(delete("/api/research-notes/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentNote() throws Exception {
        doThrow(new ResourceNotFoundException("ResearchNote not found with id: 99"))
                .when(researchNoteService).deleteResearchNote(99L);

        mockMvc.perform(delete("/api/research-notes/99"))
                .andExpect(status().isNotFound());
    }
}