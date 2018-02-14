package com.carma.geoconfig.geoconfig.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.carma.geoconfig.geoconfig.service.CustomUserDetailsService;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

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

		http.httpBasic().and().authorizeRequests().antMatchers("/granular/**")
				.hasRole("USER").antMatchers("/**").hasRole("ADMIN").and()
				.csrf().disable().headers().frameOptions().disable();
	}

	public void configure(WebSecurity web) throws Exception {
		System.out.println("------------------->entered web");

//		web.ignoring().antMatchers("/granular/**");
		web.ignoring().antMatchers("/user/**");


	}
	
	

}
