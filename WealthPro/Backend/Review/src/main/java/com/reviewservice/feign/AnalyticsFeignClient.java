package com.reviewservice.feign;

import com.reviewservice.feign.dto.PerformanceRecordDTO;
import com.reviewservice.feign.dto.RiskMeasureDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "analytics-service")
public interface AnalyticsFeignClient {

    @GetMapping("/api/analytics/accounts/{accountId}/performance")
    List<PerformanceRecordDTO> getPerformanceByAccountId(@PathVariable Long accountId);

    @GetMapping("/api/analytics/accounts/{accountId}/risk-measures")
    List<RiskMeasureDTO> getRiskMeasuresByAccountId(@PathVariable Long accountId);

    @GetMapping("/api/analytics/accounts/{accountId}/dashboard")
    Object getAccountDashboard(@PathVariable Long accountId);
}
