package com.final_project.flight_booking.services;

import com.final_project.flight_booking.models.User;
import com.final_project.flight_booking.models.UserRole;
import com.final_project.flight_booking.repositories.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private AppUserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(usernameOrEmail);
        
        if (user == null) {
            user = userRepository.findByEmail(usernameOrEmail);
        }

        if (user == null) {
            throw new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail);
        }

        if (!user.getActivated()) {
            throw new UsernameNotFoundException("User account is not activated: " + usernameOrEmail);
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        Set<UserRole> roles = user.getRoles();
        if (roles != null) {
            for (UserRole userRole : roles) {
                authorities.add(new SimpleGrantedAuthority(userRole.getRole().getName()));
            }
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}
