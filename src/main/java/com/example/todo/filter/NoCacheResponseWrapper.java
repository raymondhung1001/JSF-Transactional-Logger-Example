package com.example.todo.filter;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Response wrapper that ensures no-cache headers are always set,
 * preventing browsers from caching responses even if other code tries to override.
 */
public class NoCacheResponseWrapper extends HttpServletResponseWrapper {
    
    public NoCacheResponseWrapper(HttpServletResponse response) {
        super(response);
        // Set no-cache headers immediately
        setNoCacheHeaders();
    }
    
    /**
     * Set comprehensive no-cache headers that browsers must respect
     */
    private void setNoCacheHeaders() {
        // Standard HTTP/1.1 cache control
        setHeader("Cache-Control", "no-cache, no-store, must-revalidate, max-age=0, private");
        // HTTP/1.0 cache control
        setHeader("Pragma", "no-cache");
        // Expires header (past date)
        setHeader("Expires", "0");
        // Vary header to prevent caching based on different request headers
        setHeader("Vary", "*");
        // Additional header to prevent caching
        setHeader("X-Content-Type-Options", "nosniff");
    }
    
    @Override
    public void setHeader(String name, String value) {
        // Prevent overriding of cache-control headers
        String lowerName = name.toLowerCase();
        if (lowerName.equals("cache-control") || 
            lowerName.equals("pragma") || 
            lowerName.equals("expires")) {
            // Don't allow override of cache headers
            return;
        }
        super.setHeader(name, value);
    }
    
    @Override
    public void addHeader(String name, String value) {
        // Prevent adding conflicting cache-control headers
        String lowerName = name.toLowerCase();
        if (lowerName.equals("cache-control") || 
            lowerName.equals("pragma") || 
            lowerName.equals("expires")) {
            // Don't allow adding conflicting cache headers
            return;
        }
        super.addHeader(name, value);
    }
    
    @Override
    public void setDateHeader(String name, long date) {
        // Prevent overriding expires header
        if ("expires".equalsIgnoreCase(name)) {
            return;
        }
        super.setDateHeader(name, date);
    }
}

