package com.fleet.maintenance.auth.service;

import static com.fleet.maintenance.TestFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fleet.maintenance.auth.dto.LoginRequest;
import com.fleet.maintenance.auth.repository.UserRepository;
import com.fleet.maintenance.shared.dto.Role;
import com.fleet.maintenance.shared.exception.AuthenticationException;
import com.fleet.maintenance.shared.security.JwtTokenUtil;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    JwtTokenUtil jwtTokenUtil;
    @Mock
    UserRepository userRepository;

    PasswordEncoder passwordEncoder;
    AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(jwtTokenUtil, passwordEncoder, userRepository,
                new LoginAttemptService(5, java.time.Duration.ofMinutes(15), java.time.Clock.systemUTC()));
    }

    @Test
    void should_ReturnToken_When_CredentialsAreValid() {
        String hash = passwordEncoder.encode("coordinator123");
        when(userRepository.findByUsername("coordinator")).thenReturn(Optional.of(user("coordinator", hash, Role.ROLE_COORDINATOR)));
        when(jwtTokenUtil.generateToken("coordinator", "Coordinator", Role.ROLE_COORDINATOR)).thenReturn("jwt-token");

        var response = authService.login(new LoginRequest("coordinator", "coordinator123"));

        assertThat(response.username()).isEqualTo("coordinator");
        assertThat(response.token()).isEqualTo("jwt-token");
    }

    @Test
    void should_ThrowError_When_PasswordIsWrong() {
        when(userRepository.findByUsername("coordinator"))
                .thenReturn(Optional.of(user("coordinator", passwordEncoder.encode("coordinator123"), Role.ROLE_COORDINATOR)));

        assertThatThrownBy(() -> authService.login(new LoginRequest("coordinator", "bad-password")))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid username or password");
    }

    @Test
    void should_ThrowError_When_UserDoesNotExist() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("missing", "password")))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid username or password");
    }

    @Test
    void should_HashPasswordsWithBCrypt_When_EncodingPassword() {
        String hash = passwordEncoder.encode("secret");

        assertThat(hash).startsWith("$2");
        assertThat(hash).isNotEqualTo("secret");
        assertThat(passwordEncoder.matches("secret", hash)).isTrue();
    }

    @Test
    void should_ThrowError_When_UsernameIsNull() {
        assertThatThrownBy(() -> authService.login(new LoginRequest(null, "password")))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void should_ThrowError_When_PasswordIsNull() {
        when(userRepository.findByUsername("coordinator"))
                .thenReturn(Optional.of(user("coordinator", passwordEncoder.encode("coordinator123"), Role.ROLE_COORDINATOR)));

        assertThatThrownBy(() -> authService.login(new LoginRequest("coordinator", null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_HandleConcurrentLoginAttempts_When_CredentialsAreValid() throws Exception {
        int attempts = 8;
        String hash = passwordEncoder.encode("coordinator123");
        when(userRepository.findByUsername("coordinator"))
                .thenReturn(Optional.of(user("coordinator", hash, Role.ROLE_COORDINATOR)));
        when(jwtTokenUtil.generateToken(eq("coordinator"), eq("Coordinator"), eq(Role.ROLE_COORDINATOR))).thenReturn("jwt-token");
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(attempts);
        var executor = Executors.newFixedThreadPool(attempts);

        for (int i = 0; i < attempts; i++) {
            executor.submit(() -> {
                try {
                    start.await();
                    assertThat(authService.login(new LoginRequest("coordinator", "coordinator123")).token()).isEqualTo("jwt-token");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        done.await();
        executor.shutdownNow();

        verify(userRepository, times(attempts)).findByUsername("coordinator");
    }

    @Test
    void should_LockAccountTemporarily_When_FailedAttemptsReachThreshold() {
        LoginAttemptService loginAttemptService = new LoginAttemptService(2, java.time.Duration.ofMinutes(15), java.time.Clock.systemUTC());
        authService = new AuthService(jwtTokenUtil, passwordEncoder, userRepository, loginAttemptService);
        when(userRepository.findByUsername("coordinator"))
                .thenReturn(Optional.of(user("coordinator", passwordEncoder.encode("coordinator123"), Role.ROLE_COORDINATOR)));

        assertThatThrownBy(() -> authService.login(new LoginRequest("coordinator", "bad-password")))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid username or password");
        assertThatThrownBy(() -> authService.login(new LoginRequest("coordinator", "bad-password")))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid username or password");
        assertThatThrownBy(() -> authService.login(new LoginRequest("coordinator", "coordinator123")))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Account is temporarily locked. Try again later.");
    }
}
