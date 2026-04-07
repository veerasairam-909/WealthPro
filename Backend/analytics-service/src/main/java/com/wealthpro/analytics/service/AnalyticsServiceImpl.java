package com.wealthpro.analytics.service;

import com.wealthpro.analytics.dto.*;
import com.wealthpro.analytics.entities.*;
import com.wealthpro.analytics.enums.*;
import com.wealthpro.analytics.exception.ResourceNotFoundException;
import com.wealthpro.analytics.repository.*;
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

/**
 * Implementation of {@link AnalyticsService} containing simulated core
 * business logic for:
 * <ul>
 *   <li>Daily and monthly return calculations</li>
 *   <li>Risk assessment (volatility, drawdown, VaR, tracking error)</li>
 *   <li>Compliance breach detection and lifecycle management</li>
 * </ul>
 *
 * <p>All calculations use dummy/simulated data for Phase-1 demonstration.</p>
 *
 * @author WealthPro Team
 * @version 2.0
 */
@Service
@Transactional
public class AnalyticsServiceImpl implements AnalyticsService {

    private final PerformanceRecordRepository performanceRecordRepository;
    private final RiskMeasureRepository riskMeasureRepository;
    private final ComplianceBreachRepository complianceBreachRepository;
    private final ModelMapper modelMapper;

    // Seeded random for reproducible dummy calculations
    private final Random random = new Random(42);

