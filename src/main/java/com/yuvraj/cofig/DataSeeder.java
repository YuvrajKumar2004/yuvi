package com.yuvraj.cofig;

import com.yuvraj.model.StudentProfile;
import com.yuvraj.model.User;
import com.yuvraj.model.enums.PlacementStatus;
import com.yuvraj.model.enums.Role;
import com.yuvraj.repository.StudentProfileRepository;
import com.yuvraj.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {


    private final UserRepository userRepo;
    private final StudentProfileRepository studentProfileRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    public void run(String... args) throws Exception {
        if(userRepo.count()>0){
            log.info("Database already seeded - skipping.");
            return;
        }
        log.info("seeding initial data.......");

        //CCD Admin
        createUser("ccd_admin","admin123", Role.CCD_ADMIN,null);
        //CCD Member
        createUser("ccd_member","member123",Role.CCD_MEMBER,null);
        //Coordinator
        createUser("coord01","coord123",Role.COORDINATOR,null);
        //Students
        User s1=createUser("23UEC077","student123",Role.STUDENT,null);
        studentProfileRepository.save(StudentProfile.builder()
                .user(s1)
                .enrollmentNo("23UEC077")
                .name("Ravi Kumar")
                .email("ravi@nita.ac.in")
                .mobile("9876543210")
                .branch("ECE")
                .cgpa(8.5)
                .xPercentage(92.0)
                .xiiPercentage(88.0)
                .activeBacklogs(0)
                .deadBacklogs(0)
                .hasYearGap(false)
                .placementStatus(PlacementStatus.UNPLACED)
                .build());

        User s2 = createUser("23EC001", "student123", Role.STUDENT, null);
        studentProfileRepository.save(StudentProfile.builder()
                .user(s2)
                .enrollmentNo("23EC001")
                .name("Priya Singh")
                .email("priya@nita.ac.in")
                .mobile("9876543211")
                .branch("ECE")
                .cgpa(7.8)
                .xPercentage(85.0)
                .xiiPercentage(82.0)
                .activeBacklogs(0)
                .deadBacklogs(0)
                .hasYearGap(false)
                .placementStatus(PlacementStatus.UNPLACED)
                .build());


        log.info("Seeding complete. Default credentials:");
        log.info("  CCD Admin   → loginId: ccd_admin  / password: admin123");
        log.info("  CCD Member  → loginId: ccd_member / password: member123");
        log.info("  Coordinator → loginId: coord01    / password: coord123");
        log.info("  Student     → loginId: 23CS001    / password: student123");
    }
    private User createUser(String loginId, String password, Role role, String ignored) {
        User user = User.builder()
                .loginId(loginId)
                .passwordHash(passwordEncoder.encode(password))
                .role(role)
                .locked(false)
                .build();
        return userRepo.save(user);
    }
}
