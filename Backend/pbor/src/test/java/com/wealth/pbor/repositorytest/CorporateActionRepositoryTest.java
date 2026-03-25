package com.wealth.pbor.repositorytest;

import com.wealth.pbor.entity.CorporateAction;
import com.wealth.pbor.enums.CAType;
import com.wealth.pbor.repository.CorporateActionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class CorporateActionRepositoryTest {

    @Autowired
    private CorporateActionRepository corporateActionRepository;

    private CorporateAction corporateAction;

    @BeforeEach
    void setUp() {
        corporateActionRepository.deleteAll();

        corporateAction = new CorporateAction();
        corporateAction.setSecurityId(101L);
        corporateAction.setCaType(CAType.DIVIDEND);
        corporateAction.setRecordDate(LocalDate.of(2024, 6, 15));
        corporateAction.setExDate(LocalDate.of(2024, 6, 14));
        corporateAction.setPayDate(LocalDate.of(2024, 6, 20));
        corporateAction.setTermsJson("{\"amount\":5.0}");
        corporateActionRepository.save(corporateAction);
    }

    @Test
    void testSaveCorporateAction() {
        CorporateAction newAction = new CorporateAction();
        newAction.setSecurityId(102L);
        newAction.setCaType(CAType.BONUS);
        newAction.setRecordDate(LocalDate.of(2024, 7, 15));
        newAction.setExDate(LocalDate.of(2024, 7, 14));
        newAction.setPayDate(LocalDate.of(2024, 7, 20));
        newAction.setTermsJson("{\"ratio\":\"1:1\"}");
        CorporateAction saved = corporateActionRepository.save(newAction);
        assertThat(saved.getCaId()).isNotNull();
    }

    @Test
    void testFindById() {
        Optional<CorporateAction> found = corporateActionRepository.findById(corporateAction.getCaId());
        assertThat(found).isPresent();
        assertThat(found.get().getCaType()).isEqualTo(CAType.DIVIDEND);
    }

    @Test
    void testFindBySecurityId() {
        List<CorporateAction> actions = corporateActionRepository.findBySecurityId(101L);
        assertThat(actions).hasSize(1);
        assertThat(actions.get(0).getSecurityId()).isEqualTo(101L);
    }

    @Test
    void testFindByCaType() {
        List<CorporateAction> actions = corporateActionRepository.findByCaType(CAType.DIVIDEND);
        assertThat(actions).hasSize(1);
        assertThat(actions.get(0).getCaType()).isEqualTo(CAType.DIVIDEND);
    }

    @Test
    void testFindByRecordDateBetween() {
        List<CorporateAction> actions = corporateActionRepository.findByRecordDateBetween(
                LocalDate.of(2024, 6, 1), LocalDate.of(2024, 6, 30));
        assertThat(actions).hasSize(1);
    }

    @Test
    void testFindByRecordDateBetween_NoResults() {
        List<CorporateAction> actions = corporateActionRepository.findByRecordDateBetween(
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
        assertThat(actions).isEmpty();
    }

    @Test
    void testDeleteCorporateAction() {
        corporateActionRepository.delete(corporateAction);
        Optional<CorporateAction> found = corporateActionRepository.findById(corporateAction.getCaId());
        assertThat(found).isEmpty();
    }
}