package com.yuvraj.model;

import com.yuvraj.model.enums.PlacementStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name="student_profiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudentProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id",unique = true,nullable = false)
    private User user;

    @Column(unique = true,nullable = false,length = 20)
    private String enrollmentNo;
    private String name;
    private String email;
    private String mobile;
    private String branch;
    private String photoUrl;

    // SGPA per semester
    private Double sgpaSem1;
    private Double sgpaSem2;
    private Double sgpaSem3;
    private Double sgpaSem4;
    private Double sgpaSem5;
    private Double sgpaSem6;
    private Double sgpaSem7;
    private Double sgpaSem8;

    private Double cgpa;
    private Double xPercentage;
    private Double xiiPercentage;

    @Column(nullable = false)
    private int activeBacklogs=0;
    @Column(nullable = false)
    private int deadBacklogs=0;
    @Column(nullable = false)
    private boolean hasYearGap=false;
    private Integer yearGapDuration;

    private String cv1Url1;
    private String cv2Url1;
    private String cv3Url1;

    // TPO contact
    private String tpoName;
    private String tpoEmail;
    private String tpoMobile;

    // TNP contact
    private String tnpName;
    private String tnpEmail;
    private String tnpMobile;

    // IC contact
    private String icName;
    private String icEmail;
    private String icMobile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlacementStatus placementStatus = PlacementStatus.UNPLACED;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    private List<Application> applications;
}

