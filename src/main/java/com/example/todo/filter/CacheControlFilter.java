package com.example.todo.filter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter to prevent caching of pages to avoid security issues with browser back/forward buttons.
 * This ensures that pages are always fetched from the server, not from browser cache.
 */
@WebFilter(urlPatterns = {"*.xhtml", "/"})
public class CacheControlFilter implements Filter {
    private static final Logger logger = LogManager.getLogger(CacheControlFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("CacheControlFilter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Wrap response to ensure no-cache headers are always set and can't be overridden
        NoCacheResponseWrapper wrappedResponse = new NoCacheResponseWrapper(httpResponse);
        
        // For HTTPS, also set security headers
        if (httpRequest.isSecure()) {
            wrappedResponse.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        }

        chain.doFilter(request, wrappedResponse);
    }

    @Override
    public void destroy() {
        logger.info("CacheControlFilter destroyed");
    }
}

