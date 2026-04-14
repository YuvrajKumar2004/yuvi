package com.yuvraj.controller;

import com.yuvraj.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthHelper {
    private final UserRepository userRepository;
            public Long getCurrentUserId(){
                Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
                String loginId=authentication.getName();
                        return userRepository.findByLoginId(loginId)
                                .orElseThrow(()->new RuntimeException("Authenticated user not found in DB"))
                                .getId();


            }
}
