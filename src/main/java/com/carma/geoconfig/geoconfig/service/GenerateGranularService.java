package com.carma.geoconfig.geoconfig.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.carma.geoconfig.geoconfig.model.ElasticGranularModel;
import com.carma.geoconfig.geoconfig.model.MongoGranularModel;

@Service
public class GenerateGranularService {
	
	@Autowired 
	MongoTemplate mongoTemplate;
	
	@Autowired
	UserLoginService userLoginService;
	
	@Autowired
	ConfigGeneratorService configGeneratorService;
	
	@Autowired
	FileWriterUtil fileWriterUtil;

	
	public MongoGranularModel getMultiPoints(MongoGranularModel mongoGranularModel) throws IOException, ParseException {

		/* get tripNo details */
		mongoGranularModel.setTripNo(userLoginService.getAndUpdateTripNo(mongoGranularModel.getCarId()));

		/* generate granular points */
		Map<MongoGranularModel, List<ElasticGranularModel>> mappedData = new CalculateGeoGranular()
				.getGranularPoints(mongoGranularModel);
		MongoGranularModel mongoGranularModelCalc = mappedData.keySet().iterator().next();

		/*save granular points*/
		mongoTemplate.insertAll(mongoGranularModelCalc.poly);
		mongoTemplate.insert(mongoGranularModelCalc);
		
		
		/* generate config json */
		JSONObject configJson = configGeneratorService.generateConfig(mongoGranularModel);

		/* write it into a file */
		fileWriterUtil.configFileWriter(
				mongoGranularModel.getV2xServer() + "__" + mongoGranularModel.getCarId() + "__"
						+ System.currentTimeMillis(),
				configJson.toJSONString(), mongoGranularModel.getRemoteIp(), mongoGranularModel.getRemoteUser(),
				mongoGranularModel.getRemotePath(), mongoGranularModel.getRemotePass());
		
		return mongoGranularModelCalc;
	}

}
