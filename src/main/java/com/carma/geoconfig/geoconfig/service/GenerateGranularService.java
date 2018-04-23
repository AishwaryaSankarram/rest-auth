package com.carma.geoconfig.geoconfig.service;

import java.io.IOException;
import java.util.ArrayList;
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
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import com.carma.geoconfig.geoconfig.model.ElasticGranularModel;
import com.carma.geoconfig.geoconfig.model.LoginModel;
import com.carma.geoconfig.geoconfig.model.MapData;
import com.carma.geoconfig.geoconfig.model.MongoGranularChildModel;
import com.carma.geoconfig.geoconfig.model.MongoGranularModel;
import com.carma.geoconfig.geoconfig.model.Scenario;
import com.carma.geoconfig.geoconfig.service.utils.AsynchFileWriter;
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
	

	@Transactional
	public Scenario createMultiPoints(Scenario scenario, User user) throws Exception {

		/*fetching user details from loginUser table*/
		//LoginModel loginModel=null;
		 if(user!=null) {
			Query query = new Query();
			query.addCriteria(Criteria.where("id").is(user.getUsername()));
//			loginModel = mongoTemplate.findOne(query, LoginModel.class);
			scenario.setScenarioId(UUID.randomUUID().toString());
//			scenario.setEmailId(loginModel.getEmailId());
//			scenario.setName(loginModel.getName());
			scenario.setParentUserId(user.getUsername());
//			if(loginModel.getAddress()!=null)mongoGranularModel.setAddress(loginModel.getAddress());
		 }
		 if(null!=scenario.mapDataList && scenario.getMapDataList().size()>0) {
			scenario.setMapDataList( scenario.getMapDataList().stream().map(o->{
				o.setSignalId(UUID.randomUUID().toString());
				return o;
			}).collect(Collectors.toList()));
		 }
		if(null!=scenario.getCars() && scenario.getCars().size()>0) {
			
			for(int i=0;i<scenario.getCars().size();i++) {
				/* get tripNo details */
				MongoGranularModel mongoGranularModel=scenario.getCars().get(i);
				 if(mongoGranularModel.getCarId()==null)mongoGranularModel.setCarId(UUID.randomUUID().toString());
				
				 mongoGranularModel.setScenarioId(scenario.getScenarioId());
				 mongoGranularModel.setTripNo(userLoginService.getAndUpdateTripNo(mongoGranularModel.getCarId()));
				
				/*fetching user details from loginUser table*/
				// if(loginModel!=null) {
//					mongoGranularModel.setEmailId(loginModel.getEmailId());
//					mongoGranularModel.setName(loginModel.getName());
				 mongoGranularModel.setFileWritten(false);
				 mongoGranularModel.setParentUserId(user.getUsername());
		//			if(loginModel.getAddress()!=null)mongoGranularModel.setAddress(loginModel.getAddress());
				// }
				 
					/* generate config json */

				 JSONObject configJson = configGeneratorService.generateConfig(mongoGranularModel);

				 mongoTemplate.insert(mongoGranularModel);

				 scenario.getCars().set(i,mongoGranularModel);
				 AsynchFileWriter asynchFileWriter=new AsynchFileWriter(mongoGranularModel,configJson.toString(),mongoTemplate,fileWriterUtil);
				 Thread fileWriteThread=new Thread(asynchFileWriter);
				 fileWriteThread.start();
					
				/* generate granular points */
			
			}
		}
		
		mongoTemplate.save(scenario);
		return scenario;
	}



	// update scenario 
	public Scenario findAndUpdateTripDetails(Scenario scenario, User user) throws Exception {
	
		Query queryToGetScenario = new Query();
		queryToGetScenario.fields();
		queryToGetScenario.addCriteria(Criteria.where("parentUserId").is(user.getUsername()).and("scenarioId").is(scenario.getScenarioId()));
		Scenario scenarioExists=mongoTemplate.findOne(queryToGetScenario,Scenario.class);
		 log.info("response body of existing scenario : "+new ObjectMapper().writeValueAsString(scenarioExists));

		if(scenario.getName()!=null)scenarioExists.setName(scenario.getName());
		if(scenario.getUserAddress()!=null)scenarioExists.setUserAddress(scenario.getUserAddress());
		scenarioExists.getCars().removeAll(Collections.singleton(null));
		scenarioExists.getCars().removeIf( o-> o.deleted);

		 if(null!=scenario.getMapDataList() && scenario.getMapDataList().size()>0) {
			 scenarioExists.setMapDataList( scenario.getMapDataList().stream().map(o->{
					o.setSignalId(UUID.randomUUID().toString());
					return o;
				}).collect(Collectors.toList()));
			 }
		
		 
		if(null!=scenario.getCars() && scenario.getCars().size()>0) {
			
			for(int i=0;i<scenario.getCars().size();i++) {
				
				MongoGranularModel mongoGranularModel=scenario.getCars().get(i);
				
				if(mongoGranularModel.getCarId()!=null) {
					
					MongoGranularModel mongoGranularModelExists=scenarioExists.getCars().stream().filter(o->o.carId.equals(mongoGranularModel.carId)).findFirst().get();
					scenarioExists.getCars().removeAll(Collections.singleton(null));

					scenarioExists.getCars().removeIf(o->  o.carId.equals(mongoGranularModel.carId));
						// to update granular table for particular car Id 
						Query query = new Query();
						query.addCriteria(Criteria.where("parentUserId").is(user.getUsername()).and("carId").is(mongoGranularModel.getCarId()));
						/*Remove child data first*/
						mongoTemplate.remove(query,MongoGranularChildModel.class);
	//					MongoGranularModel mongoGranularModelExists=mongoTemplate.findOne(query,MongoGranularModel.class);
						
						if(mongoGranularModel.getCarLabel()!=null)mongoGranularModelExists.setCarLabel(mongoGranularModel.getCarLabel());
						if(mongoGranularModel.getAddress()!=null)mongoGranularModelExists.setAddress(mongoGranularModel.getAddress());
						if(mongoGranularModel.getGpsCanServer()!=null)mongoGranularModelExists.setGpsCanServer(mongoGranularModel.getGpsCanServer());
						if(mongoGranularModel.getRemoteIp()!=null)mongoGranularModelExists.setRemoteIp(mongoGranularModel.getRemoteIp());
						if(mongoGranularModel.getRemotePass()!=null)mongoGranularModelExists.setRemotePass(mongoGranularModel.getRemotePass());
						if(mongoGranularModel.getRemotePath()!=null)mongoGranularModelExists.setRemotePath(mongoGranularModel.getRemotePath());
						if(mongoGranularModel.getRemoteUser()!=null)mongoGranularModelExists.setRemoteUser(mongoGranularModel.getRemoteUser());
						if(mongoGranularModel.getStartAtSec()!=null)mongoGranularModelExists.setStartAtSec(mongoGranularModel.getStartAtSec());
						if(mongoGranularModel.getStepSize()!=null)mongoGranularModelExists.setStepSize(mongoGranularModel.getStepSize());
						if(mongoGranularModel.getV2xServer()!=null)mongoGranularModelExists.setV2xServer(mongoGranularModel.getV2xServer());
						if(mongoGranularModel.getPoly()!=null)mongoGranularModelExists.setPoly(mongoGranularModel.getPoly());
						if(mongoGranularModel.getUseAsEv()!=null)mongoGranularModelExists.setUseAsEv(mongoGranularModel.getUseAsEv());
						
						/* generate config json */
						JSONObject configJson = configGeneratorService.generateConfig(mongoGranularModel);
						mongoTemplate.save(mongoGranularModel);
						scenarioExists.getCars().add(mongoGranularModel);

						AsynchFileWriter asynchFileWriter=new AsynchFileWriter(mongoGranularModel,configJson.toString(),mongoTemplate,fileWriterUtil);
						Thread fileWriteThread=new Thread(asynchFileWriter);
						fileWriteThread.start();
//						Map<MongoGranularModel, List<ElasticGranularModel>> mappedData = new CalculateGeoGranular()
//								.getGranularPoints(mongoGranularModelExists);
//						MongoGranularModel mongoGranularModelCalc = mappedData.keySet().iterator().next();
//						
//				
						
						/*update granular points*/
				//		mongoTemplate.insertAll(mongoGranularModelCalc.getGranularPoints());
				//		Update update=new Update();
				//		update.set("poly", mongoGranularModel.getPoly());
						
				//		mongoTemplate.findAndModify(query, new Update().set("address",mongoGranularModel.getAddress()) ,LoginModel.class);
				
//						/* write it into a file */
//						fileWriterUtil.configFileWriter(
//								mongoGranularModelCalc.getV2xServer() + "__" + fileName + "__"
//										+ System.currentTimeMillis(),
//								configJson.toJSONString(), mongoGranularModelCalc.getRemoteIp(), mongoGranularModelCalc.getRemoteUser(),
//								mongoGranularModelCalc.getRemotePath(), mongoGranularModelCalc.getRemotePass());
//	
//						mongoTemplate.insertAll(mongoGranularModelCalc.getGranularPoints());
						
						
					}else {
						
						 if(mongoGranularModel.getCarId()==null)mongoGranularModel.setCarId(UUID.randomUUID().toString());
						 mongoGranularModel.setScenarioId(scenario.getScenarioId());
						 mongoGranularModel.setTripNo(userLoginService.getAndUpdateTripNo(mongoGranularModel.getCarId()));
						 String cofigFileName=mongoGranularModel.getV2xServer() + "__" + mongoGranularModel.getCarId() + "__"
									+ System.currentTimeMillis();
						 mongoGranularModel.setConfigFileName(cofigFileName);
						/*fetching user details from loginUser table*/
//							mongoGranularModel.setEmailId(scenario.getEmailId());
							//mongoGranularModel.setName(loginModel.getName());
							mongoGranularModel.setParentUserId(user.getUsername());
				//			if(loginModel.getAddress()!=null)mongoGranularModel.setAddress(loginModel.getAddress());
				//		
							
							/* generate config json */
							JSONObject configJson = configGeneratorService.generateConfig(mongoGranularModel);
					
						/* generate granular points */
							mongoTemplate.insert(mongoGranularModel);
							scenarioExists.getCars().add(mongoGranularModel);
							AsynchFileWriter asynchFileWriter=new AsynchFileWriter(mongoGranularModel,configJson.toString(),mongoTemplate,fileWriterUtil);
							Thread fileWriteThread=new Thread(asynchFileWriter);
							fileWriteThread.start();
								
//							
//						Map<MongoGranularModel, List<ElasticGranularModel>> mappedData = new CalculateGeoGranular()
//								.getGranularPoints(mongoGranularModel);
//						MongoGranularModel mongoGranularModelCalc = mappedData.keySet().iterator().next();
//									
//					
//						/* write it into a file */
//						fileWriterUtil.configFileWriter(
//								mongoGranularModel.getConfigFileName(),
//								configJson.toJSONString(), mongoGranularModel.getRemoteIp(), mongoGranularModel.getRemoteUser(),
//								mongoGranularModel.getRemotePath(), mongoGranularModel.getRemotePass());
//						
						/*save granular points*/
//						mongoTemplate.insertAll(mongoGranularModelCalc.getGranularPoints());
					

					}
					
			}
		}
		mongoTemplate.save(scenarioExists);

		scenarioExists.getCars().sort(Comparator.comparing(MongoGranularModel::getCreatedAt));
		return scenarioExists;
		
		
	}
	

	
	
	public List<Scenario> getMultiPointsById(String id, User user,int page,int size,boolean isOnlyScenarios) throws IOException, ParseException {
		Query query = new Query();
	
	    if(id!=null)query.addCriteria(Criteria.where("scenarioId").is(id));
		
//		Pageable pageable = new PageRequest(page, size);
		query.addCriteria(Criteria.where("parentUserId").is("5a9f71b5bc661922deff1585"/*user.getUsername()*/).and("deleted").is(false)).with(new Sort(Sort.Direction.ASC,"createdAt")/*.with(new Sort(Sort.Direction.DESC,"tripNo")*/);
//		query.fields().exclude("poly");
		log.info("getMultiPointsById query==> "+query);
		if(isOnlyScenarios) {
			query.fields().exclude("cars").exclude("mapDataList");
			log.info("getMultiPointsByFileWritten query==> "+query);

			List<Scenario> scenarioList= mongoTemplate.find(query, Scenario.class);
			return scenarioList;
		}
		

//		
//		return mongoTemplate.find(query, MongoGranularModel.class);
		//##############################   recently used working code below  ##########################
		List<Scenario> scenarioList= mongoTemplate.find(query, Scenario.class);

//		 return scenarioList;
		return scenarioList.stream().map(o -> {
					if(null!=o &&  o.getCars()!=null && o.getCars().size()>0  ) {
			
				
			
       				List<MongoGranularModel> cars = o.getCars().stream()
       								.filter( x->x!=null && !x.deleted)
       								.map(mongoGranularModel->{
       									mongoGranularModel.setGranularPoints(null);
       									return mongoGranularModel; 
       								})
       								.collect(Collectors.toList());
       							
       							cars.sort(Comparator.comparing(MongoGranularModel::getCreatedAt));
       							o.setCars(cars);
       							
					}
       								return o;
       								
					}).collect(Collectors.toList());
	}
	
	
	//used by guru in the out put system 
	public List<Scenario> getMultiPointsByFileWritten(User user,int page,int size,boolean isOnlyScenarios) throws IOException, ParseException {
		Query query = new Query();
		
//		Pageable pageable = new PageRequest(page, size);
		query.addCriteria(Criteria.where("parentUserId").is(user.getUsername()).and("deleted").is(false)).with(new Sort(Sort.Direction.ASC,"createdAt")/*.with(new Sort(Sort.Direction.DESC,"tripNo")*/);
//		query.fields().exclude("poly");
		log.info("getMultiPointsByFileWritten query==> "+query);
		if(isOnlyScenarios) {
			query.fields().exclude("cars").exclude("mapDataList");
			List<Scenario> scenarioList= mongoTemplate.find(query, Scenario.class);
			return scenarioList;
		}
//		
		log.info("getMultiPointsByFileWritten query==> "+query);

		
//		return mongoTemplate.find(query, MongoGranularModel.class);
		//##############################   recently used working code below  ##########################
		
		List<Scenario> scenarioList= mongoTemplate.find(query, Scenario.class);
		return scenarioList.stream().map(o -> {
					if(null!=o &&  o.getCars()!=null && o.getCars().size()>0 ) {
			
       				List<MongoGranularModel> cars = o.getCars().stream()
       								.filter(x->!x.deleted && null!=x.getFileWritten() && x.getFileWritten())
       								.map(mongoGranularModel->{
       									mongoGranularModel.setGranularPoints(null);
       									return mongoGranularModel; 
       								})
       								.collect(Collectors.toList());
       							cars.sort(Comparator.comparing(MongoGranularModel::getCreatedAt));
       							o.setCars(cars);
       							
					}
       								return o;
       								
					}).collect(Collectors.toList());
	}
	
	public void deleteCarDetails(String scenarioId,String carId,User user) throws JsonProcessingException {
		Query query =new Query();
		query.addCriteria(Criteria.where("parentUserId").is(user.getUsername()).and("scenarioId").is(scenarioId));
		
		Update update=new Update();
		update.set("deleted", true);
		try {
			mongoTemplate.findAndModify(query.addCriteria(Criteria.where("carId").is(carId)),update,MongoGranularModel.class);
//			mongoTemplate.remove(query,MongoGranularChildModel.class);	
			mongoTemplate.findAndModify(query,update,Scenario.class);
			log.info("delete a car query ===>"+query);

		}catch (Exception e) {
			// TODO: handle exception
			log.error(e.getMessage());
			throw new SecurityException("Not able to delete this car");

		}
	}
	
	
	public void deleteScenario(String scenarioId,User user) throws JsonProcessingException {
		Query query =new Query();
		query.addCriteria(Criteria.where("parentUserId").is(user.getUsername()).and("scenarioId").is(scenarioId));
		try {
			Update update=new Update();
			update.set("deleted", true);
			mongoTemplate.findAndModify(query,update,Scenario.class);
			mongoTemplate.findAndModify(query,update,MongoGranularModel.class);
//			mongoTemplate.remove(query,MongoGranularChildModel.class);
			log.info("delete a scenario query ===>"+query);

		}catch (Exception e) {
			// TODO: handle exception
			log.error(e.getMessage());
			throw new SecurityException("Not able to delete this scenario");

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
	
	
	// old APIs ******************************************************************
	public LoginModel findAndUpdateAddress(LoginModel loginModel, User user) {
		Query queryToUpdateAddress = new Query();
		queryToUpdateAddress.addCriteria(Criteria.where("id").is(user.getUsername()));
		LoginModel loginResp=mongoTemplate.findOne(queryToUpdateAddress, LoginModel.class);
		if(loginModel.getUserAddress()!=null)loginResp.setUserAddress(loginModel.getUserAddress());
		if(loginModel.getName()!=null)loginResp.setName(loginModel.getName());
		if(loginModel.getPassword()!=null && loginModel.getOldPassword()!=null) {
			PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
			if (!passwordEncoder.matches(loginModel.getOldPassword(), loginResp.getPassword())) {
				throw new SecurityException("Passwords do not match");
			}else {
				loginResp.setPassword(passwordEncoder.encode(loginModel.getPassword()));
			}
		}
//		if(loginModel.getPlaceId()!=null)loginResp.setPlaceId(loginModel.getPlaceId());
//		if(loginModel.getLatitude()!=null)loginResp.setLatitude(loginModel.getLatitude());
//		if(loginModel.getLongitude()!=null)loginResp.setLongitude(loginModel.getLongitude());

		mongoTemplate.save(loginResp);
		return loginResp;
		// to update granular table
//		Query query = new Query();
//		query.addCriteria(Criteria.where("parentUserId").is(user.getUsername())).fields().exclude("granularPoints");
//		return mongoTemplate.findAndModify(query, new Update().set("address",mongoGranularModel.getAddress()), MongoGranularModel.class);
		
		
	}
	
	public List<MongoGranularModel> getMultiPointsByFilter(String id, User user,int page,int size,Map<String, Object> params) throws IOException, ParseException {
		Query query = new Query();
		Pageable pageable = new PageRequest(page, size);
	
		if(params!=null)query=new QueryUtil().getQuery(params);
//		query.addCriteria(Criteria.where("parentUserId").is(id)).with(pageable);

		query.addCriteria(Criteria.where("parentUserId").is(id)).with(new Sort(Sort.Direction.DESC,"tripNo")/*.with(new Sort(Sort.Direction.DESC,"tripNo")*/);

		
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

//		List<MongoGranularModel> results=(List<MongoGranularModel>) mongoTemplate.aggregate(aggw,"mongoGranularModel" ,MongoGranularModel.class).getMappedResults();
//		return results;

//		DBObject o1 = new BasicDBObject("carId","$max");
        
//		return mongoTemplate.getCollection("mongoGranularModel").distinct("carId",new DBCollectionDistinctOptions().filter(query.getQueryObject()));
		
//		query.addCriteria(Criteria.where("parentUserId").is(id));
//		DBObject group = new BasicDBObject("$group", JSON.parse(
//			    "{
//			        '_id': null,
//			        'array': {
//			            '$addToSet': 'fieldC.an_array.a'
//			        }
//			     }"
//			));
		
//		AggregationOperation group = ;

		Aggregation aggw = newAggregation(
	            match(Criteria.where("parentUserId").is(id)),
	            sort(Sort.Direction.DESC, "tripNo"),
	            //	            group(Fields.field("_id", "carId")),
	            group("carId").count().as("totalTrips"),
//	            group()
	            project("carId","createdAt","tripNo").and("totalTrips").previousOperation(),

	            sort(Sort.Direction.ASC,"createdAt")
	            
	        );
		
		log.info("filter query: "+aggw);
		return mongoTemplate.aggregate(aggw,"mongoGranularModel", MongoGranularModel.class).getMappedResults();

//		log.info("getMultiPointsByFilter query==> "+query);
//
//		return  mongoTemplate.find(query,MongoGranularModel.class);
	}
	
	
	public Scenario createMapData(List<MapData> mapDataList,String scenarioId,User user) {
		Query query =new Query();
		Criteria criteria = null;
		if(user!=null) {
			query.addCriteria(criteria.where("parentUserId").is(user.getUsername()));
		}
		query.addCriteria(criteria.where("scenarioId").is(scenarioId));
		log.info("mapdata query: "+query);
		Scenario scenario=mongoTemplate.findOne(query, Scenario.class);
		if(scenario==null) {
			throw new SecurityException("scenario not found");
		}else {
			mapDataList.stream().map(o->{
				o.setSignalId(UUID.randomUUID().toString());
				return o;
			}).collect(Collectors.toList());
			scenario.setMapDataList(mapDataList);	
			mongoTemplate.save(scenario);
			return scenario;
		}
	}
	
	public List<MapData> getMapData(String scenarioId,User user){
		List<MapData> mapDataList = new ArrayList<MapData>();
		Query query =new Query();
		Criteria criteria = null;
		if(user!=null) {
			query.addCriteria(criteria.where("parentUserId").is(user.getUsername()));
		}
		query.addCriteria(criteria.where("scenarioId").is(scenarioId));
		Scenario scenario = mongoTemplate.findOne(query,Scenario.class);
		if(scenario==null)throw new SecurityException("scenario not found");
		return scenario.getMapDataList()!=null ?scenario.getMapDataList():mapDataList;
	}

}
