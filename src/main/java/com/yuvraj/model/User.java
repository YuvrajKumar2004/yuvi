package com.yuvraj.model;


import com.yuvraj.model.enums.Role;
import jakarta.persistence.*;
import lombok.*;

//import javax.management.Notification;
import java.util.List;

@Entity
@Table(name="users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true,nullable = false,length = 100)
    private String loginId;
    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    @Column(nullable = false)
    private boolean locked=false;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private StudentProfile studentProfile;

    @OneToMany(mappedBy = "coordinator",cascade = CascadeType.ALL)
    private List<Opportunity>opportunities;
    @OneToMany(mappedBy ="user",cascade = CascadeType.ALL)
    private List<Notification>notifications;
    @OneToMany(mappedBy ="actor",cascade = CascadeType.ALL)
    private  List<AuditLog>auditLogs;

}
