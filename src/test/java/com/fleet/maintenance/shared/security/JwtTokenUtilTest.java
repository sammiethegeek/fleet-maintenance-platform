package com.fleet.maintenance.shared.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fleet.maintenance.shared.dto.Role;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

class JwtTokenUtilTest {
    private static final String SECRET = "mysecretkey123456789012345678901234567890";

    @Test
    void should_GenerateAndParseToken_When_TokenIsValid() {
        JwtTokenUtil jwtTokenUtil = new JwtTokenUtil(SECRET, 60_000);

        String token = jwtTokenUtil.generateToken("coordinator", "Coordinator", Role.ROLE_COORDINATOR);
        UserPrincipal principal = jwtTokenUtil.parseToken(token);

        assertThat(principal.id()).isEqualTo("coordinator");
        assertThat(principal.name()).isEqualTo("Coordinator");
        assertThat(principal.role()).isEqualTo(Role.ROLE_COORDINATOR);
    }

    @Test
    void should_ThrowError_When_TokenIsExpired() throws InterruptedException {
        JwtTokenUtil jwtTokenUtil = new JwtTokenUtil(SECRET, 1);
        String token = jwtTokenUtil.generateToken("provider", "Provider", Role.ROLE_PROVIDER);

        Thread.sleep(5);

        assertThatThrownBy(() -> jwtTokenUtil.parseToken(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void should_ThrowError_When_TokenFormatIsInvalid() {
        JwtTokenUtil jwtTokenUtil = new JwtTokenUtil(SECRET, 60_000);

        assertThatThrownBy(() -> jwtTokenUtil.parseToken("not-a-jwt"))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void should_ThrowError_When_TokenWasSignedWithDifferentSecret() {
        JwtTokenUtil issuer = new JwtTokenUtil(SECRET, 60_000);
        JwtTokenUtil verifier = new JwtTokenUtil("anothersecretkey1234567890123456789012345", 60_000);
        String token = issuer.generateToken("provider", "Provider", Role.ROLE_PROVIDER);

        assertThatThrownBy(() -> verifier.parseToken(token))
                .isInstanceOf(JwtException.class);
    }
}
