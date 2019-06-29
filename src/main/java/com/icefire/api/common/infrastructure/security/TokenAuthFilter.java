package com.icefire.api.common.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class TokenAuthFilter extends OncePerRequestFilter {


	private JwtConfig jwtConfig;

	private static Logger logger = LoggerFactory.getLogger(TokenAuthFilter.class);

	public TokenAuthFilter(JwtConfig jwtConfig) {
		this.jwtConfig = jwtConfig;
	}

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request,@NonNull HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		String header = request.getHeader(jwtConfig.getHeader());
		logger.info("Called doFilterInternal {}",request);
		if (null != header && header.startsWith(jwtConfig.getPrefix())) {
			String token = header.replace(jwtConfig.getPrefix(),"");
			try{
				Claims claims = Jwts.parser()
						.setSigningKey(jwtConfig.getSecret().getBytes())
						.parseClaimsJws(token)
						.getBody();
				String username = claims.getSubject();
				if (username!=null){
					List<String> authorities = (List<String>) claims.get("authorities");
					UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
							username, null, authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
					SecurityContextHolder.getContext().setAuthentication(auth);
				}
			}catch (Exception e){
				e.printStackTrace();
				SecurityContextHolder.clearContext();
			}
		}
		filterChain.doFilter(request, response);
	}
}
