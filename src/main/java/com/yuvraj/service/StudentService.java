package com.yuvraj.service;

import com.yuvraj.model.Application;
import com.yuvraj.model.Notification;
import com.yuvraj.model.Opportunity;
import com.yuvraj.model.StudentProfile;
import com.yuvraj.model.enums.OpportunityCategory;
import com.yuvraj.repository.ApplicationRepository;
import com.yuvraj.repository.Notificationrepository;
import com.yuvraj.repository.Opportunityrepository;
import com.yuvraj.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentProfileRepository studentProfileRepository;
    private final Opportunityrepository opportunityrepository;
    private final ApplicationRepository applicationRepository;
    private final Notificationrepository notificationrepository;
    private final EligibilityService eligibilityService;

    /* RETURN THE STUDENT'S PROFILE FOR THE LOGGED-IN USER.*/
    public StudentProfile getStudentProfile(Long userId){
        return studentProfileRepository.findByUserId(userId)
                .orElseThrow(()->new RuntimeException("Student Profile not found"));
    }

    /* UPDATE ONLY CV LINKS-STUDENTS CAN ONLY CHANGE THEIR OWN CVS. */
    @Transactional
    public void updateCvLinks(Long userId,String cv1,String cv2,String cv3){
        StudentProfile profile=getStudentProfile(userId);
        profile.setCv1Url1(cv1);
        profile.setCv2Url1(cv2);
        profile.setCv3Url1(cv3);
        studentProfileRepository.save(profile);
    }

    /*RETURNS ALL ON_CAMPUS OPPORTUNITIES THE STUDENT IS ELIGIBLE FOR.*/
    public List<Opportunity>getEligibleOnCampusOpportunities(Long userId){
        StudentProfile student=getStudentProfile(userId);
        return opportunityrepository.findByCategory(OpportunityCategory.ON_CAMPUS)
                .stream().filter(opportunity -> eligibilityService.checkEligibility(student,opportunity))
                .toList();
    }
    /* Return all OFF_Campus opportunities(no eligibility filter)*/
    public List<Opportunity>getOffCampusOpportunities(){
        return opportunityrepository.findByCategory(OpportunityCategory.OFF_CAMPUS);
    }
    /*
     * Apply to an opportunity.
     * One-student-one-application rule: if student already applied, old application
     * and all its rounds are deleted before creating the new one.
     * Eligibility is re-validated server-side before creating the application.
     */
    @Transactional
    public void apply(Long userId,Long opportunityId,String selectCv){
        StudentProfile student=getStudentProfile(userId);
        Opportunity opportunity=opportunityrepository.findById(opportunityId)
                .orElseThrow(()->new RuntimeException("opportunity not found"));

        //Server-side eligibility check(authoritative)
        if(!eligibilityService.checkEligibility(student,opportunity)){
            throw new RuntimeException("You are not eligible for this opportunity");

        }
        //One-student-one-application:remove previous appication if exists
        applicationRepository.findByStudentIdAndOpportunityId(student.getId(),opportunityId)
                .ifPresent(existing->applicationRepository.delete(existing));

        Application application=Application.builder()
                .student(student).opportunity(opportunity)
                .selectedCv(selectCv).acceptedTerms(true).build();
        applicationRepository.save(application);

        //NOTIFY Students
        Notification notification=Notification.builder()
                .user(student.getUser())
                .title("Application Submitted")
                .body("You have successfully applied to "+opportunity.getCompanyName()+" - "+opportunity.getJobRole())
                .build();
        notificationrepository.save(notification);


    }
    /* Returns all applications with their round data for the logged-in student */
    public List<Application>getApplied(Long userId){
        StudentProfile student=getStudentProfile(userId);
        return applicationRepository.findByStudentId(student.getId());
    }

    /* Returns all notifications for the logged-in user */
    public List<Notification>getNotifications(Long userId){
        return notificationrepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
