package com.yuvraj.repository;

import com.yuvraj.model.StudentSharedField;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentShareFieldRepository extends JpaRepository<StudentSharedField,Long> {
    List<StudentSharedField>findByOpportunityId(Long opportunity);
    void deleteByOpportunityId(Long opportunityId);
}
