package com.carma.geoconfig.geoconfig.security;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.carma.geoconfig.geoconfig.service.CustomUserDetailsService;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

	 @Autowired
     private CustomUserDetailsService customUserDetailsService;
	
	// Authentication : User --> Roles
	protected void configure(AuthenticationManagerBuilder auth)
			throws Exception {
		System.out.println("------------------->entered auth");
		
		auth.userDetailsService(customUserDetailsService).passwordEncoder(new BCryptPasswordEncoder());
	}

	protected void configure(HttpSecurity http) throws Exception {
		System.out.println("------------------->entered http");

		http.httpBasic().and().authorizeRequests().antMatchers(HttpMethod.OPTIONS).permitAll().and().authorizeRequests().antMatchers("/granular/**")
				.hasRole("USER").antMatchers("/**").hasRole("ADMIN").and()
				.csrf().disable().headers().frameOptions().disable();
	}

	public void configure(WebSecurity web) throws Exception {
		System.out.println("------------------->entered web");

		web.ignoring().antMatchers("/granular/executeCommands");
		web.ignoring()
				.antMatchers("/user/**")
				.antMatchers("/resources/**", "/static/**", "/css/**", "/js/**", "/images/**");



	}
	

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
