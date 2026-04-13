package com.yuvraj.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

public class RoleBasedSuccessHandler implements AuthenticationSuccessHandler {


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Collection<? extends GrantedAuthority>authorities=authentication.getAuthorities();
        String redirectUrl1="/login";

        for(GrantedAuthority authority:authorities){
            String role=authority.getAuthority();
            switch (role){
                case "ROLE_STUDENT" ->redirectUrl1="/student/dashboard";
                case "ROLE_COORDINATOR" ->redirectUrl1="/coordinator/dashboard";
                case "ROLE_CCD_ADMIN"  ->redirectUrl1="/ccd/dashboard";
                case "ROLE_CCD_MEMBER" ->redirectUrl1="/ccd/dashboard";
            }
        }
        response.sendRedirect(redirectUrl1);
    }
}
