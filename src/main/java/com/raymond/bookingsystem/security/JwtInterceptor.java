package com.raymond.bookingsystem.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtService jwtService;
    public JwtInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(401);
            return false;
        }
        String token = authHeader.substring(7);
        try {
            String email = jwtService.validateAndGetEmail(token);
            request.setAttribute("customerEmail", email);
            return true;
        } catch (Exception e) {
            response.setStatus(401);
            return false;
        }
    }

}
