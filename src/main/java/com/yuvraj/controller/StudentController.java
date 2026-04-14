package com.yuvraj.controller;

import com.yuvraj.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService service;
    private final AuthHelper authHelper;

    //=========DashBoard tabs===========

    @GetMapping("/dashboard")
    public String dashboard(Model model){
        Long userId= authHelper.getCurrentUserId();
        model.addAttribute("profile", service.getStudentProfile(userId));
        model.addAttribute("onCampus",service.getEligibleOnCampusOpportunities(userId));
        model.addAttribute("offCampus",service.getOffCampusOpportunities());
        model.addAttribute("applied",service.getApplied(userId));
        model.addAttribute("notification",service.getNotifications(userId));
        model.addAttribute("activeTab","profile");
        return "student/dashboard";
    }
    @GetMapping("/opportunities")
    public String opportunities(Model model){
        Long userId= authHelper.getCurrentUserId();
        model.addAttribute("profile",service.getStudentProfile(userId));
        model.addAttribute("onCampus",service.getEligibleOnCampusOpportunities(userId));
        model.addAttribute("offCampus",service.getOffCampusOpportunities());
        model.addAttribute("applied",service.getApplied(userId));
        model.addAttribute("notification",service.getNotifications(userId));
        model.addAttribute("activeTab","opportunities");
        return "student/dashboard";
    }
    @GetMapping("/applied")
    public String applied(Model model){
        Long userId= authHelper.getCurrentUserId();
        model.addAttribute("profile",service.getStudentProfile(userId));
        model.addAttribute("onCampus",service.getEligibleOnCampusOpportunities(userId));
        model.addAttribute("offCampus",service.getOffCampusOpportunities());
        model.addAttribute("applied",service.getApplied(userId));
        model.addAttribute("notification",service.getNotifications(userId));
        model.addAttribute("activeTab","applied");
        return "student/dashboard";
    }
    @GetMapping("/notifications")
    public String notifications(Model model){
        Long userId= authHelper.getCurrentUserId();
        model.addAttribute("profile",service.getStudentProfile(userId));
        model.addAttribute("onCampus",service.getEligibleOnCampusOpportunities(userId));
        model.addAttribute("offCampus",service.getOffCampusOpportunities());
        model.addAttribute("applied",service.getApplied(userId));
        model.addAttribute("notification",service.getNotifications(userId));
        model.addAttribute("activeTab","notifications");

        return "student/dashboard";
    }

    //=========Actions=============

    @PostMapping("/cv")
    public String updateCv(@RequestParam String cv1Url1,
                           @RequestParam String cv2Url1,
                           @RequestParam String cv3Url1,
                           RedirectAttributes ra){
        Long userId= authHelper.getCurrentUserId();
        service.updateCvLinks(userId,cv1Url1,cv2Url1,cv3Url1);
        ra.addFlashAttribute("success","CV links updated Successfully.");
        return "redirect:/student/dashboard";
    }

    @PostMapping("/apply")
    public String apply(@RequestParam Long oppotunityId,
                        @RequestParam String selectedCv,
                        RedirectAttributes ra){
        Long userId= authHelper.getCurrentUserId();
        try{
            service.apply(userId,oppotunityId,selectedCv);
            ra.addFlashAttribute("success","Application Submitted Successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error",e.getMessage());
        }
        return "redirect:/student/opportunities";
    }
}
