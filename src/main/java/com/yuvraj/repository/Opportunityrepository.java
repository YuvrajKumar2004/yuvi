package com.yuvraj.repository;

import com.yuvraj.model.Opportunity;
import com.yuvraj.model.enums.OpportunityCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface Opportunityrepository extends JpaRepository<Opportunity,Long> {
    List<Opportunity>findByCategory(OpportunityCategory category);
    List<Opportunity> findByCoordinatorId(Long coordinatorId);
}
