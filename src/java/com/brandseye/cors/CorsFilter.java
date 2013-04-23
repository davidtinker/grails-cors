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

    private final Map<String, String> optionsHeaders = new LinkedHashMap<String, String>();

    private Pattern allowOriginRegex;
    private String allowOrigin;
    private String exposeHeaders;

    public void init(FilterConfig cfg) throws ServletException {
        String regex = cfg.getInitParameter("allow.origin.regex");
        if (regex != null) {
            allowOriginRegex = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        } else {
            optionsHeaders.put("Access-Control-Allow-Origin", "*");
        }

        optionsHeaders.put("Access-Control-Allow-Headers", "origin, authorization, accept, content-type, x-requested-with");
        optionsHeaders.put("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS");
        optionsHeaders.put("Access-Control-Max-Age", "3600");
        for (Enumeration<String> i = cfg.getInitParameterNames(); i.hasMoreElements(); ) {
            String name = i.nextElement();
            if (name.startsWith("header:")) {
                optionsHeaders.put(name.substring(7), cfg.getInitParameter(name));
            }
        }

        //maintained for backward compatibility on how to set allowOrigin if not
        //using a regex
        allowOrigin = optionsHeaders.get("Access-Control-Allow-Origin");
        //since all methods now go through checkOrigin() to apply the Access-Control-Allow-Origin
        //header, and that header should have a single value of the requesting Origin since
        //Access-Control-Allow-Credentials is always true, we remove it from the options headers
        optionsHeaders.remove("Access-Control-Allow-Origin");

        exposeHeaders = cfg.getInitParameter("expose.headers");
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            HttpServletRequest req = (HttpServletRequest)request;
            HttpServletResponse resp = (HttpServletResponse)response;
            if ("OPTIONS".equals(req.getMethod())) {
                if (checkOrigin(req, resp)) {
                    for (Map.Entry<String, String> e : optionsHeaders.entrySet()) {
                        resp.addHeader(e.getKey(), e.getValue());
                    }
                }
            } else if (checkOrigin(req, resp)) {
                if (exposeHeaders != null) {
                    resp.addHeader("Access-Control-Expose-Headers", exposeHeaders);
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean checkOrigin(HttpServletRequest req, HttpServletResponse resp) {
        String origin = req.getHeader("Origin");
        if (origin == null) {
            //no origin; per W3C spec, terminate further processing for both preflight and actual requests
            return false;
        }

        boolean matches = false;
        //check if using regex to match origin
        if (allowOriginRegex != null) {
            matches = allowOriginRegex.matcher(origin).matches();
        } else if (allowOrigin != null) {
            matches = allowOrigin.equals("*") || allowOrigin.equals(origin);
        }

        if (matches) {
            resp.addHeader("Access-Control-Allow-Origin", origin);
            resp.addHeader("Access-Control-Allow-Credentials", "true");
            return true;
        } else {
            return false;
        }
    }

    public void destroy() {
    }
}
