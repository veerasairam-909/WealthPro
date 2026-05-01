package com.wealth.goalsadvisory.repository;

import com.wealth.goalsadvisory.entity.Goal;
import com.wealth.goalsadvisory.enums.GoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findByClientId(Long clientId);
    List<Goal> findByClientIdAndStatus(Long clientId, GoalStatus status);

}