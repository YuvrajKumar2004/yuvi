package com.yuvraj.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "student_shared_fields")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentSharedField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opportunity_id", nullable = false)
    private Opportunity opportunity;

    @Column(nullable = false, length = 50)
    private String fieldKey; // e.g. "email", "cgpa", "cv1Url"
}
