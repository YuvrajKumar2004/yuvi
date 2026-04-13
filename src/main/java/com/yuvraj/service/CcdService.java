package com.yuvraj.service;

import com.yuvraj.model.AuditLog;
import com.yuvraj.model.StudentProfile;
import com.yuvraj.model.User;
import com.yuvraj.model.enums.PlacementStatus;
import com.yuvraj.model.enums.Role;
import com.yuvraj.repository.Auditlogrepository;
import com.yuvraj.repository.StudentProfileRepository;
import com.yuvraj.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;



@Service
@RequiredArgsConstructor
public class CcdService {
    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final Auditlogrepository auditlogrepository;
    private final PasswordEncoder passwordEncoder;

    //------DASHBOARD---------------
    public Map<String,Object> getDashboardstats(){
        long total=studentProfileRepository.count();
        long dreamPlaced=studentProfileRepository.countByPlacementStatus(PlacementStatus.DREAM_PLACED);
        long standardPlaced=studentProfileRepository.countByPlacementStatus(PlacementStatus.STANDARD_PLACED);
        long normalPlaced=studentProfileRepository.countByPlacementStatus(PlacementStatus.NORMAL_PLACED);
        long unplaced=studentProfileRepository.countByPlacementStatus(PlacementStatus.UNPLACED);
        long placed=dreamPlaced+standardPlaced+normalPlaced;
        double placementPercent=total>0?((double) (placed * 100) /total):0;

        //Branch-Wise stats
        Map<String,Long>branchTotal=new LinkedHashMap<>();
        Map<String,Long>branchPlaced=new LinkedHashMap<>();
        for(Object[] row:studentProfileRepository.countByBranchGrouped()){
            branchTotal.put((String)row[0],(Long)row[1]);
        }
        for(Object[] row:studentProfileRepository.countPlacedByBranchGrouped()){
            branchPlaced.put((String)row[0],(Long)row[1]);
        }
        long lockedCount=studentProfileRepository.findAllLocked().size();

        Map<String,Object>stats=new LinkedHashMap<>();
        stats.put("TotalStudents",total);
        stats.put("PlacedStudents",placed);
        stats.put("placementPercent",String.format("%.2f",placementPercent));
        stats.put("DreamPlaced",dreamPlaced);
        stats.put("standardPlaced",standardPlaced);
        stats.put("normalPlaced",normalPlaced);
        stats.put("unplaced",unplaced);
        stats.put("branchTotal",branchTotal);
        stats.put("lockedCount",lockedCount);
        return stats;
    }

    //--------USER MANAGEMENT---------------
    /**
     * create or update a Coordinator/CCD Member account.
     * Uses loginId as the unique key(upsert behaviour).
     */
    @Transactional
    public void upsertUser(String loginId, String plainPassword, Role role, Long actorId){
        User user=userRepository.findByLoginId(loginId)
                .orElse(User.builder().loginId(loginId).build());
        user.setPasswordHash(passwordEncoder.encode(plainPassword));
        user.setRole(role);
        userRepository.save(user);
        writeAudit(actorId,"UPSERT_USER","{\"loginId\":\"" + loginId + "\",\"role\":\"" + role + "\"}");
    }

    //-----------STUDENT LOCK/UNLOCKED-------------

    @Transactional
    public void setLocked(String enrollment,boolean locked,Long actorId){
        StudentProfile profile=studentProfileRepository.findByEnrollmentNo(enrollment)
                .orElseThrow(()->new RuntimeException("Student not found with enrollment "+enrollment));
        profile.getUser().setLocked(locked);
        userRepository.save(profile.getUser());
        String action=locked?"LOCK_STUDENT":"UNLOCK_STUDENT";
        writeAudit(actorId,action,"{\"enrollment\":\""+enrollment+"\"}");
    }
    public List<StudentProfile>getLockedStudents(){
        return studentProfileRepository.findAllLocked();
    }

    //------------Student search & edit-----------
    public StudentProfile searchByEnrollment(String enrollment){
        return studentProfileRepository.findByEnrollmentNo(enrollment)
                .orElseThrow(()->new RuntimeException("Student not found"));
    }
    public StudentProfile getStudentByuserId(Long userId){
        return studentProfileRepository.findByUserId(userId)
                .orElseThrow(()->new RuntimeException("Student not found"));

    }

