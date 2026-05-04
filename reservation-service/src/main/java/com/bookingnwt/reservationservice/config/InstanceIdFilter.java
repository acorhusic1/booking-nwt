package com.bookingnwt.reservationservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Stamps every response with X-Instance-Id so the Task 4 load-balancing
 * script can show how many of the 100 requests landed on which pod.
 */
@Component
public class InstanceIdFilter extends OncePerRequestFilter {

    private final String instanceId;

    public InstanceIdFilter(@Value("${spring.application.name:reservation-service}") String app,
                            @Value("${server.port:8083}") String port) {
        this.instanceId = app + "@" + resolveHost() + ":" + port;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        response.setHeader("X-Instance-Id", instanceId);
        filterChain.doFilter(request, response);
    }

    private String resolveHost() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }
}
