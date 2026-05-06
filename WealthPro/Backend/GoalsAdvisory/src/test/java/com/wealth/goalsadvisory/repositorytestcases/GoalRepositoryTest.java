package com.wealth.goalsadvisory.repositorytestcases;

import com.wealth.goalsadvisory.entity.Goal;
import com.wealth.goalsadvisory.enums.GoalStatus;
import com.wealth.goalsadvisory.enums.GoalType;
import com.wealth.goalsadvisory.repository.GoalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class GoalRepositoryTest {

    @Autowired
    private GoalRepository goalRepository;

    private Goal goal1;
    private Goal goal2;

    @BeforeEach
    void setUp() {
        goalRepository.deleteAll();

        goal1 = new Goal();
        goal1.setClientId(101L);
        goal1.setGoalType(GoalType.RETIREMENT);
        goal1.setTargetAmount(new BigDecimal("5000000.00"));
        goal1.setTargetDate(LocalDate.of(2035, 12, 31));
        goal1.setPriority(1);
        goal1.setStatus(GoalStatus.ACTIVE);
        goalRepository.save(goal1);

        goal2 = new Goal();
        goal2.setClientId(101L);
        goal2.setGoalType(GoalType.EDUCATION);
        goal2.setTargetAmount(new BigDecimal("1000000.00"));
        goal2.setTargetDate(LocalDate.of(2030, 6, 1));
        goal2.setPriority(2);
        goal2.setStatus(GoalStatus.IN_PROGRESS);
        goalRepository.save(goal2);
    }

    @Test
    void findById_WhenExists() {
        Optional<Goal> found = goalRepository.findById(goal1.getGoalId());
        assertTrue(found.isPresent());
        assertEquals(GoalType.RETIREMENT, found.get().getGoalType());
    }

    @Test
    void findById_WhenNotExists() {
        assertFalse(goalRepository.findById(999L).isPresent());
    }

    @Test
    void findAll_ReturnsAllSavedGoals() {

        assertEquals(2, goalRepository.findAll().size());
    }

    @Test
    void delete_RemoveGoal() {
        goalRepository.delete(goal1);
        assertFalse(goalRepository.findById(goal1.getGoalId()).isPresent());
    }


    @Test
    void findByClientId_AllGoalsForClient() {
        List<Goal> goals = goalRepository.findByClientId(101L);
        assertEquals(2, goals.size());
    }

    @Test
    void findByClientId_WhenClientHasNoGoals() {
        assertTrue(goalRepository.findByClientId(999L).isEmpty());
    }

    @Test
    void findByClientIdAndStatus_ActiveGoalOnly() {
        List<Goal> goals = goalRepository.findByClientIdAndStatus(101L, GoalStatus.ACTIVE);
        assertEquals(1, goals.size());
        assertEquals(GoalType.RETIREMENT, goals.get(0).getGoalType());
    }

    @Test
    void findByClientIdAndStatus_WhenNoMatch() {
        assertTrue(goalRepository.findByClientIdAndStatus(101L, GoalStatus.ACHIEVED).isEmpty());
    }
}