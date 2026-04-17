package com.yuvraj.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "application_rounds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationRound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Column(nullable = false)
    private int roundNumber;

    private String description;
    private LocalDate date;
    private String centre;
    private String time;

    // "SELECTED", "REJECTED", "PENDING"
    @Column(length = 20)
    @Builder.Default
    private String status = "PENDING";

}