    @Autowired
    public AnalyticsServiceImpl(PerformanceRecordRepository performanceRecordRepository,
                                RiskMeasureRepository riskMeasureRepository,
                                ComplianceBreachRepository complianceBreachRepository,
                                ModelMapper modelMapper) {
        this.performanceRecordRepository = performanceRecordRepository;
        this.riskMeasureRepository = riskMeasureRepository;
        this.complianceBreachRepository = complianceBreachRepository;
        this.modelMapper = modelMapper;
    }


    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "performanceRecords", key = "#accountId")
    public List<PerformanceRecordResponseDTO> getPerformanceByAccountId(Long accountId) {
        // Fetch all records and convert each to DTO using stream + map
        return performanceRecordRepository.findByAccountId(accountId).stream()
                .map(record -> modelMapper.map(record, PerformanceRecordResponseDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Simulates a daily return by generating a random return between -3% and +5%.
     * A benchmark return (e.g. Nifty 50) is also generated for comparison.
     * </p>
     */
    @Override
    @CacheEvict(value = "performanceRecords", key = "#accountId")
    public PerformanceRecordResponseDTO calculateDailyReturn(Long accountId, Long portfolioId) {
        LocalDate today = LocalDate.now();

        // Simulate portfolio daily return: random value between -3.0% and +5.0%
        double portfolioReturn = Math.round((-3.0 + random.nextDouble() * 8.0) * 100.0) / 100.0;

        // Simulate benchmark (e.g. Nifty 50) daily return: random between -2.0% and +3.0%
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

    /**
     * {@inheritDoc}
     * <p>
     * Aggregates existing daily records for the past 30 days using
     * geometric-style compounding (simplified). If no daily records exist,
     * a simulated monthly return is generated.
     * </p>
     */
    @Override
    @CacheEvict(value = "performanceRecords", key = "#accountId")
    public PerformanceRecordResponseDTO calculateMonthlyReturn(Long accountId, Long portfolioId) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);

        // Fetch daily records for this account and calculate aggregate return
        List<PerformanceRecord> dailyRecords = performanceRecordRepository
                .findByAccountIdAndPeriod(accountId, Period.DAILY);

        double monthlyReturn;
        double benchmarkReturn;

        if (!dailyRecords.isEmpty()) {
            // Aggregate daily returns: compounded return = product of (1 + r_i) - 1
            // stream reduces all daily returns into a single compounded figure
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
            // No daily data — generate simulated monthly figures
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

    /**
     * {@inheritDoc}
     * <p>
     * Calculates four risk metrics with dummy/simulated market data:
     * <ul>
     *   <li><b>VOLATILITY</b>: annualised standard deviation (simulated 5%–25%)</li>
     *   <li><b>MAX_DRAWDOWN</b>: peak-to-trough loss (simulated -2% to -20%)</li>
     *   <li><b>VAR_95</b>: Value-at-Risk at 95% confidence (simulated 1%–5%)</li>
     *   <li><b>TRACKING_ERROR</b>: deviation from benchmark (simulated 0.5%–4%)</li>
     * </ul>
     * </p>
     */
    @Override
    @CacheEvict(value = "riskMeasures", key = "#accountId")
    public List<RiskMeasureResponseDTO> runRiskAssessment(Long accountId) {
        List<RiskMeasure> measures = new ArrayList<>();

        // Volatility — annualised standard deviation (simulated 5% to 25%)
        measures.add(RiskMeasure.builder()
                .accountId(accountId)
                .measureType(MeasureType.VOLATILITY)
                .measureValue(Math.round((5.0 + random.nextDouble() * 20.0) * 100.0) / 100.0)
                .description("Annualised portfolio volatility based on simulated daily returns")
                .calculatedAt(LocalDateTime.now())
                .build());

        // Max Drawdown — peak to trough (simulated -2% to -20%)
        measures.add(RiskMeasure.builder()
                .accountId(accountId)
                .measureType(MeasureType.MAX_DRAWDOWN)
                .measureValue(Math.round((-2.0 - random.nextDouble() * 18.0) * 100.0) / 100.0)
                .description("Maximum peak-to-trough drawdown from simulated NAV series")
                .calculatedAt(LocalDateTime.now())
                .build());

        // VaR at 95% confidence (simulated 1% to 5%)
        measures.add(RiskMeasure.builder()
                .accountId(accountId)
                .measureType(MeasureType.VAR_95)
                .measureValue(Math.round((1.0 + random.nextDouble() * 4.0) * 100.0) / 100.0)
                .description("Value-at-Risk at 95% confidence level using historical simulation")
                .calculatedAt(LocalDateTime.now())
                .build());

        // Tracking Error (simulated 0.5% to 4%)
        measures.add(RiskMeasure.builder()
                .accountId(accountId)
                .measureType(MeasureType.TRACKING_ERROR)
                .measureValue(Math.round((0.5 + random.nextDouble() * 3.5) * 100.0) / 100.0)
                .description("Tracking error vs. benchmark calculated from return differentials")
                .calculatedAt(LocalDateTime.now())
                .build());

        // Persist all measures and map to DTOs
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

    /**
     * {@inheritDoc}
     * <p>
     * Scans for three types of violations using simulated portfolio data:
     * <ul>
     *   <li><b>CONCENTRATION</b>: single holding &gt; 15% of portfolio</li>
     *   <li><b>EXPOSURE_LIMIT</b>: sector exposure &gt; 30%</li>
     *   <li><b>RESTRICTED_SECURITY</b>: holding a sanctioned/restricted security</li>
     * </ul>
     * Uses random chance (30%) to simulate whether each violation is detected.
     * </p>
     */
    @Override
    public List<ComplianceBreachResponseDTO> runComplianceScan(Long accountId) {
        List<ComplianceBreach> breaches = new ArrayList<>();

        // Simulate concentration breach (30% chance of detection)
        if (random.nextDouble() < 0.30) {
            breaches.add(ComplianceBreach.builder()
                    .accountId(accountId)
                    .ruleViolated("CONCENTRATION_LIMIT")
                    .severity(Severity.HIGH)
                    .description("Single holding 'RELIANCE' exceeds 15% concentration limit at 22.5%")
                    .status(BreachStatus.OPEN)
                    .detectedAt(LocalDateTime.now())
                    .build());
        }

        // Simulate exposure limit breach (30% chance)
        if (random.nextDouble() < 0.30) {
            breaches.add(ComplianceBreach.builder()
                    .accountId(accountId)
                    .ruleViolated("SECTOR_EXPOSURE_LIMIT")
                    .severity(Severity.MEDIUM)
                    .description("Technology sector exposure at 35% exceeds 30% policy limit")
                    .status(BreachStatus.OPEN)
                    .detectedAt(LocalDateTime.now())
                    .build());
        }

        // Simulate restricted security breach (30% chance)
        if (random.nextDouble() < 0.30) {
            breaches.add(ComplianceBreach.builder()
                    .accountId(accountId)
                    .ruleViolated("RESTRICTED_SECURITY")
                    .severity(Severity.CRITICAL)
                    .description("Portfolio holds restricted security 'XYZ Corp' — sanctioned entity")
                    .status(BreachStatus.OPEN)
                    .detectedAt(LocalDateTime.now())
                    .build());
        }

        if (breaches.isEmpty()) {
            // No violations detected — return empty list
            return List.of();
        }

        List<ComplianceBreach> saved = complianceBreachRepository.saveAll(breaches);
        return saved.stream()
                .map(b -> modelMapper.map(b, ComplianceBreachResponseDTO.class))
                .collect(Collectors.toList());
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
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


    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public AccountDashboardDTO getAccountDashboard(Long accountId) {
        AccountDashboardDTO dashboard = new AccountDashboardDTO();
        dashboard.setAccountId(accountId);

        // Aggregate performance records
        dashboard.setPerformanceRecords(
                performanceRecordRepository.findByAccountId(accountId).stream()
                        .map(r -> modelMapper.map(r, PerformanceRecordResponseDTO.class))
                        .collect(Collectors.toList()));

        // Aggregate risk measures
        dashboard.setRiskMeasures(
                riskMeasureRepository.findByAccountId(accountId).stream()
                        .map(m -> modelMapper.map(m, RiskMeasureResponseDTO.class))
                        .collect(Collectors.toList()));

        // Show only OPEN breaches on the dashboard
        dashboard.setComplianceBreaches(
                complianceBreachRepository.findByAccountIdAndStatus(accountId, BreachStatus.OPEN).stream()
                        .map(b -> modelMapper.map(b, ComplianceBreachResponseDTO.class))
                        .collect(Collectors.toList()));

        return dashboard;
    }
}
