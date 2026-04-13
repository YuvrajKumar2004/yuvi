package com.yuvraj.repository;

import com.yuvraj.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application,Long> {
    List<Application> findByStudentId(Long studentId);
    List<Application>findByOpportunityId(Long opportunityId);

    Optional<Application> findByStudentIdAndOpportunityId(Long studentId, Long opportunityId);
    boolean existsByStudentIdAndOpportunityId(Long studentId,Long opportunityId);
    void deleteByStudentIdAndOpportunityId(Long studentId,Long opportunityId);
}
