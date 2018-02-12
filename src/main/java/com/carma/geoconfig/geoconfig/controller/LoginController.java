package com.carma.geoconfig.geoconfig.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carma.geoconfig.geoconfig.model.LoginModel;
import com.carma.geoconfig.geoconfig.service.UserLoginService;

@RestController
@RequestMapping("/user")
public class LoginController {
	@Autowired
	UserLoginService userLoginService;
	
	
	@PostMapping("/login")
	public LoginModel getUserDetail(@RequestBody LoginModel loginModel) {
		return userLoginService.getUserdetail(loginModel);
	}
    
	@PostMapping("/create")
	public String createUsers(@RequestBody LoginModel loginModel) {
		 userLoginService.createUser(loginModel);		
		 return "Successfully Created";
		
	}
	

}
