package com.fleet.maintenance.auth.service;

import com.fleet.maintenance.auth.dto.LoginRequest;
import com.fleet.maintenance.auth.dto.LoginResponse;
import com.fleet.maintenance.auth.entity.UserAccount;
import com.fleet.maintenance.auth.repository.UserRepository;
import com.fleet.maintenance.shared.exception.AuthenticationException;
import com.fleet.maintenance.shared.security.JwtTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final JwtTokenUtil jwtTokenUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final LoginAttemptService loginAttemptService;

    public AuthService(
            JwtTokenUtil jwtTokenUtil,
            PasswordEncoder passwordEncoder,
            UserRepository userRepository,
            LoginAttemptService loginAttemptService
    ) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.loginAttemptService = loginAttemptService;
    }

    public LoginResponse login(LoginRequest request) {
        if (loginAttemptService.isLocked(request.username())) {
            throw new AuthenticationException("Account is temporarily locked. Try again later.");
        }
        UserAccount user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> {
                    loginAttemptService.recordFailure(request.username());
                    return new AuthenticationException("Invalid username or password");
                });
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            loginAttemptService.recordFailure(request.username());
            throw new AuthenticationException("Invalid username or password");
        }
        loginAttemptService.recordSuccess(request.username());
        log.info("User {} logged in", request.username());
        return new LoginResponse(user.getUsername(), jwtTokenUtil.generateToken(user.getUsername(), user.getName(), user.getRole()));
    }
}
