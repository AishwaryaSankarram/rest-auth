package com.carma.geoconfig.geoconfig.service;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.carma.geoconfig.geoconfig.model.LoginModel;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
    	Query query =new Query();
    	query.addCriteria(Criteria.where("id").is(id));

        LoginModel user = mongoTemplate.findOne(query,LoginModel.class);

        if (user == null) {
            throw new UsernameNotFoundException(id);
        }
        return new org.springframework.security.core.userdetails.User(user.getId(), user.getPassword(), user.isEnabled(), user.isAccountNonExpired(),
                user.isCredentialsNonExpired(), user.isAccountNonLocked(), getAuthorities(user));

    }

    private List<GrantedAuthority> getAuthorities(LoginModel user) {
        Set<String> roles = user.getRoles();
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        for (String role : roles) {
        	System.out.println("-->"+role);
            authorities.add(new SimpleGrantedAuthority(role));
        }
        return authorities;
    }
}
