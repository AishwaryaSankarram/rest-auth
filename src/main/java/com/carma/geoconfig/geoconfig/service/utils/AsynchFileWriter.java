package com.carma.geoconfig.geoconfig.service.utils;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.data.mongodb.core.MongoTemplate;
import com.carma.geoconfig.geoconfig.model.ElasticGranularModel;
import com.carma.geoconfig.geoconfig.model.MongoGranularModel;

public class AsynchFileWriter implements Runnable{

	public MongoTemplate mongoTemplate;
	public FileWriterUtil fileWriterUtil;
	public MongoGranularModel mongoGranularModel;
	public String configJson;
	
	public AsynchFileWriter() {
		// TODO Auto-generated constructor stub
	}
	
	public AsynchFileWriter(MongoGranularModel mongoGranularModel,String json,MongoTemplate mongoTemplate, FileWriterUtil fileWriterUtil) {
		this.mongoGranularModel=mongoGranularModel;
		this.configJson=json;
		this.mongoTemplate=mongoTemplate;
		this.fileWriterUtil=fileWriterUtil;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("entered seperate thread carId ===> "+mongoGranularModel.getCarId()+" id-->"+mongoGranularModel.getId());
		Map<MongoGranularModel, List<ElasticGranularModel>> mappedData;
		try {
			mappedData = new CalculateGeoGranular()
					.getGranularPoints(mongoGranularModel);
			 String cofigFileName=mongoGranularModel.getV2xServer() + "__" + mongoGranularModel.getCarId() + "__"
						+ System.currentTimeMillis();
			 mongoGranularModel.setConfigFileName(cofigFileName);
			 
		MongoGranularModel mongoGranularModelCalc = mappedData.keySet().iterator().next();
					
System.out.println("file writer-->"+fileWriterUtil);
		/* write it into a file */
		fileWriterUtil.configFileWriter(
				mongoGranularModel.getConfigFileName(),
				configJson, mongoGranularModel.getRemoteIp(), mongoGranularModel.getRemoteUser(),
				mongoGranularModel.getRemotePath(), mongoGranularModel.getRemotePass());
		
		/*save granular points*/
		mongoTemplate.insertAll(mongoGranularModelCalc.getGranularPoints());
		mongoGranularModelCalc.setFileWritten(true);
		mongoTemplate.save(mongoGranularModelCalc);

//		scenario.getCars().set(i,mongoGranularModelCalc);
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
}
