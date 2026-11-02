package com.example.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Value;

@Component
public class ApiAccess implements HandlerInterceptor {
    private final SessionAuth auth;
    private final String frontendOrigin;
    public ApiAccess(SessionAuth auth, @Value("${app.frontend.origin}") String frontendOrigin) {
        this.auth = auth;
        this.frontendOrigin = frontendOrigin;
    }
    @Override public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String method = request.getMethod(), path = request.getRequestURI();
        if ("OPTIONS".equals(method)) return true;
        if (!"GET".equals(method)) {
            String origin = request.getHeader("Origin");
            if (origin != null && !origin.equals("http://localhost:3000") && !origin.equals(frontendOrigin)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Origin is not allowed.");
        }
        if (path.equals("/api/users/login") || path.equals("/api/users/register") || (path.equals("/api/contact") && "POST".equals(method))) return true;
        if ("GET".equals(method) && (path.equals("/api/requests") || path.matches("/api/requests/\\d+"))) return true;
        if (path.startsWith("/api/admin") || path.startsWith("/api/db") || (path.equals("/api/contact") && "GET".equals(method))) auth.require(request, "admin");
        else auth.require(request);
        return true;
    }
}
