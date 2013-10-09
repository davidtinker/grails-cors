package com.brandseye.cors;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Prevents authentication of HTTP OPTIONS preflight requests which would otherwise break CORS in most browsers.
 */
public class CorsCompatibleBasicAuthenticationEntryPoint extends BasicAuthenticationEntryPoint {

    public CorsCompatibleBasicAuthenticationEntryPoint() {
        super();
    }

    @Override
    public void commence(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException authException) throws IOException, ServletException {
        if(!request.getMethod().equals("OPTIONS")) {
            super.commence(request, response, authException);
        }
    }

}