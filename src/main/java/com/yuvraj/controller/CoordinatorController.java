package com.yuvraj.controller;

import com.yuvraj.model.Opportunity;
import com.yuvraj.service.CoordinatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/coordinator")
@RequiredArgsConstructor
public class CoordinatorController {
    private final CoordinatorService coordinatorService;
    private final AuthHelper authHelper;

    //============Dashboard===================
    @GetMapping("/dashboard")
    public String dashboard(Model model){
        Long userId= authHelper.getCurrentUserId();
        model.addAttribute("posts",coordinatorService.getMyposts(userId));
        model.addAttribute("activeTab","posts");
        return "coordinator/dashboard";
    }

    @GetMapping("/new-post")
    public String newPostPage(Model model){
        model.addAttribute("opportunity",new Opportunity());
        model.addAttribute("editMode",false);
        model.addAttribute("deadlineValue","");
        model.addAttribute("eligibilityXiiValue",null);
        model.addAttribute("selectedSharedFields", List.of());
        return "coordinator/post-form";
    }

    @GetMapping("edit-post/{id}")
    public String editPostPage(@PathVariable Long id,Model model){
        Long userId= authHelper.getCurrentUserId();
        //LOAD EXISTING POST(SERVICE DOES OWNERSHIP CHECK)
        List<Opportunity>myPosts=coordinatorService.getMyposts(userId);
        Opportunity opp=myPosts.stream()
                .filter(p->p.getId().equals(id))
                .findFirst()
                .orElseThrow(()->new RuntimeException("Post not Found"));
        model.addAttribute("opportunity",opp);
        model.addAttribute("editMode",true);
        model.addAttribute("deadlineValue", opp.getDeadline() != null ? opp.getDeadline().toLocalDate().toString() : "");
        model.addAttribute("eligibilityXiiValue", opp.getGetEligibilityXiiPercentage());
        model.addAttribute("selectedSharedFields",
                opp.getShareFields() == null ? List.of() :
                        opp.getShareFields().stream().map(f -> f.getFieldKey()).collect(Collectors.toList()));
        return "coordinator/post-form";
    }

    //============Post CRUD==============

    @PostMapping("/posts")
    public String createPost(@ModelAttribute Opportunity opportunity,
                             @RequestParam(required = false)List<String>sharedFields,
                             @RequestParam(required = false)String deadlineStr,
                             RedirectAttributes ra){
        Long userId= authHelper.getCurrentUserId();
        if(deadlineStr !=null && !deadlineStr.isBlank()){
            opportunity.setDeadline(LocalDate.parse(deadlineStr).atStartOfDay());
        }
        coordinatorService.createPost(opportunity,userId,sharedFields);
        ra.addFlashAttribute("success","Post Created Successfully.");
        return "redirect:/coordinator/dashboard";
    }

    @PostMapping("/posts/{id}/update")
    public String updatePost(@PathVariable Long id,
                             @ModelAttribute Opportunity opportunity,
                             @RequestParam(required = false)List<String>sharedFiels,
                             @RequestParam(required = false)String deadlineStr,
                             RedirectAttributes ra){
        Long userId= authHelper.getCurrentUserId();
        if(deadlineStr !=null && !deadlineStr.isBlank()){
            opportunity.setDeadline(LocalDate.parse(deadlineStr).atStartOfDay());
        }
        coordinatorService.updatePost(id,opportunity,userId,sharedFiels);
        ra.addFlashAttribute("success","Post updated. Ineligible applications removed.");
        return "redirect:/coordinator/dashboard";
    }

    //===============Applications============
    @GetMapping("/posts/{id}/applications")
    public String viewApplications(@PathVariable Long id,Model model){
        Long userId= authHelper.getCurrentUserId();
        model.addAttribute("postId",id);
        model.addAttribute("applications",coordinatorService.getApplicationsForPost(id,userId));
        return "coordinator/application";
    }
    //=============CSV Export===============

    @GetMapping("/posts/{id}/export")
    public ResponseEntity<byte[]>exportCsv(@PathVariable Long id)throws IOException{
        Long userId= authHelper.getCurrentUserId();
        String csv= coordinatorService.exportCsv(id,userId);
        byte[] bytes=csv.getBytes();
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment;filename=\"applications-post-"+id+".csv\"")
                .contentType(MediaType.TEXT_PLAIN)
                .contentLength(bytes.length).body(bytes);
    }

    //=================Rounds================================
    @PostMapping("/posts/{postId}/rounds")
    public String upsertRound(@PathVariable Long postId,
                              @RequestParam Long applicationId,
                              @RequestParam int roundNumber,
                              @RequestParam(required = false)String description,
                              @RequestParam(required = false)String date,
                              @RequestParam(required = false)String centre,
                              @RequestParam(required = false)String time,
                              @RequestParam String status,
                              RedirectAttributes ra){
        Long userId= authHelper.getCurrentUserId();
        coordinatorService.upsertRound(postId,userId,applicationId,roundNumber,description,date,centre,time,status);
        ra.addFlashAttribute("success","Round Result saved successfully.");
        return "redirect:/coordinator/posts/"+postId+"/applications";
    }
}
