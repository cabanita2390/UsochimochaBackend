package com.app.usochicamochabackend.utils;

import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.List;

public class TestSecurityUtils {

    public static void setUpSecurityContext(Long userId, String username, String role) {
        UserPrincipal userPrincipal = new UserPrincipal(userId, username);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userPrincipal, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    public static void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }
}
