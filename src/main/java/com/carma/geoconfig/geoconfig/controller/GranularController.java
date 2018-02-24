package com.carma.geoconfig.geoconfig.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.carma.geoconfig.geoconfig.model.MongoGranularModel;
import com.carma.geoconfig.geoconfig.service.GenerateGranularService;
import com.carma.geoconfig.geoconfig.service.utils.SshCommandUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/granular")
public class GranularController {
    private static final Logger log = LoggerFactory.getLogger(GranularController.class);

	@Autowired
	GenerateGranularService generateGranularService;
	
	@PostMapping("/createGranularPoints")
	public MongoGranularModel createGranularPoints(@RequestBody MongoGranularModel mongoGranularModel)
			throws IOException, ParseException {
		 log.info("requst body : "+new ObjectMapper().writeValueAsString(mongoGranularModel));
	     /*getting authorized user detail*/
         Authentication auth = SecurityContextHolder.getContext().getAuthentication();
         User user =null;
         if(auth!=null) {
        	  user = (User) auth.getPrincipal();
         }
         //System.out.println("user Object==> "+new ObjectMapper().writeValueAsString(user));
		return generateGranularService.createMultiPoints(mongoGranularModel,user);
	}
	
	
	@GetMapping("/getGranularPoints/{id}")
	public List<MongoGranularModel> getGranularPointsById(@PathVariable(required =true) String id,@RequestParam(name="page",defaultValue="0") int page,@RequestParam(name="size",defaultValue="10") int size) throws IOException, ParseException {
		/*getting authorized user detail*/
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user =null;
        if(auth!=null) {
       	  user = (User) auth.getPrincipal();
        }
		return generateGranularService.getMultiPointsById(id, user,page,size);
	}
	
	@PostMapping("/getGranularPoints/filter/{id}")
	public List<MongoGranularModel> getGranularPointsByFilter(@PathVariable(required =true) String id, @RequestBody(required=false) Map<String, Object> params ,@RequestParam(name="page",defaultValue="0") int page,@RequestParam(name="size",defaultValue="5") int size) throws IOException, ParseException {
		/*getting authorized user detail*/
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user =null;
        if(auth!=null) {
       	  user = (User) auth.getPrincipal();
        }
		return generateGranularService.getMultiPointsByFilter(id, user,page,size,params);
	}
	
	
	@DeleteMapping("/deleteCarDetails/{id}")
	public ResponseEntity<String> deleteCarDetails(@PathVariable(required =true) String id, @RequestParam(required=true) long carId ) throws IOException, ParseException {
		/*getting authorized user detail*/
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user =null;
        if(auth!=null) {
       	  user = (User) auth.getPrincipal();
        }
        try {
        	generateGranularService.deleteCarDetails(id,carId, user);
        	return ResponseEntity.ok("successfully deleted");
        }catch (Exception e) {
			// TODO: handle exception
  		  return ResponseEntity.notFound().build();

		}
	}

	@PostMapping("/executeCommands")
	public String executeCommands(@RequestBody(required=true) List<MongoGranularModel> mongoGranularModels) throws IOException, ParseException {
		/*getting authorized user detail*/
      /*  Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user =null;
        if(auth!=null) {
       	  user = (User) auth.getPrincipal();
        }*/
        log.info("payload==>"+new ObjectMapper().writeValueAsString(mongoGranularModels));
		return generateGranularService.executeCommands(mongoGranularModels);
	}
	
	
}
