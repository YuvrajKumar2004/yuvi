package com.yuvraj.security;

import com.yuvraj.model.User;
import com.yuvraj.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserdetailsArgsConstructor implements UserDetailsService {
    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername( String loginId) throws UsernameNotFoundException {
       User user= userRepository.findByLoginId(loginId).
               orElseThrow(()->new UsernameNotFoundException("User not found with loginId "+loginId));

       if(user.isLocked()){
           throw new DisabledException("Account is locked.Contact your CCD Admin");
       }
       String roleAuthority="ROLE_"+user.getRole().name();
       return new org.springframework.security.core.userdetails.User(
               user.getLoginId(),
               user.getPasswordHash(),
//               !user.isLocked(),
               true,
               true,
               true,
               !user.isLocked(),
//               true,
               List.of(new SimpleGrantedAuthority(roleAuthority))
       );
    }
}
