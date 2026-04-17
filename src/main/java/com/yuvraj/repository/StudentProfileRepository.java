package com.yuvraj.repository;

import com.yuvraj.model.StudentProfile;
import com.yuvraj.model.enums.PlacementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StudentProfileRepository extends JpaRepository<StudentProfile,Long> {

    Optional<StudentProfile> findByEnrollmentNo(String enrollmentNo);


    Optional<StudentProfile> findByUserId(Long userId);
    boolean existsByEnrollmentNo(String enrollmentNo);
    List<StudentProfile> findByBranch(String branch);
    long countByPlacementStatus(PlacementStatus status);
    @Query("SELECT s.branch, COUNT(s) FROM StudentProfile s GROUP BY s.branch")
    List<Object[]> countByBranchGrouped();

    @Query("SELECT s.branch, COUNT(s) FROM StudentProfile s " +
            "WHERE s.placementStatus != com.yuvraj.model.enums.PlacementStatus.UNPLACED " +
            "GROUP BY s.branch")
    List<Object[]> countPlacedByBranchGrouped();

    @Query("SELECT s FROM StudentProfile s JOIN s.user u WHERE u.locked = true")
    List<StudentProfile> findAllLocked();
}
