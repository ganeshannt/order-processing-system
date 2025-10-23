package io.ganeshannt.asm.ops.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Adds a requestId to MDC for all logs for this request.
 * Also logs incoming request metadata at INFO level.
 * <p>
 * Keep filter simple so unit-testable; no business logic here.
 */
@Component
public class RequestLoggingFilter implements Filter {

    public static final String REQUEST_ID = "requestId";

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        var request = (HttpServletRequest) req;
        // generate a requestId (production: prefer header from gateway if present)
        String requestId = request.getHeader("X-Request-ID");
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        // populate MDC
        MDC.put(REQUEST_ID, requestId);
        try {
            // minimal structured log
            org.slf4j.LoggerFactory.getLogger(RequestLoggingFilter.class)
                    .info("incoming-request method={} path={} requestId={}", request.getMethod(), request.getRequestURI(), requestId);
            chain.doFilter(req, res);
        } finally {
            MDC.remove(REQUEST_ID);
        }
    }
}
