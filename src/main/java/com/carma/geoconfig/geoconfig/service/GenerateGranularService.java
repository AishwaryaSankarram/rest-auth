package com.carma.geoconfig.geoconfig.service;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
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
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
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
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import com.mongodb.client.model.DBCollectionDistinctOptions;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

@Service
public class GenerateGranularService {
    private static final Logger log = LoggerFactory.getLogger(GenerateGranularService.class);
    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
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
		 if(mongoGranularModel.getCarId()==null)mongoGranularModel.setCarId(UUID.randomUUID().toString());
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
		String fileName=mongoGranularModel.getCarLabel()==null?mongoGranularModel.getCarId():mongoGranularModel.getCarLabel();

		/* write it into a file */
		fileWriterUtil.configFileWriter(
				mongoGranularModel.getV2xServer() + "__" + fileName + "__"
						+ System.currentTimeMillis(),
				configJson.toJSONString(), mongoGranularModel.getRemoteIp(), mongoGranularModel.getRemoteUser(),
				mongoGranularModel.getRemotePath(), mongoGranularModel.getRemotePass());
		
		return mongoGranularModelCalc;
	}

	
	public List<MongoGranularModel> getMultiPointsById(String id, User user,int page,int size) throws IOException, ParseException {
		Query query = new Query();
		
//		AggregationResults<MongoGranularModel> agg = mongoTemplate.aggregate(newAggregation()
//				match(Criteria.where("_id").lt(10)),
//				group("hosting").count().as("total"),
//				project("total").and("hosting").previousOperation(),
//				sort(Sort.Direction.DESC, "total"));
//		
////
//	    Aggregation aggregation = new Aggregation(MongoGranularModel.class,
//	             match(Criteria.where("parentUserId").is(id)),
//	             group("carId").max("tripNo")
//	        );

	    
		
//		Pageable pageable = new PageRequest(page, size);
		query.addCriteria(Criteria.where("parentUserId").is(id)).with(new Sort(Sort.Direction.DESC,"tripNo")/*.with(new Sort(Sort.Direction.DESC,"tripNo")*/);
//		query.with(new Sort(Sort.Direction.DESC,"tripNo"));
		log.info("getMultiPointsById query==> "+query);
//		List<MongoGranularModel> results=(List<MongoGranularModel>) mongoTemplate.aggregate(aggw,"mongoGranularModel" ,MongoGranularModel.class).getMappedResults();
//		return results;

		List<MongoGranularModel> mongoGranularModelsList= mongoTemplate.find(query, MongoGranularModel.class).stream()
	             .map(o -> {
	                   		List<MongoGranularChildModel> mongoGranularChildModel = o.getPoly().stream()
	                                    .filter(x -> x.isParent())
	                                    .collect(Collectors.toList());
	            						 o.setPoly(mongoGranularChildModel);
	            						 return o;
	             			}).filter(distinctByKey(MongoGranularModel::getCarId)).sorted(Comparator.comparing(o->o.createdAt)).collect(Collectors.toList());
//		Collections.reverse(mongoGranularModelsList);
		return mongoGranularModelsList;
	}
	
	public List<MongoGranularModel> getMultiPointsByFilter(String id, User user,int page,int size,Map<String, Object> params) throws IOException, ParseException {
		Query query = new Query();
		Pageable pageable = new PageRequest(page, size);
	
		if(params!=null)query=new QueryUtil().getQuery(params);
//		query.addCriteria(Criteria.where("parentUserId").is(id)).with(pageable);

		query.addCriteria(Criteria.where("parentUserId").is(id)).with(new Sort(Sort.Direction.DESC,"tripNo")/*.with(new Sort(Sort.Direction.DESC,"tripNo")*/);

//		DBObject o1 = new BasicDBObject("carId","$max");
        
		return mongoTemplate.getCollection("mongoGranularModel").distinct("carId",new DBCollectionDistinctOptions().filter(query.getQueryObject()));
		
//		query.addCriteria(Criteria.where("parentUserId").is(id));
//		Aggregation aggw = newAggregation(
//	            match(Criteria.where("parentUserId").is(id)),
//	            sort(Sort.Direction.ASC, "createdAt"),
//	            group("carId").max("tripNo").as("tripNo").last("$carId").as("carId")
//	            
//	        );
//		return mongoTemplate.aggregate(aggw,"mongoGranularModel", MongoGranularModel.class).getMappedResults();

//		log.info("getMultiPointsByFilter query==> "+query);
//
//		return  mongoTemplate.find(query,MongoGranularModel.class);
	}
	public void deleteCarDetails(String parentUserId,String carId,User user) throws JsonProcessingException {
		Query query =new Query();
		query.addCriteria(Criteria.where("parentUserId").is(parentUserId).and("carId").is(carId));
		log.info("delete query ===>"+query);
		WriteResult writeResult=null;
		try {
			mongoTemplate.remove(query,MongoGranularModel.class);
			mongoTemplate.remove(query,MongoGranularChildModel.class);
		}catch (Exception e) {
			// TODO: handle exception
			log.error(e.getMessage());
			throw new SecurityException(e);

		}
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
