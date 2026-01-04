package com.grabit.handler;

import com.grabit.Utilities.Utility;
import com.grabit.enums.CommonErrors;
import com.grabit.exception.APIError;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Log4j2
public class CustomAuthHandler implements AuthenticationEntryPoint, AccessDeniedHandler {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        if(Utility.isNullOrEmpty(request.getAttribute("responseWriterFlag"))){
            log.warn("Remote Host : "+request.getRemoteHost());
            log.warn("Remote Address : "+request.getRemoteAddr());
            log.info("Authentication Error : "+authException);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(Utility.toJson(new APIError(CommonErrors.AUTHENTICATION_ERROR.toString(),CommonErrors.AUTHENTICATION_ERROR.getMessage())));
        }
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.info("AccessDeniedException --> "+accessDeniedException.getMessage());
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write(Utility.toJson(new APIError(CommonErrors.Forbidden.toString(),CommonErrors.Forbidden.getMessage())));
    }
}
