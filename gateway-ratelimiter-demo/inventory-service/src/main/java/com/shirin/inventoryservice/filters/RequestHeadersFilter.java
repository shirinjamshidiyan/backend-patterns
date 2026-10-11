package com.shirin.inventoryservice.filters;

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
public class RequestHeadersFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-Correlation-ID";
    private static final String MDC_KEY = "correlationId";
    private static final String CALLER = "X-Calling-Service";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String corrIdHeader = request.getHeader(HEADER);
        if(corrIdHeader == null || corrIdHeader.isBlank())
           corrIdHeader=  UUID.randomUUID().toString();


        String callerHeader = request.getHeader(CALLER);
        if(callerHeader == null || callerHeader.isBlank())
            callerHeader=  "UNKNOWN";

        try {
            MDC.put(MDC_KEY, corrIdHeader);
            MDC.put(CALLER, callerHeader);

            response.setHeader(HEADER, corrIdHeader);

            filterChain.doFilter(request, response);

        }
        finally {

            MDC.remove(MDC_KEY);
            MDC.remove(CALLER);
        }


    }
}
