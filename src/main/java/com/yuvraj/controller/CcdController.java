package com.yuvraj.controller;

import com.yuvraj.model.StudentProfile;
import com.yuvraj.model.enums.Role;
import com.yuvraj.service.CcdService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ccd")
@RequiredArgsConstructor
public class CcdController {
    private final CcdService ccdService;
    private final AuthHelper authHelper;

    //===============Dashboard(admin + member)=======

    @GetMapping("/dashboard")
    public String dashboard(Model model){
        model.addAttribute("stats",ccdService.getDashboardstats());
        return "ccd/dashboard";
    }

    //===============User Management (admin only)================
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('CCD_ADMIN')")
    public String usersPage(){
        return "ccd/users";
    }

    @PostMapping("/admin/users")
    @PreAuthorize("hasRole('CCD_ADMIN')")
    public String upsertUser(@RequestParam String loginId,
                             @RequestParam String password,
                             @RequestParam Role role,
                             RedirectAttributes ra){
        Long actorId= authHelper.getCurrentUserId();
        ccdService.upsertUser(loginId,password,role,actorId);
        ra.addFlashAttribute("success","User '"+loginId+"'saved successfully.");
        return "redirect:/ccd/admin/users";
    }

    //============LOCK/UNLOCK Student(admin only)===========

    @GetMapping("/admin/students/locked")
    @PreAuthorize("hasRole('CCD_ADMIN')")
    public String lockedStudents(Model model){
        model.addAttribute("lockedStudents",ccdService.getLockedStudents());
        return "ccd/locked-students";
    }

    //=============Student Search & edit (admin only)====================

    @GetMapping("/admin/students")
    @PreAuthorize("hasRole('CCD_ADMIN')")
    public String studentsPage(Model model,
                               @RequestParam(required = false)String enrollment){
        if(enrollment !=null && !enrollment.isBlank()){
            try{
                model.addAttribute("foundStudent",ccdService.searchByEnrollment(enrollment));

            } catch (Exception e) {
                model.addAttribute("searcherror",e.getMessage());
            }
        }
        return "ccd/students";
    }

    @GetMapping("/admin/students/{userId}/edit")
    @PreAuthorize("hasRole('CCD_ADMIN')")
    public String editStudentPage(@PathVariable Long userId,Model model){
        model.addAttribute("student",ccdService.getStudentByuserId(userId));
        return "ccd/edit-student";
    }
    @PostMapping("/admin/students/{userId}/edit")
    public String updateStudentProfile(@PathVariable Long userId,
                                       @ModelAttribute StudentProfile profile,
                                       RedirectAttributes ra){
        Long actorId= authHelper.getCurrentUserId();
        ccdService.updateStudentProfile(userId,profile,actorId);
        ra.addFlashAttribute("success","Profile updated.");
        return "redirect:ccd/admin/students/"+userId+"/edit";
    }

    //============Add single Student(admin only)===================
    @GetMapping("/admin/students/add")
    @PreAuthorize("hasRole('CCD_ADMIN')")
    public String addStudent(@RequestParam String loginId,
                             @RequestParam String password,
                             @RequestParam String enrollment,
                             @ModelAttribute StudentProfile profile,
                             RedirectAttributes ra){
        String result= ccdService.upsertStudent(loginId,password,enrollment,profile);
        ra.addFlashAttribute("success","Student "+enrollment+" "+result+".");
        return "redirect:/ccd/admin/students/add";
    }

    //===========Bulk CSV Upload (admin only)=============

    @GetMapping("admin/students/bulk")
    @PreAuthorize("hasRole('CCD_ADMIN')")
    public String bulkUploadPage(){
        return "ccd/bulk-upload";
    }

    @PostMapping("/admin/students/bulk")
    @PreAuthorize("hasRole('CCD_ADMIN')")
    public String bulkUpload(@RequestParam("file")MultipartFile file,
                             RedirectAttributes ra){
        if(file.isEmpty()){
            ra.addFlashAttribute("error","Please select a CSV File");
            return "redirect:/ccd/admin/students/bulk";
        }
        try(Reader reader=new InputStreamReader(file.getInputStream())){
            Iterable<CSVRecord>records= CSVFormat.DEFAULT
                    .builder().setHeader().setSkipHeaderRecord(true).build()
                    .parse(reader);
            List<Map<String,String>>rows=new ArrayList<>();
            for (CSVRecord record:records){
                rows.add(record.toMap());
            }
            Map<String,Object>result=ccdService.bulkUpsertStudents(rows);
            ra.addFlashAttribute("bulkResult",result);
        } catch (Exception e) {
            ra.addFlashAttribute("error","Failed to process CSV file"+e.getMessage());
        }
        return "redirect:/ccd/admin/students/bulk";
    }
}
