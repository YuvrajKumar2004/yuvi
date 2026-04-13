package com.yuvraj.service;

import com.yuvraj.model.Opportunity;
import com.yuvraj.model.StudentProfile;
import com.yuvraj.model.enums.OpportunityTier;
import com.yuvraj.model.enums.PlacementStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class EligibilityService {

    public boolean checkEligibility(StudentProfile student, Opportunity opportunity){
        //STUDENT MUST NOT BE LOCKED
        if(student.getUser().isLocked())return false;
        //DEADLINE MUST NOT BE PASSED
        if(opportunity.getDeadline() !=null && opportunity.getDeadline().isBefore(LocalDateTime.now()))return false;
        //ENROLLMENT PREFIX
        if(hasValue(opportunity.getEligibilityEnrollmentPrefix())){
            if(!student.getEnrollmentNo().startsWith(opportunity.getEligibilityEnrollmentPrefix()))return false;
        }
        //X percentage
        if(opportunity.getEligibilityXPercentage() !=null && student.getXPercentage() !=null){
            if(student.getXPercentage()<opportunity.getEligibilityXPercentage())return false;
        }
        //Xii percentage
        if(opportunity.getGetEligibilityXiiPercentage()!=null && student.getXiiPercentage() !=null){
            if(student.getXiiPercentage()<opportunity.getGetEligibilityXiiPercentage())return false;
        }
        //ACTIVE BACKLOGS
        if(opportunity.getEligibilityActiveBacklogs() !=null){
            if(student.getActiveBacklogs() >opportunity.getEligibilityActiveBacklogs())return false;

        }
        //DEAD BACKLOGS
        if(opportunity.getEligibilityDeadBacklogs() !=null){
            if(student.getDeadBacklogs() >opportunity.getEligibilityDeadBacklogs())return false;
        }
        //CGPA
        if(opportunity.getEligibilityCgpa()!=null && student.getCgpa() !=null){
            if(student.getCgpa() <opportunity.getEligibilityCgpa())return false;
        }
        //Branch(comma-separated list,blank=all allowed
        if(hasValue(opportunity.getEligibleBranch())){
            List<String>allowedbranch= Arrays.stream(
                    opportunity.getEligibleBranch().split(",")).map(String::trim)
                    .toList();
            if(!allowedbranch.contains(student.getBranch()))return false;

        }
        //YearGap
        if(opportunity.getEligibilityMaxGapYears() !=null && student.isHasYearGap()){
            int gap= student.getYearGapDuration()!=null?student.getYearGapDuration():0;
            if(gap>opportunity.getEligibilityMaxGapYears())return false;
        }
        //PLACEMENT CELL RESTRICTION LOGIC
        PlacementStatus status=student.getPlacementStatus();
        OpportunityTier tier=opportunity.getTier();
        if(status==PlacementStatus.DREAM_PLACED)return false;
        if(status==PlacementStatus.STANDARD_PLACED && tier !=OpportunityTier.DREAM)return false;
        if(status==PlacementStatus.NORMAL_PLACED && tier !=OpportunityTier.DREAM && tier !=OpportunityTier.STANDARD)return false;

        return true;

    }
    private boolean hasValue(String s){
        return s!=null && !s.isBlank();
    }
}
