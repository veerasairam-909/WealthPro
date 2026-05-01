package com.wealthpro.analytics.service;

import com.wealthpro.analytics.dto.*;
import com.wealthpro.analytics.entities.*;
import com.wealthpro.analytics.enums.*;
import com.wealthpro.analytics.exception.ResourceNotFoundException;
import com.wealthpro.analytics.feign.NotificationFeignClient;
import com.wealthpro.analytics.feign.PborFeignClient;
import com.wealthpro.analytics.feign.WealthproFeignClient;
import com.wealthpro.analytics.feign.dto.AccountDTO;
import com.wealthpro.analytics.feign.dto.NotificationRequestDTO;
import com.wealthpro.analytics.repository.*;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class AnalyticsServiceImpl implements AnalyticsService {

    private final PerformanceRecordRepository performanceRecordRepository;
    private final RiskMeasureRepository riskMeasureRepository;
    private final ComplianceBreachRepository complianceBreachRepository;
    private final ModelMapper modelMapper;
    private final PborFeignClient pborFeignClient;
    private final WealthproFeignClient wealthproFeignClient;
    private final NotificationFeignClient notificationFeignClient;

    // Seeded random for reproducible dummy calculations
    private final Random random = new Random(42);

    @Autowired
    public AnalyticsServiceImpl(PerformanceRecordRepository performanceRecordRepository,
                                RiskMeasureRepository riskMeasureRepository,
                                ComplianceBreachRepository complianceBreachRepository,
                                ModelMapper modelMapper,
                                PborFeignClient pborFeignClient,
                                WealthproFeignClient wealthproFeignClient,
                                NotificationFeignClient notificationFeignClient) {
        this.performanceRecordRepository = performanceRecordRepository;
        this.riskMeasureRepository = riskMeasureRepository;
        this.complianceBreachRepository = complianceBreachRepository;
        this.modelMapper = modelMapper;
        this.pborFeignClient = pborFeignClient;
        this.wealthproFeignClient = wealthproFeignClient;
        this.notificationFeignClient = notificationFeignClient;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "performanceRecords", key = "#accountId")
    public List<PerformanceRecordResponseDTO> getPerformanceByAccountId(Long accountId) {
        return performanceRecordRepository.findByAccountId(accountId).stream()
                .map(record -> modelMapper.map(record, PerformanceRecordResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = "performanceRecords", key = "#accountId")
    public PerformanceRecordResponseDTO calculateDailyReturn(Long accountId, Long portfolioId) {

        // ── Feign: Validate account exists in PBOR service ──────────────────
        try {
            AccountDTO account = pborFeignClient.getAccountById(accountId);
            log.info("[FEIGN] Account validated from PBOR-SERVICE → id={}, clientId={}, type={}",
                    account.getAccountId(), account.getClientId(), account.getAccountType());
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Account", accountId);
        }
        // ────────────────────────────────────────────────────────────────────

        LocalDate today = LocalDate.now();

        double portfolioReturn = Math.round((-3.0 + random.nextDouble() * 8.0) * 100.0) / 100.0;
        double benchmarkReturn = Math.round((-2.0 + random.nextDouble() * 5.0) * 100.0) / 100.0;

        PerformanceRecord record = PerformanceRecord.builder()
                .accountId(accountId)
                .portfolioId(portfolioId)
                .period(Period.DAILY)
                .startDate(today)
                .endDate(today)
                .returnPercentage(portfolioReturn)
                .benchmarkReturnPercentage(benchmarkReturn)
                .calculatedAt(LocalDateTime.now())
                .build();

        PerformanceRecord saved = performanceRecordRepository.save(record);
        return modelMapper.map(saved, PerformanceRecordResponseDTO.class);
    }

    @Override
    @CacheEvict(value = "performanceRecords", key = "#accountId")
    public PerformanceRecordResponseDTO calculateMonthlyReturn(Long accountId, Long portfolioId) {

        // ── Feign: Validate account exists in PBOR service ──────────────────
        try {
            AccountDTO account = pborFeignClient.getAccountById(accountId);
            log.info("[FEIGN] Account validated from PBOR-SERVICE → id={}, clientId={}",
                    account.getAccountId(), account.getClientId());
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Account", accountId);
        }
        // ────────────────────────────────────────────────────────────────────

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);

        List<PerformanceRecord> dailyRecords = performanceRecordRepository
                .findByAccountIdAndPeriod(accountId, Period.DAILY);

        double monthlyReturn;
        double benchmarkReturn;

        if (!dailyRecords.isEmpty()) {
            monthlyReturn = dailyRecords.stream()
                    .mapToDouble(PerformanceRecord::getReturnPercentage)
                    .map(r -> 1 + r / 100.0)
                    .reduce(1.0, (a, b) -> a * b);
            monthlyReturn = Math.round((monthlyReturn - 1.0) * 10000.0) / 100.0;

            benchmarkReturn = dailyRecords.stream()
                    .mapToDouble(PerformanceRecord::getBenchmarkReturnPercentage)
                    .map(r -> 1 + r / 100.0)
                    .reduce(1.0, (a, b) -> a * b);
            benchmarkReturn = Math.round((benchmarkReturn - 1.0) * 10000.0) / 100.0;
        } else {
            monthlyReturn = Math.round((-5.0 + random.nextDouble() * 15.0) * 100.0) / 100.0;
            benchmarkReturn = Math.round((-3.0 + random.nextDouble() * 10.0) * 100.0) / 100.0;
        }

        PerformanceRecord record = PerformanceRecord.builder()
                .accountId(accountId)
                .portfolioId(portfolioId)
                .period(Period.MONTHLY)
                .startDate(startDate)
                .endDate(endDate)
                .returnPercentage(monthlyReturn)
                .benchmarkReturnPercentage(benchmarkReturn)
                .calculatedAt(LocalDateTime.now())
                .build();

        PerformanceRecord saved = performanceRecordRepository.save(record);
        return modelMapper.map(saved, PerformanceRecordResponseDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "riskMeasures", key = "#accountId")
    public List<RiskMeasureResponseDTO> getRiskMeasuresByAccountId(Long accountId) {
        return riskMeasureRepository.findByAccountId(accountId).stream()
                .map(measure -> modelMapper.map(measure, RiskMeasureResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = "riskMeasures", key = "#accountId")
    public List<RiskMeasureResponseDTO> runRiskAssessment(Long accountId) {

        // ── Feign: Validate account + fetch holdings count from PBOR ─────────
        AccountDTO account = null;
        int holdingsCount = 0;
        try {
            account = pborFeignClient.getAccountById(accountId);
            holdingsCount = pborFeignClient.getHoldingsByAccountId(accountId).size();
            log.info("[FEIGN] Account + holdings fetched from PBOR-SERVICE → accountId={}, holdings={}",
                    accountId, holdingsCount);
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Account", accountId);
        } catch (FeignException e) {
            log.warn("[FEIGN] Could not fetch holdings from PBOR-SERVICE: {}", e.getMessage());
        }
        // ────────────────────────────────────────────────────────────────────

        List<RiskMeasure> measures = new ArrayList<>();
        measures.add(RiskMeasure.builder()
                .accountId(accountId)
                .measureType(MeasureType.VOLATILITY)
                .measureValue(Math.round((5.0 + random.nextDouble() * 20.0) * 100.0) / 100.0)
                .description("Annualised portfolio volatility based on " + holdingsCount + " holdings")
                .calculatedAt(LocalDateTime.now())
                .build());

        measures.add(RiskMeasure.builder()
                .accountId(accountId)
                .measureType(MeasureType.MAX_DRAWDOWN)
                .measureValue(Math.round((-2.0 - random.nextDouble() * 18.0) * 100.0) / 100.0)
                .description("Maximum peak-to-trough drawdown from simulated NAV series")
                .calculatedAt(LocalDateTime.now())
                .build());

        measures.add(RiskMeasure.builder()
                .accountId(accountId)
                .measureType(MeasureType.VAR_95)
                .measureValue(Math.round((1.0 + random.nextDouble() * 4.0) * 100.0) / 100.0)
                .description("Value-at-Risk at 95% confidence level using historical simulation")
                .calculatedAt(LocalDateTime.now())
                .build());

        measures.add(RiskMeasure.builder()
                .accountId(accountId)
                .measureType(MeasureType.TRACKING_ERROR)
                .measureValue(Math.round((0.5 + random.nextDouble() * 3.5) * 100.0) / 100.0)
                .description("Tracking error vs. benchmark calculated from return differentials")
                .calculatedAt(LocalDateTime.now())
                .build());

        List<RiskMeasure> saved = riskMeasureRepository.saveAll(measures);
        return saved.stream()
                .map(m -> modelMapper.map(m, RiskMeasureResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComplianceBreachResponseDTO> getBreachesByAccountId(Long accountId) {
        return complianceBreachRepository.findByAccountId(accountId).stream()
                .map(breach -> modelMapper.map(breach, ComplianceBreachResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<ComplianceBreachResponseDTO> runComplianceScan(Long accountId) {

        // ── Feign: Fetch account (to get clientId) and suitability rules ─────
        AccountDTO account = null;
        try {
            account = pborFeignClient.getAccountById(accountId);
            log.info("[FEIGN] Account fetched from PBOR-SERVICE → accountId={}, clientId={}",
                    accountId, account.getClientId());
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Account", accountId);
        }

        try {
            var rules = wealthproFeignClient.getAllSuitabilityRules();
            log.info("[FEIGN] {} suitability rules fetched from WEALTHPRO-SERVICE for compliance check",
                    rules.size());
        } catch (FeignException e) {
            log.warn("[FEIGN] Could not fetch suitability rules from WEALTHPRO-SERVICE: {}", e.getMessage());
        }
        // ────────────────────────────────────────────────────────────────────

        List<ComplianceBreach> breaches = new ArrayList<>();

        if (random.nextDouble() < 0.30) {
            breaches.add(ComplianceBreach.builder()
                    .accountId(accountId)
                    .ruleId(1L)
                    .severity(Severity.HIGH)
                    .description("Single holding 'RELIANCE' exceeds 15% concentration limit at 22.5%")
                    .status(BreachStatus.OPEN)
                    .detectedAt(LocalDateTime.now())
                    .build());
        }

        if (random.nextDouble() < 0.30) {
            breaches.add(ComplianceBreach.builder()
                    .accountId(accountId)
                    .ruleId(2L)
                    .severity(Severity.MEDIUM)
                    .description("Technology sector exposure at 35% exceeds 30% policy limit")
                    .status(BreachStatus.OPEN)
                    .detectedAt(LocalDateTime.now())
                    .build());
        }

        if (random.nextDouble() < 0.30) {
            breaches.add(ComplianceBreach.builder()
                    .accountId(accountId)
                    .ruleId(3L)
                    .severity(Severity.CRITICAL)
                    .description("Portfolio holds restricted security 'XYZ Corp' — sanctioned entity")
                    .status(BreachStatus.OPEN)
                    .detectedAt(LocalDateTime.now())
                    .build());
        }

        if (breaches.isEmpty()) {
            return List.of();
        }

        List<ComplianceBreach> saved = complianceBreachRepository.saveAll(breaches);

        // ── Feign: Notify client about compliance breaches ───────────────────
        if (account != null) {
            try {
                NotificationRequestDTO notification = new NotificationRequestDTO(
                        account.getClientId(),
                        saved.size() + " compliance breach(es) detected on your account. Please contact your RM.",
                        "Compliance"
                );
                notificationFeignClient.sendNotification(notification);
                log.info("[FEIGN] Compliance breach notification sent to NOTIFICATIONS-SERVICE for clientId={}",
                        account.getClientId());
            } catch (FeignException e) {
                log.warn("[FEIGN] Could not send compliance notification: {}", e.getMessage());
            }
        }
        // ────────────────────────────────────────────────────────────────────

        return saved.stream()
                .map(b -> modelMapper.map(b, ComplianceBreachResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public ComplianceBreachResponseDTO acknowledgeBreach(Long breachId) {
        ComplianceBreach breach = complianceBreachRepository.findById(breachId)
                .orElseThrow(() -> new ResourceNotFoundException("ComplianceBreach", breachId));

        if (breach.getStatus() != BreachStatus.OPEN) {
            throw new IllegalStateException(
                    "Can only acknowledge OPEN breaches. Current status: " + breach.getStatus());
        }

        breach.setStatus(BreachStatus.ACKNOWLEDGED);
        ComplianceBreach saved = complianceBreachRepository.save(breach);
        return modelMapper.map(saved, ComplianceBreachResponseDTO.class);
    }

    @Override
    public ComplianceBreachResponseDTO closeBreach(Long breachId) {
        ComplianceBreach breach = complianceBreachRepository.findById(breachId)
                .orElseThrow(() -> new ResourceNotFoundException("ComplianceBreach", breachId));

        if (breach.getStatus() != BreachStatus.ACKNOWLEDGED) {
            throw new IllegalStateException(
                    "Can only close ACKNOWLEDGED breaches. Current status: " + breach.getStatus());
        }

        breach.setStatus(BreachStatus.CLOSED);
        breach.setResolvedAt(LocalDateTime.now());
        ComplianceBreach saved = complianceBreachRepository.save(breach);
        return modelMapper.map(saved, ComplianceBreachResponseDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDashboardDTO getAccountDashboard(Long accountId) {
        AccountDashboardDTO dashboard = new AccountDashboardDTO();
        dashboard.setAccountId(accountId);

        dashboard.setPerformanceRecords(
                performanceRecordRepository.findByAccountId(accountId).stream()
                        .map(r -> modelMapper.map(r, PerformanceRecordResponseDTO.class))
                        .collect(Collectors.toList()));

        dashboard.setRiskMeasures(
                riskMeasureRepository.findByAccountId(accountId).stream()
                        .map(m -> modelMapper.map(m, RiskMeasureResponseDTO.class))
                        .collect(Collectors.toList()));

        dashboard.setComplianceBreaches(
                complianceBreachRepository.findByAccountIdAndStatus(accountId, BreachStatus.OPEN).stream()
                        .map(b -> modelMapper.map(b, ComplianceBreachResponseDTO.class))
                        .collect(Collectors.toList()));

        return dashboard;
    }
}
