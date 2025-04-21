package com.example.demo.config;

import java.io.IOException;
import java.util.Date;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.example.demo.domain.response.ResResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper mapper;

    public CustomAccessDeniedHandler(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {

        response.setStatus(HttpStatus.FORBIDDEN.value());
        // Support Vietnamese
        response.setContentType("application/json;charset=UTF-8");

        ResResponse<Object> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.FORBIDDEN.value());
        res.setError("Access denied");
        res.setTimestamp(new Date());
        res.setPath(request.getRequestURI());
        res.setMessage("You do not have permission to access this resource");

        mapper.writeValue(response.getWriter(), res);
    }

}
