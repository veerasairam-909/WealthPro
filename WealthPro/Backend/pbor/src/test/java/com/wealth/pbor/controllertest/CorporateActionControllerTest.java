package com.wealth.pbor.controllertest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wealth.pbor.controller.CorporateActionController;
import com.wealth.pbor.controller.impl.CorporateActionControllerImpl;
import com.wealth.pbor.dto.request.CorporateActionRequest;
import com.wealth.pbor.dto.response.CorporateActionResponse;
import com.wealth.pbor.enums.CAType;
import com.wealth.pbor.exception.ResourceNotFoundException;
import com.wealth.pbor.service.CorporateActionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CorporateActionControllerImpl.class)
class CorporateActionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CorporateActionService corporateActionService;

    private ObjectMapper objectMapper;
    private CorporateActionRequest request;
    private CorporateActionResponse response;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        request = new CorporateActionRequest();
        request.setSecurityId(101L);
        request.setCaType(CAType.DIVIDEND);
        request.setRecordDate(LocalDate.of(2024, 6, 15));
        request.setExDate(LocalDate.of(2024, 6, 14));
        request.setPayDate(LocalDate.of(2024, 6, 20));
        request.setTermsJson("{\"amount\":5.0}");

        response = new CorporateActionResponse();
        response.setCaId(1L);
        response.setSecurityId(101L);
        response.setCaType(CAType.DIVIDEND);
        response.setRecordDate(LocalDate.of(2024, 6, 15));
        response.setExDate(LocalDate.of(2024, 6, 14));
        response.setPayDate(LocalDate.of(2024, 6, 20));
        response.setTermsJson("{\"amount\":5.0}");
    }

//    @Test
//    void testCreateCorporateAction_Success() throws Exception {
//        when(corporateActionService.createCorporateAction(any(CorporateActionRequest.class))).thenReturn(response);
//
//        mockMvc.perform(post("/api/corporate-actions")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.caId").value(1L))
//                .andExpect(jsonPath("$.caType").value("DIVIDEND"));
//    }

    @Test
    void testCreateCorporateAction_ValidationFails() throws Exception {
        CorporateActionRequest invalidRequest = new CorporateActionRequest();

        mockMvc.perform(post("/api/corporate-actions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetCorporateActionById_Success() throws Exception {
        when(corporateActionService.getCorporateActionById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/corporate-actions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caId").value(1L));
    }

    @Test
    void testGetCorporateActionById_NotFound() throws Exception {
        when(corporateActionService.getCorporateActionById(99L)).thenThrow(new ResourceNotFoundException("Corporate action not found with id: 99"));

        mockMvc.perform(get("/api/corporate-actions/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllCorporateActions() throws Exception {
        when(corporateActionService.getAllCorporateActions()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/corporate-actions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testGetCorporateActionsBySecurityId() throws Exception {
        when(corporateActionService.getCorporateActionsBySecurityId(101L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/corporate-actions/security/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testGetCorporateActionsByCaType() throws Exception {
        when(corporateActionService.getCorporateActionsByCaType(CAType.DIVIDEND)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/corporate-actions/ca-type/DIVIDEND"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testGetCorporateActionsByDateRange() throws Exception {
        when(corporateActionService.getCorporateActionsByRecordDateRange(any(), any())).thenReturn(List.of(response));

        mockMvc.perform(get("/api/corporate-actions/date-range")
                        .param("from", "2024-06-01")
                        .param("to", "2024-06-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

//    @Test
//    void testUpdateCorporateAction_Success() throws Exception {
//        when(corporateActionService.updateCorporateAction(eq(1L), any(CorporateActionRequest.class))).thenReturn(response);
//
//        mockMvc.perform(put("/api/corporate-actions/1")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.caId").value(1L));
//    }

    @Test
    void testDeleteCorporateAction_Success() throws Exception {
        doNothing().when(corporateActionService).deleteCorporateAction(1L);

        mockMvc.perform(delete("/api/corporate-actions/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Corporate action deleted successfully."));
    }
}