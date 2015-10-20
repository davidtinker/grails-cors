/*
 * Copyright 2013 BrandsEye (http://www.brandseye.com/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.brandseye.cors;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Adds CORS headers to requests to enable cross-site AJAX calls.
 */
public class CorsFilter implements Filter {

    private static Logger logger = LoggerFactory.getLogger(CorsFilter.class);

    private final Map<String, String> optionsHeaders = new LinkedHashMap<String, String>();

    private Pattern allowOriginRegex;
    private String allowOrigin;
    private String exposeHeaders;
    private boolean enableLogging;

    public void init(FilterConfig cfg) throws ServletException {
        optionsHeaders.put("Access-Control-Allow-Headers", "origin, authorization, accept, content-type, x-requested-with");
        optionsHeaders.put("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS");
        optionsHeaders.put("Access-Control-Max-Age", "3600");
        for (Enumeration<String> i = cfg.getInitParameterNames(); i.hasMoreElements(); ) {
            String name = i.nextElement();
            if (name.startsWith("header:")) {
                optionsHeaders.put(name.substring(7), cfg.getInitParameter(name));
            }
        }

        String regex = cfg.getInitParameter("allow.origin.regex");
        if (regex != null) {
            allowOriginRegex = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        }
        allowOrigin = optionsHeaders.remove("Access-Control-Allow-Origin");

        exposeHeaders = cfg.getInitParameter("expose.headers");

        String logging = cfg.getInitParameter("enable.logging");
        if(logging != null && logging.equalsIgnoreCase("true")){
            enableLogging = true;
        } else {
            enableLogging = false;
        }
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

                    // We need to return here since we don't want the chain to further process
                    // a preflight request since this can lead to unexpected processing of the preflighted
                    // request or a 405 - method not allowed in Grails 2.3
                    return;

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
            if(enableLogging){
                logger.error("No origin header for request");
                logger.error(requestToString(req));
            }
            return false;
        }

        boolean matches;
        if (allowOriginRegex != null) {
            matches = allowOriginRegex.matcher(origin).matches();
        } else {
            matches = allowOrigin == null || allowOrigin.equals("*") || allowOrigin.equals(origin);
        }

        if (matches) {
            // if no 'Access-Control-Allow-Origin' specified in cors.headers then echo back Origin
            resp.addHeader("Access-Control-Allow-Origin", allowOrigin == null ? origin : allowOrigin);
            resp.addHeader("Access-Control-Allow-Credentials", "true");
            return true;
        } else {
            if(enableLogging){
                logger.error("Origin header is present but does not match to allowed origin");
                logger.error(requestToString(req));
            }
            return false;
        }
    }

    private String requestToString(HttpServletRequest req){
        String reqStr = "==================Request details start=================\r\n";
        reqStr += requestUrl(req);
        reqStr += requestHeaders(req);
        reqStr += "==================Request details end=================\n";
        return reqStr;
    }

    private String requestUrl(HttpServletRequest req) {
        String reqUrl = req.getRequestURL().toString();
        String queryString = req.getQueryString();
        if (queryString != null) {
            reqUrl += "?"+queryString;
        }
        return "Url: " + reqUrl + "\r\n";
    }

    private String requestHeaders(HttpServletRequest req) {
        String reqHeaders = "Headers:\n";
        Enumeration<String> headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            reqHeaders += "\t" + headerName + ": ";
            String headerValue = req.getHeader(headerName);
            reqHeaders += headerValue + "\r\n";
        }
        return reqHeaders;
    }

    public void destroy() {
    }
}
