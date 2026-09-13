package com.traazu.auth_service.services.auth;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.traazu.auth_service.domain.entities.BaseUser;
import com.traazu.auth_service.repositories.StaffRepository;
import com.traazu.auth_service.repositories.UserRepository;
import com.traazu.auth_service.security.CustomUserDetails;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final StaffRepository staffRepository;

    public CustomUserDetailsService(UserRepository userRepository, StaffRepository staffRepository) {
        this.userRepository = userRepository;
        this.staffRepository = staffRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        BaseUser baseUser = staffRepository.findByEmail(username).<BaseUser>map(u -> u)
                .or(() -> userRepository.findByEmail(username).map(s -> (BaseUser) s))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
                
        return new CustomUserDetails(baseUser);
    }
    
}
