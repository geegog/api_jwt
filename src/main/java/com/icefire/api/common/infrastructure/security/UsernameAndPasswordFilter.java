package com.icefire.api.common.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icefire.api.common.application.dto.AuthResult;
import com.icefire.api.user.domain.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;

public class UsernameAndPasswordFilter extends UsernamePasswordAuthenticationFilter {

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
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
		String token = jwtConfig.generateToken(authResult.getName());

		response.addHeader(jwtConfig.getHeader(), jwtConfig.getPrefix() + token);
		response.addHeader(jwtConfig.getExpires(), String.valueOf(jwtConfig.getExpiration()));
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer = response.getWriter();
		AuthResult result = new AuthResult();
		result.setError(false);
		result.setMessage("Login successful!");
		writer.print(new ObjectMapper().writeValueAsString(result));
		writer.flush();
	}

	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.setStatus(400);
		PrintWriter writer = response.getWriter();
		AuthResult result = new AuthResult();
		result.setError(true);
		result.setMessage("Login failed");
		writer.print(new ObjectMapper().writeValueAsString(result));
		writer.flush();
	}

}
