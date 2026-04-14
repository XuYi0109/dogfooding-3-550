package com.example.ecommerce.user.service;

import com.example.ecommerce.common.exception.BusinessException;
import com.example.ecommerce.security.JwtUtil;
import com.example.ecommerce.user.dto.LoginRequest;
import com.example.ecommerce.user.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsernameOrEmail(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            
            String jwt = jwtUtil.generateToken(userDetails);
            
            log.info("User logged in successfully: {}", userDetails.getUsername());
            
            return LoginResponse.builder()
                    .token(jwt)
                    .type("Bearer")
                    .username(userDetails.getUsername())
                    .build();
                    
        } catch (Exception e) {
            log.warn("Login failed for user: {}", request.getUsernameOrEmail());
            throw new BusinessException("INVALID_CREDENTIALS", "Invalid username or password");
        }
    }

    public void logout() {
        SecurityContextHolder.clearContext();
        log.info("User logged out successfully");
    }
}