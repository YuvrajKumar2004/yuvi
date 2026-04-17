package com.yuvraj.service;

import com.yuvraj.model.*;
import com.yuvraj.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CoordinatorService {

    private final OpportunityRepository opportunityrepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationroundRepository roundRepository;
    private final StudentShareFieldRepository shareFieldRepository;
    private final NotificationRepository notificationrepository;
    private final UserRepository userRepository;
    private final EligibilityService eligibilityService;

    public List<Opportunity>getMyposts(Long coordinatorId){
        return opportunityrepository.findByCoordinatorId(coordinatorId);
    }

    /**Create a new opportunity post and save the shared field selection */

    public Opportunity createPost(Opportunity opportunity,Long coordinatorId,List<String>sharedFields){
        User coordinator=userRepository.findById(coordinatorId)
                .orElseThrow(()->new RuntimeException("Coordinator Not Found"));
        opportunity.setCoordinator(coordinator);
        Opportunity saved=opportunityrepository.save(opportunity);
        saveSharedFields(saved,sharedFields);

         return saved;
    }
    /**
     * Update an existing post. After updating eligibility criteria,
     * re-validates all existing applications and deletes any that no longer qualify.
     */

    @Transactional
    public Opportunity updatePost(Long postId,Opportunity updated,Long coordinatorId,List<String>sharedFields){
        Opportunity existing=getPost(postId,coordinatorId);

        //Update Fields
        existing.setCategory(updated.getCategory());
        existing.setCompanyName(updated.getCompanyName());
        existing.setJobRole(updated.getJobRole());
        existing.setTier(updated.getTier());
        existing.setStipendCtc(updated.getStipendCtc());
        existing.setDeadline(updated.getDeadline());
        existing.setSkills(updated.getSkills());
        existing.setOtherDetails(updated.getOtherDetails());
        existing.setEligibilityCgpa(updated.getEligibilityCgpa());
        existing.setEligibilityDeadBacklogs(updated.getEligibilityDeadBacklogs());
        existing.setEligibilityActiveBacklogs(updated.getEligibilityActiveBacklogs());
        existing.setEligibleBranch(updated.getEligibleBranch());
        existing.setEligibilityXPercentage(updated.getEligibilityXPercentage());
        existing.setGetEligibilityXiiPercentage(updated.getGetEligibilityXiiPercentage());
        existing.setEligibilityEnrollmentPrefix(updated.getEligibilityEnrollmentPrefix());
        existing.setEligibilityMaxGapYears(updated.getEligibilityMaxGapYears());

        Opportunity saved=opportunityrepository.save(existing);

        //Rebuild Shared field
        shareFieldRepository.deleteByOpportunityId(postId);
        saveSharedFields(saved,sharedFields);

        //Re-validate all existing applications against updated criteria
        List<Application>applications=applicationRepository.findByOpportunityId(postId);
        for(Application app:applications){
            if(!eligibilityService.checkEligibility(app.getStudent(),saved)){
                applicationRepository.delete(app);
            }

        }
        return saved;
    }
    public List<Application>getApplicationsForPost(Long postId,Long coordinatorId){
        getPost(postId,coordinatorId);
        return applicationRepository.findByOpportunityId(postId);
    }
    /** GENERATE CSV STRING DATA USING ONLY THE SHARED FIELDS.*/
    public String exportCsv(Long postId,Long coordinatorId)throws IOException {
        getPost(postId,coordinatorId);//ownership check
        List<Application>applications=applicationRepository.findByOpportunityId(postId);
        List<StudentSharedField>sharedFields=shareFieldRepository.findByOpportunityId(postId);
        List<String>fieldKeys=sharedFields.stream().
                map(StudentSharedField::getFieldKey).toList();

        StringWriter sw=new StringWriter();
        List<String>headers=new ArrayList<>(fieldKeys);
        headers.add(0,"enrollment");
        headers.add("selectCv");
        headers.add("appliedAt");

        try(CSVPrinter printer=new CSVPrinter(sw,
                CSVFormat.DEFAULT.builder().setHeader(headers.toArray(new String[0])).build())) {
            for (Application app:applications){
                StudentProfile s=app.getStudent();
                List<Object>row=new ArrayList<>();
                row.add(s.getEnrollmentNo());
                for(String key:fieldKeys){
                    row.add(getStudentField(s,key));
                }
                row.add(app.getSelectedCv());
                row.add(app.getCreatedAt().toString());
                printer.printRecord(row);
            }

        }
        return sw.toString();
    }

    /**
     * Upsert a round result for a given application.
     * If a round with the same roundNumber already exists for that application, update it;
     * otherwise create a new one.
     */
    @Transactional
    public void upsertRound(Long postId,Long coordinatorId,Long applicationId,
                            int roundNumber,String description,String date,
                            String centre,String time,String status){
        getPost(postId,coordinatorId);//ownership check

        Application app=applicationRepository.findById(applicationId)
                .orElseThrow(()->new RuntimeException("Application Not Found"));

        ApplicationRound round=roundRepository.findByApplicationIdAndRoundNumber(applicationId,roundNumber)
                .orElse(ApplicationRound.builder().application(app).roundNumber(roundNumber).build());

        round.setDescription(description);
        round.setDate(date !=null && !date.isBlank() ? LocalDate.parse(date):null);
        round.setCentre(centre);
        round.setTime(time);
        round.setStatus(status);
        roundRepository.save(round);

        //Notify student of result
        String notifBody="SELECTED".equals(status)
                ?"Congratulations! You are selected for Round "+roundNumber+" at"+app.getOpportunity().getCompanyName()
                :"Regret To inform you that you are not Qualified for round  "+roundNumber+" at"+app.getOpportunity().getCompanyName();
        Notification notification=Notification.builder().
                user(app.getStudent().getUser()).title("Round "+roundNumber+" Result")
                .body(notifBody).build();
        notificationrepository.save(notification);

    }

    //HELPER
    private Opportunity getPost(Long postId,Long coordinatorId){
        Opportunity opportunity=opportunityrepository.findById(postId)
                .orElseThrow(()->new RuntimeException("Post not Found"));
        if(!opportunity.getCoordinator().getId().equals(coordinatorId)){
            throw  new RuntimeException("Access Denied");
        }
        return opportunity;
    }
    private void saveSharedFields(Opportunity opp, List<String> keys) {
        if (keys == null) return;
        for (String key : keys) {
            StudentSharedField f = StudentSharedField.builder()
                    .opportunity(opp).fieldKey(key).build();
            shareFieldRepository.save(f);
        }
    }

    private Object getStudentField(StudentProfile s, String key) {
        return switch (key) {
            case "email"          -> s.getEmail();
            case "mobile"         -> s.getMobile();
            case "branch"         -> s.getBranch();
            case "cgpa"           -> s.getCgpa();
            case "xPercentage"    -> s.getXPercentage();
            case "xiiPercentage"  -> s.getXiiPercentage();
            case "activeBacklogs" -> s.getActiveBacklogs();
            case "deadBacklogs"   -> s.getDeadBacklogs();
            case "cv1Url"         -> s.getCv1Url1();
            case "cv2Url"         -> s.getCv2Url1();
            case "cv3Url"         -> s.getCv3Url1();
            default               -> "";
        };
    }
}