    @Transactional
    public void updateStudentProfile(Long userId,StudentProfile updated,Long actorId){
        StudentProfile existing=getStudentByuserId(userId);
        existing.setName(updated.getName());
        existing.setEmail(updated.getEmail());
        existing.setMobile(updated.getMobile());
        existing.setBranch(updated.getBranch());
        existing.setCgpa(updated.getCgpa());
        existing.setXPercentage(updated.getXPercentage());
        existing.setXiiPercentage(updated.getXiiPercentage());
        existing.setSgpaSem1(updated.getSgpaSem1());
        existing.setSgpaSem2(updated.getSgpaSem2());
        existing.setSgpaSem3(updated.getSgpaSem3());
        existing.setSgpaSem4(updated.getSgpaSem4());
        existing.setSgpaSem5(updated.getSgpaSem5());
        existing.setSgpaSem6(updated.getSgpaSem6());
        existing.setSgpaSem7(updated.getSgpaSem7());
        existing.setSgpaSem8(updated.getSgpaSem8());
        existing.setActiveBacklogs(updated.getActiveBacklogs());
        existing.setDeadBacklogs(updated.getDeadBacklogs());
        existing.setHasYearGap(updated.isHasYearGap());
        existing.setYearGapDuration(updated.getYearGapDuration());
        existing.setCv1Url1(updated.getCv1Url1());
        existing.setCv2Url1(updated.getCv2Url1());
        existing.setCv3Url1(updated.getCv3Url1());
        existing.setTpoName(updated.getTpoName());
        existing.setTpoEmail(updated.getTpoEmail());
        existing.setTpoMobile(updated.getTpoMobile());
        existing.setTnpName(updated.getTnpName());
        existing.setTnpEmail(updated.getTnpEmail());
        existing.setTnpMobile(updated.getTnpMobile());
        existing.setIcName(updated.getIcName());
        existing.setIcEmail(updated.getIcEmail());
        existing.setIcMobile(updated.getIcMobile());
        existing.setPlacementStatus(updated.getPlacementStatus());

        studentProfileRepository.save(existing);
        writeAudit(actorId,"UPDATE_STUDENT_PROFILE",
                "{\"userId\":" + userId + ",\"enrollment\":\"" + existing.getEnrollmentNo() + "\"}");

    }
    /// ---------Single student upsert------------
    @Transactional
    public String upsertStudent(String loginId,String plainPassword,
                               String enrollment,StudentProfile profileData){
        if(studentProfileRepository.existsByEnrollmentNo(enrollment)){
            //update existing
            StudentProfile existing=studentProfileRepository.findByEnrollmentNo(enrollment).get();
            if(plainPassword !=null && !plainPassword.isBlank()){
                existing.getUser().setPasswordHash(passwordEncoder.encode(plainPassword));
                userRepository.save(existing.getUser());

            }
            updateStudentProfile(existing.getUser().getId(),profileData,null);
            return "updated";
        }else{
            User user=User.builder().loginId(loginId).
                    passwordHash(passwordEncoder.encode(plainPassword)).
                    role(Role.STUDENT).locked(false).build();
            userRepository.save(user);
            profileData.setUser(user);
            profileData.setEnrollmentNo(enrollment);
            studentProfileRepository.save(profileData);
            return "created";
        }
    }


    //==============Bulk student upload===========

    @Transactional
    public Map<String,Object>bulkUpsertStudents(List<Map<String,String>>rows){
        int created=0,updated=0;
        List<String>errors=new ArrayList<>();

        for(int i=0;i<rows.size();i++){
            Map<String,String>row=rows.get(i);
            try{
                String result=upsertStudent(
                        row.get("loginId"),
                        row.get("password"),
                        row.get("enrollment"),
                        buildProfileFromMap(row)
                );
                if("created".equals(result))created++;
                else updated++;
            }catch (Exception e){
                errors.add("Row "+(i+2)+": "+e.getMessage());
            }
        }
        return Map.of("created",created,"updated",updated,"errors",errors);
    }
    //=============private helpers==================
    private StudentProfile buildProfileFromMap(Map<String,String>row){
        StudentProfile p=new StudentProfile();
        p.setName(row.getOrDefault("name", ""));
        p.setEmail(row.getOrDefault("email", ""));
        p.setMobile(row.getOrDefault("mobile", ""));
        p.setBranch(row.getOrDefault("branch", ""));
        p.setCgpa(parseDouble(row.get("cgpa")));
        p.setXPercentage(parseDouble(row.get("xPercentage")));
        p.setXiiPercentage(parseDouble(row.get("xiiPercentage")));
        p.setActiveBacklogs(parseInt(row.get("activeBacklogs"), 0));
        p.setDeadBacklogs(parseInt(row.get("deadBacklogs"), 0));
        p.setPlacementStatus(PlacementStatus.UNPLACED);
        return p;
    }
    private  int parseInt(String s,int def){
        try{return  s!=null && !s.isBlank()?Integer.parseInt(s):def;}
        catch (NumberFormatException e){return def;}

    }
    private Double parseDouble(String s){
        try{return s!=null && !s.isBlank()?Double.parseDouble(s):null;}
        catch (NumberFormatException e){return null;}
    }
    private void writeAudit(Long actorId,String action,String meta){
        if(actorId ==null)return;
        User actor=userRepository.findById(actorId).orElse(null);
        AuditLog log=AuditLog.builder().actor(actor).action(action).meta(meta).build();
        auditlogrepository.save(log);
    }
}
