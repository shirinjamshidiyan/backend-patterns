package com.shirin.orderservice.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-Correlation-ID";
    private static final String MDC_KEY = "correlationId";
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String corrIdHeader = request.getHeader(HEADER);
        if(corrIdHeader == null || corrIdHeader.isBlank())
           corrIdHeader=  UUID.randomUUID().toString();

        try {
            MDC.put(MDC_KEY, corrIdHeader);

            response.setHeader(HEADER, corrIdHeader);

            filterChain.doFilter(request, response);
        }
        finally {

            MDC.remove(MDC_KEY);
        }


    }
}
