package com.carma.geoconfig.geoconfig.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.carma.geoconfig.geoconfig.model.LoginModel;
import com.carma.geoconfig.geoconfig.model.MongoTripDataModel;

@Service
public class UserLoginService {
	@Autowired
	MongoTemplate mongoTemplate;

	public void createUser(LoginModel loginModel) {
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		loginModel.setPassword(passwordEncoder.encode(loginModel.getPassword()));
		loginModel.setAccountNonExpired(true);
		loginModel.setAccountNonLocked(true);
		loginModel.setCredentialsNonExpired(true);
		loginModel.setEnabled(true);
		loginModel.getRoles().clear();
		loginModel.addRole("ROLE_USER");
		mongoTemplate.save(loginModel);
	}

	public LoginModel getUserdetail(LoginModel loginModel) {
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		Query query = new Query();
		query.addCriteria(Criteria.where("username").is(loginModel.getUsername()));
		LoginModel loginModelResp = mongoTemplate.findOne(query, LoginModel.class);
		if (loginModelResp != null) {
			if (passwordEncoder.matches(loginModel.getPassword(), loginModelResp.getPassword())) {
				return loginModelResp;
			} else {
				throw new SecurityException("Password  not matched");

			}
		} else {
			throw new SecurityException("User not found");
		}

	}
	
	public long getAndUpdateTripNo(long carId) {
		Query query = new Query();
		query.addCriteria(Criteria.where("carId").is(carId));
		MongoTripDataModel mongoTripDataModel=mongoTemplate.findOne(query, MongoTripDataModel.class);
		
		if (mongoTripDataModel == null) {
			mongoTemplate.save(new MongoTripDataModel(carId,1));
			return 1;
		}
//		mongoTripDataModel.setTripNo(mongoTripDataModel.getTripNo()+1);
		
		mongoTemplate.updateFirst(query, Update.update("tripNo", mongoTripDataModel.getTripNo()+1),MongoTripDataModel.class);
		
		return mongoTripDataModel.getTripNo()+1;
	}

}
