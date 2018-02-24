package com.carma.geoconfig.geoconfig.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import com.carma.geoconfig.geoconfig.model.ElasticGranularModel;
import com.carma.geoconfig.geoconfig.model.LoginModel;
import com.carma.geoconfig.geoconfig.model.MongoGranularChildModel;
import com.carma.geoconfig.geoconfig.model.MongoGranularModel;
import com.carma.geoconfig.geoconfig.service.utils.CalculateGeoGranular;
import com.carma.geoconfig.geoconfig.service.utils.FileWriterUtil;
import com.carma.geoconfig.geoconfig.service.utils.QueryUtil;
import com.carma.geoconfig.geoconfig.service.utils.SshCommandUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GenerateGranularService {
    private static final Logger log = LoggerFactory.getLogger(GenerateGranularService.class);

	@Autowired 
	MongoTemplate mongoTemplate;
	
	@Autowired
	UserLoginService userLoginService;
	
	@Autowired
	ConfigGeneratorService configGeneratorService;
	
	@Autowired
	FileWriterUtil fileWriterUtil;

	
	public MongoGranularModel createMultiPoints(MongoGranularModel mongoGranularModel, User user) throws IOException, ParseException {

		/* get tripNo details */
		mongoGranularModel.setTripNo(userLoginService.getAndUpdateTripNo(mongoGranularModel.getCarId()));


		/*fetching user details from loginUser table*/
		 if(user!=null) {
			Query query = new Query();
			query.addCriteria(Criteria.where("id").is(user.getUsername()));
			LoginModel loginModel = mongoTemplate.findOne(query, LoginModel.class);
			mongoGranularModel.setEmailId(loginModel.getEmailId());
			mongoGranularModel.setName(loginModel.getName());
			mongoGranularModel.setParentUserId(user.getUsername());
		 }
//		
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

	
	public List<MongoGranularModel> getMultiPointsById(String id, User user,int page,int size) throws IOException, ParseException {
		Query query = new Query();
//		Pageable pageable = new PageRequest(page, size);
		query.addCriteria(Criteria.where("parentUserId").is(id)).with(new Sort(Sort.Direction.DESC,"carId")).limit(1);
		log.info("query==> "+query);
		
		return mongoTemplate.find(query,MongoGranularModel.class).stream()
	             .map(o -> {
	                   		List<MongoGranularChildModel> mongoGranularChildModel = o.getPoly().stream()
	                                    .filter(x -> x.isParent())
	                                    .collect(Collectors.toList());
	            						 o.setPoly(mongoGranularChildModel);
	            						 return o;
	             			}).collect(Collectors.toList());
}
	
	public List<MongoGranularModel> getMultiPointsByFilter(String id, User user,int page,int size,Map<String, Object> params) throws IOException, ParseException {
		Query query = new Query();
		Pageable pageable = new PageRequest(page, size);
	
		if(params!=null)query=new QueryUtil().getQuery(params);
		query.addCriteria(Criteria.where("parentUserId").is(id)).with(pageable);
		log.info("query==> "+query);

		return  mongoTemplate.find(query,MongoGranularModel.class);
	}
	public void deleteCarDetails(String parentUserId,long carId,User user) throws JsonProcessingException {
		Query query =new Query();
		query.addCriteria(Criteria.where("parentUserId").is(parentUserId).and("carId").is(carId));
		System.out.println("===>"+query);
		mongoTemplate.remove(query,MongoGranularModel.class);
		mongoTemplate.remove(query,MongoGranularChildModel.class);

	}
	
	public String executeCommands(List<MongoGranularModel> mongoGranularModelList) {
		
		for (MongoGranularModel mongoGranularModel : mongoGranularModelList) {
//			String command="";
////			if(mongoGranularModel.configFileName!=null && mongoGranularModel.geoFileName!=null) {
//				command="cd "+mongoGranularModel.getRemotePath()+";ls -ltr";
//			}else {
//				
//			}
			new SshCommandUtil().executeCommand(mongoGranularModel.getRemoteUser(),mongoGranularModel.getRemoteIp(), mongoGranularModel.getRemotePass(), mongoGranularModel.getCommand());

		}
		return "success";
	}

}
