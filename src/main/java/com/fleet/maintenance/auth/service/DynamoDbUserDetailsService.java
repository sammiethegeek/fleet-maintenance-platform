package com.fleet.maintenance.auth.service;

import com.fleet.maintenance.auth.repository.UserRepository;
import com.fleet.maintenance.shared.security.UserPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DynamoDbUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public DynamoDbUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .map(user -> new UserPrincipal(user.getUsername(), user.getName(), user.getRole()))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
