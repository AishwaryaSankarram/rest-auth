package com.carma.geoconfig.geoconfig.controller;

import java.io.IOException;

import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carma.geoconfig.geoconfig.model.MongoGranularModel;
import com.carma.geoconfig.geoconfig.service.GenerateGranularService;

@RestController
@RequestMapping("/granular")
public class GranularController {

	@Autowired
	GenerateGranularService generateGranularService;
	
	@PostMapping("/getGranularPoints")
	public MongoGranularModel getGranularPoints(@RequestBody MongoGranularModel mongoGranularModel)
			throws IOException, ParseException {

		return generateGranularService.getMultiPoints(mongoGranularModel);
	}
}
