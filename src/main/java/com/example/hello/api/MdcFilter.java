package com.example.hello.api;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
public class MdcFilter extends OncePerRequestFilter {

    private static final String HDR_REQUEST_ID = "X-Request-Id";
    private static final String HDR_USER_ID = "X-User-Id";
    private static final String HDR_TENANT_ID = "X-Tenant-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestId = Optional.ofNullable(request.getHeader(HDR_REQUEST_ID))
                .filter(s -> !s.isBlank())
                .orElse(UUID.randomUUID().toString());

        String userId = Optional.ofNullable(request.getHeader(HDR_USER_ID)).filter(s -> !s.isBlank()).orElse("anonymous");
        String tenantId = Optional.ofNullable(request.getHeader(HDR_TENANT_ID)).filter(s -> !s.isBlank()).orElse("default");

        MDC.put("requestId", requestId);
        MDC.put("userId", userId);
        MDC.put("tenantId", tenantId);

        response.setHeader(HDR_REQUEST_ID, requestId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}