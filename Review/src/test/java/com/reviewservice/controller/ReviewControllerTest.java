package com.reviewservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.reviewservice.dto.request.ReviewRequest;
import com.reviewservice.dto.response.ReviewResponse;
import com.reviewservice.enums.PeriodType;
import com.reviewservice.enums.ReviewStatus;
import com.reviewservice.exception.ResourceNotFoundException;
import com.reviewservice.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewControllerImpl.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewService reviewService;

    private ObjectMapper objectMapper;
    private ReviewRequest reviewRequest;
    private ReviewResponse reviewResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        reviewRequest = ReviewRequest.builder()
                .accountId(101L)
                .periodStart(LocalDate.of(2024, 1, 1))
                .periodEnd(LocalDate.of(2024, 3, 31))
                .periodType(PeriodType.QUARTERLY)
                .highlightsJson("{\"growth\": \"5%\"}")
                .reviewedBy("John Advisor")
                .reviewDate(LocalDate.of(2024, 4, 1))
                .status(ReviewStatus.COMPLETED)
                .build();

        reviewResponse = ReviewResponse.builder()
                .reviewId(1L)
                .accountId(101L)
                .periodStart(LocalDate.of(2024, 1, 1))
                .periodEnd(LocalDate.of(2024, 3, 31))
                .periodType(PeriodType.QUARTERLY)
                .highlightsJson("{\"growth\": \"5%\"}")
                .reviewedBy("John Advisor")
                .reviewDate(LocalDate.of(2024, 4, 1))
                .status(ReviewStatus.COMPLETED)
                .build();
    }

    @Test
    @DisplayName("POST /api/reviews - Should create review and return 201")
    void testCreateReview() throws Exception {
        when(reviewService.createReview(any(ReviewRequest.class))).thenReturn(reviewResponse);

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reviewId").value(1L))
                .andExpect(jsonPath("$.accountId").value(101L))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("GET /api/reviews/{id} - Should return review by ID")
    void testGetReviewById() throws Exception {
        when(reviewService.getReviewById(1L)).thenReturn(reviewResponse);

        mockMvc.perform(get("/api/reviews/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId").value(1L))
                .andExpect(jsonPath("$.reviewedBy").value("John Advisor"));
    }

    @Test
    @DisplayName("GET /api/reviews/{id} - Should return 404 when not found")
    void testGetReviewById_NotFound() throws Exception {
        when(reviewService.getReviewById(99L))
                .thenThrow(new ResourceNotFoundException("Review", 99L));

        mockMvc.perform(get("/api/reviews/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/reviews - Should return all reviews")
    void testGetAllReviews() throws Exception {
        when(reviewService.getAllReviews()).thenReturn(List.of(reviewResponse));

        mockMvc.perform(get("/api/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("PUT /api/reviews/{id} - Should update review and return 200")
    void testUpdateReview() throws Exception {
        when(reviewService.updateReview(eq(1L), any(ReviewRequest.class))).thenReturn(reviewResponse);

        mockMvc.perform(put("/api/reviews/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId").value(1L));
    }

    @Test
    @DisplayName("DELETE /api/reviews/{id} - Should delete review and return 204")
    void testDeleteReview() throws Exception {
        doNothing().when(reviewService).deleteReview(1L);

        mockMvc.perform(delete("/api/reviews/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/reviews/{id} - Should return 404 when not found")
    void testDeleteReview_NotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Review", 99L))
                .when(reviewService).deleteReview(99L);

        mockMvc.perform(delete("/api/reviews/99"))
                .andExpect(status().isNotFound());
    }
}
