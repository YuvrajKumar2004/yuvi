package com.yuvraj.controller;


import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error",required = false)String error,
            @RequestParam(value = "logout",required = false)String logout,
            Model model
    ){
        if(error !=null)model.addAttribute("error","Invalid Credentials or account locked");
        if(logout !=null)model.addAttribute("logout","You have been logged out");
        return "login";
    }
    @GetMapping("/")
    public String root(){
        return "redirect:/login";
    }
}
