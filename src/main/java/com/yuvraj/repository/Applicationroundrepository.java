package com.yuvraj.repository;

import com.yuvraj.model.ApplicationRound;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface Applicationroundrepository extends JpaRepository<ApplicationRound,Long> {
    List<ApplicationRound> findByApplicationId(Long applicationId);
    List<ApplicationRound>findByApplicationIdOrderByRoundNumber(Long applicationId);
    Optional<ApplicationRound> findByApplicationIdAndRoundNumber(Long applicationId, int roundNumber);
}
