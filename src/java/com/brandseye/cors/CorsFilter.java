package com.brandseye.cors;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Adds CORS headers to requests to enable cross-site AJAX calls.
 */
public class CorsFilter implements Filter {

    private final Map<String, String> headers = new LinkedHashMap<String, String>();

    private Pattern allowOriginRegex;
    private String allowOrigin;

    public void init(FilterConfig cfg) throws ServletException {
        String regex = cfg.getInitParameter("allow.origin.regex");
        if (regex != null) {
            allowOriginRegex = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        } else {
            headers.put("Access-Control-Allow-Origin", "*");
        }

        headers.put("Access-Control-Allow-Credentials", "true");
        headers.put("Access-Control-Allow-Headers", "origin, authorization, accept, content-type, x-requested-with");
        headers.put("Access-Control-Max-Age", "3600");

        for (Enumeration<String> i = cfg.getInitParameterNames(); i.hasMoreElements(); ) {
            String name = i.nextElement();
            if (name.startsWith("header:")) {
                headers.put(name.substring(7), cfg.getInitParameter(name));
            }
        }

        allowOrigin = headers.get("Access-Control-Allow-Origin");
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            HttpServletRequest req = (HttpServletRequest)request;
            HttpServletResponse resp = (HttpServletResponse)response;
            if ("OPTIONS".equals(req.getMethod())) {
                if (allowOriginRegex == null || checkOrigin(req, resp)) {
                    for (Map.Entry<String, String> e : headers.entrySet()) {
                        resp.addHeader(e.getKey(), e.getValue());
                    }
                }
            } else {
                if (allowOriginRegex != null) {
                    checkOrigin(req, resp);
                } else {
                    resp.addHeader("Access-Control-Allow-Origin", allowOrigin);
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean checkOrigin(HttpServletRequest req, HttpServletResponse resp) {
        String origin = req.getHeader("Origin");
        if (origin != null && allowOriginRegex.matcher(origin).matches()) {
            resp.addHeader("Access-Control-Allow-Origin", origin);
            return true;
        }
        return false;
    }

    public void destroy() {
    }
}
