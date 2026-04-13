package com.yuvraj.model;

import com.yuvraj.model.enums.OpportunityCategory;
import com.yuvraj.model.enums.OpportunityTier;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name="opportunities")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Opportunity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OpportunityCategory category;
    @Column(nullable = false)
    private String companyName;
    @Column(nullable = false)
    private String jobRole;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OpportunityTier tier;
    private String stipendCtc;
    private LocalDateTime deadline;
    private String skills;
    @Column(columnDefinition = "TEXT")
    private String otherDetails;


    //Eligibility fields
    private String eligibilityEnrollmentPrefix;
    private Double eligibilityXPercentage;
    private Double getEligibilityXiiPercentage;
    private Integer eligibilityActiveBacklogs;
    private Integer eligibilityDeadBacklogs;
    private Double eligibilityCgpa;
    private String eligibleBranch;
    private Integer eligibilityMaxGapYears;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coordinator_id")
    private User coordinator;
    @OneToMany(mappedBy = "opportunity",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<Application>applications;
    @OneToMany(mappedBy = "opportunity",cascade = CascadeType.ALL,orphanRemoval = true)
    private  List<StudentSharedField>shareFields;
}
