package com.fleet.maintenance.auth.service;

import static com.fleet.maintenance.TestFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.fleet.maintenance.auth.repository.UserRepository;
import com.fleet.maintenance.shared.dto.Role;
import com.fleet.maintenance.shared.security.UserPrincipal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class DynamoDbUserDetailsServiceTest {
    @Mock
    UserRepository userRepository;

    @Test
    void should_ReturnPrincipal_When_UserExists() {
        when(userRepository.findByUsername("provider"))
                .thenReturn(Optional.of(user("provider", "hash", Role.ROLE_PROVIDER)));
        DynamoDbUserDetailsService service = new DynamoDbUserDetailsService(userRepository);

        UserPrincipal principal = (UserPrincipal) service.loadUserByUsername("provider");

        assertThat(principal.id()).isEqualTo("provider");
        assertThat(principal.role()).isEqualTo(Role.ROLE_PROVIDER);
    }

    @Test
    void should_ThrowError_When_UserIsNotFound() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());
        DynamoDbUserDetailsService service = new DynamoDbUserDetailsService(userRepository);

        assertThatThrownBy(() -> service.loadUserByUsername("missing"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found: missing");
    }
}
