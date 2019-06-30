package com.icefire.api.common.infrastructure.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletResponse;

@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true)
@EnableConfigurationProperties
public class AppTokenConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private JwtConfig jwtConfig;

    @Autowired
    UserDetailsService userDetailsService;

    private static Logger logger = LoggerFactory.getLogger(AppTokenConfig.class);

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        logger.info("Called");
        http.cors()
                .and()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .exceptionHandling().authenticationEntryPoint((req, rsp, e) -> {
            //e.printStackTrace();
            logger.info("Error {}", e.getMessage());
            rsp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }).and()
                .addFilter(new UsernameAndPasswordFilter(jwtConfig, authenticationManager()))
                .addFilterAfter(new TokenAuthFilter(jwtConfig), UsernamePasswordAuthenticationFilter.class)
                // authorization requests config
                .authorizeRequests()
                //needed for CORS config in Javascript
                .antMatchers(HttpMethod.OPTIONS).permitAll()
                // allow all who are accessing authentication
                .antMatchers(HttpMethod.POST, jwtConfig.getLogin()).permitAll()
                .antMatchers(HttpMethod.GET, jwtConfig.getRefresh()).permitAll()
                .antMatchers("/h2-console").permitAll()
                .antMatchers(HttpMethod.POST, "/api/user/register").permitAll()
                // Any other request must be authenticated
                .anyRequest().authenticated();
        http.headers().frameOptions().disable();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtConfig jwtConfig(){
        return new JwtConfig();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }
}
