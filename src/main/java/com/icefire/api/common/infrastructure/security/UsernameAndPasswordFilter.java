package com.icefire.api.common.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icefire.api.common.application.dto.AuthResult;
import com.icefire.api.user.domain.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

public class UsernameAndPasswordFilter extends UsernamePasswordAuthenticationFilter {

    private static Logger logger = LoggerFactory.getLogger(UsernameAndPasswordFilter.class);
    @Autowired
    private JwtConfig jwtConfig;
    @Autowired
    private AuthenticationManager manager;

    UsernameAndPasswordFilter(JwtConfig jwtConfig, AuthenticationManager manager) {
        this.jwtConfig = jwtConfig;
        this.manager = manager;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {

            User auth = new ObjectMapper().readValue(request.getInputStream(), User.class);

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    auth.getUsername(), auth.getPassword(), Collections.emptyList());

            return manager.authenticate(authToken);

        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        String token = jwtConfig.generateToken(authResult.getName());

        response.addHeader(jwtConfig.getHeader(), jwtConfig.getPrefix() + token);
        response.addHeader(jwtConfig.getExpires(), String.valueOf(jwtConfig.getExpiration()));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        PrintWriter writer = response.getWriter();
        AuthResult result = new AuthResult();
        result.setError(false);
        result.setMessage("Login successful!");
        logger.info("{} authenticated", authResult.getName());
        writer.print(new ObjectMapper().writeValueAsString(result));
        writer.flush();
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        PrintWriter writer = response.getWriter();
        AuthResult result = new AuthResult();
        result.setError(true);
        result.setMessage("Login failed!");
        logger.info("{} failed", request);
        writer.print(new ObjectMapper().writeValueAsString(result));
        writer.flush();
    }

}
