package com.carma.geoconfig.geoconfig.service.utils;

import static java.lang.Math.PI;
import static java.lang.Math.asin;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carma.geoconfig.geoconfig.model.ElasticGranularModel;
import com.carma.geoconfig.geoconfig.model.MongoGranularChildModel;
import com.carma.geoconfig.geoconfig.model.MongoGranularModel;
import com.carma.geoconfig.geoconfig.service.utils.FileWriterUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CalculateGeoGranular {
	
    private static final Logger LOG = LoggerFactory.getLogger(CalculateGeoGranular.class);


	public final double _default_speed = 10; // meters per second
	public final double _default_acceleration = 0; // meters per second
	
	int childPointsCount=0;
	boolean addEndPoint = false;

	double speed_at_point_one = _default_speed;
	double speed_at_point_two = _default_speed;
	double acceleration_at_point_one = _default_acceleration;
	double acceleration_at_point_two = _default_acceleration;
	JSONArray jsonArray;

	/**
	 * Get all the granular lat lng points in between the vertices of the polyline
	 *
	 * @param polyArr
	 * @param stepSizeInMeters
	 * @param sioc
	 * @param wholeObj
	 * @return
	 * @throws Exception 
	 */
	public Map<MongoGranularModel,List<ElasticGranularModel>> getGranularPoints(MongoGranularModel mongoGranularModel) throws Exception {
		CircularFifoQueue<MongoGranularChildModel> twoPairJsonQueuingWindow = new CircularFifoQueue<MongoGranularChildModel>(2);
		List<ElasticGranularModel> granularPoly = null;
		JSONObject gpsJsonToFile = new JSONObject();
		jsonArray=new JSONArray();
		if (mongoGranularModel.getPoly().size() > 1) {
			granularPoly = new ArrayList<ElasticGranularModel>();
			double timeGlobal = 0;
			for (int i = 0; i < mongoGranularModel.getPoly().size(); i++) {
				MongoGranularChildModel eachPolyObj = (MongoGranularChildModel) mongoGranularModel.getPoly().get(i);
				twoPairJsonQueuingWindow.add(eachPolyObj);
				if (twoPairJsonQueuingWindow.size() == 2) {
					if (i == (mongoGranularModel.getPoly().size() - 1))
						addEndPoint = true;
					List<ElasticGranularModel> newGranularMultiPoints = getPoints(twoPairJsonQueuingWindow, mongoGranularModel,
							timeGlobal);
					//System.out.println("Granular point count between the two sub-points of the poly -> "
						//	+ newGranularMultiPoints.size());
					granularPoly.addAll(newGranularMultiPoints);
					timeGlobal = newGranularMultiPoints.get(newGranularMultiPoints.size() - 1).getRelativeTime();

				}
			}
		}
		ObjectMapper mapper = new ObjectMapper();
		String jsonData=mapper.writeValueAsString(granularPoly);
//		mongoGranularModel.setTripNo(granularPoly.get(0).getTripNo());
		mongoGranularModel.setGranularPoints(mapper.readValue(jsonData, new TypeReference<List<MongoGranularChildModel>>(){}));
		Map<MongoGranularModel,List<ElasticGranularModel>> returnMap=new HashMap<MongoGranularModel,List<ElasticGranularModel>>();
		returnMap.put(mongoGranularModel, granularPoly);
		try {
			gpsJsonToFile.put("GPS", jsonArray);
			String fileName=mongoGranularModel.getCarLabel()==null?mongoGranularModel.getCarId():mongoGranularModel.getCarLabel();
			new FileWriterUtil().gpsFileWriter(mongoGranularModel.getV2xServer()+"__"+(fileName+"__"+mongoGranularModel.getTripNo()), gpsJsonToFile.toJSONString(),mongoGranularModel.getRemoteIp(),mongoGranularModel.getRemoteUser(),mongoGranularModel.getRemotePath(),mongoGranularModel.getRemotePass());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return returnMap;
	}

	/**
	 * Get all the lat lng points in between the two vertices
	 *
	 * @param twoPairJsonQueuingWindow
	 * @param stepSizeInMeters
	 * @param sioc
	 * @param wholeObj
	 * @param addEndPoint
	 * @return
	 */
	List<ElasticGranularModel> getPoints(CircularFifoQueue<MongoGranularChildModel> twoPairJsonQueuingWindow,
			MongoGranularModel mongoGranularModel, double timeGlobal) {

		if (twoPairJsonQueuingWindow.get(0).getSpeed() != null) {
			speed_at_point_one = twoPairJsonQueuingWindow.get(0).getSpeed();
		}
		if (twoPairJsonQueuingWindow.get(1).getSpeed() != null) {
			speed_at_point_two = twoPairJsonQueuingWindow.get(1).getSpeed();
		}
		if (twoPairJsonQueuingWindow.get(0).getAcceleration() != null) {
			acceleration_at_point_one = twoPairJsonQueuingWindow.get(0).getAcceleration();
		}
		if (twoPairJsonQueuingWindow.get(1).getAcceleration() != null) {
			acceleration_at_point_two = twoPairJsonQueuingWindow.get(1).getAcceleration();
		}
//		System.out.println("accl-> " + acceleration_at_point_one);
		double begin_latitude = twoPairJsonQueuingWindow.get(0).getLat();
		double begin_longitude = twoPairJsonQueuingWindow.get(0).getLng();
		double end_latitude = twoPairJsonQueuingWindow.get(1).getLat();
		double end_longitude = twoPairJsonQueuingWindow.get(1).getLng();
		List<ElasticGranularModel> allPoints = new ArrayList<ElasticGranularModel>();
		double bearing = getBearing(begin_latitude, begin_longitude, end_latitude, end_longitude);
//		System.out.println("begin Child count"+childPointsCount);

		ElasticGranularModel beginPoint = createGranularPoint(mongoGranularModel,  begin_latitude, begin_longitude,
				speed_at_point_one, timeGlobal,bearing, acceleration_at_point_one,true);
		allPoints.add(beginPoint);
		

		double distanceBetweenPoints = getDistance(begin_latitude, end_latitude, begin_longitude, end_longitude, 0.0,
				0.0);
		System.out
				.println("Distance Between two subsequent points of a polyline (in meters)-> " + distanceBetweenPoints);

		double timeInSeconds = distanceBetweenPoints / speed_at_point_one;
//		System.out.println("Time taken to travel from point 1 to 2 (in seconds) -> " + timeInSeconds);

		double stepSizeInMeters = mongoGranularModel.getStepSize();
		int numberOfSteps = (int) (distanceBetweenPoints / stepSizeInMeters);
		double remainingDist = distanceBetweenPoints % stepSizeInMeters;

		double timeToTravel = stepSizeInMeters / speed_at_point_one;
		LOG.info("no of steps = "+numberOfSteps);
		LOG.info("reminining dist= "+remainingDist);
		double tempStepSizeInMeters=stepSizeInMeters;
//		System.out.println("number of steps=> " + numberOfSteps + " remainingDist=> " + remainingDist);
		for (int i = 0; i < numberOfSteps;i++) {
			LOG.info("loop value i= "+i);
			// double localStepSizeInMeters = stepSizeInMeters*i;
			childPointsCount=childPointsCount+1;
//System.out.println("loop Child count"+childPointsCount);
			
			if (acceleration_at_point_one != 0 || remainingDist!=0 && i==numberOfSteps-1) {// this is to change speed based on acceleration & based on remaining dist 
				//stepsize changed to last meters and this will be taken as begin point speed of upcoming if not speed provided further   
				
				if (i == (numberOfSteps-1)  && remainingDist!=0 && addEndPoint) {
					stepSizeInMeters = remainingDist;
				}
				timeToTravel = stepSizeInMeters / speed_at_point_one;
				if (acceleration_at_point_one != 0) speed_at_point_one = (acceleration_at_point_one * timeToTravel) + speed_at_point_one;

			}
			
			if (i == (numberOfSteps-1) && remainingDist == 0  && addEndPoint) {
				//add the last point if it does not has the remaining value ....  
					if (acceleration_at_point_two != 0)
						speed_at_point_one = (acceleration_at_point_two * timeToTravel) + speed_at_point_one;
					if (acceleration_at_point_two == 0)
						acceleration_at_point_two = acceleration_at_point_one;
					if (speed_at_point_two == 10)
						speed_at_point_two = speed_at_point_one;
					System.out.println("remaining dist1==>"+stepSizeInMeters);
					ElasticGranularModel endPoint = createGranularPoint(mongoGranularModel, end_latitude, end_longitude,
							speed_at_point_two, (timeGlobal + timeInSeconds), bearing, acceleration_at_point_two, true);
					allPoints.add(endPoint);
				
			
			
				break;
			}else if(i == (numberOfSteps-1) && remainingDist != 0 ) {
				///add the last point  and remaining dist point if it has the remaining value ....  
				
				ElasticGranularModel newPoint = getNewPoints(mongoGranularModel, begin_latitude, begin_longitude, tempStepSizeInMeters, bearing,
						speed_at_point_one, ((timeToTravel * i) + timeGlobal), acceleration_at_point_one,false);
				allPoints.add(newPoint);
				begin_latitude =newPoint.getLat();
				begin_longitude = newPoint.getLng();
				bearing = getBearing(begin_latitude, begin_longitude, end_latitude, end_longitude);
	
				
					childPointsCount=childPointsCount+1;
//					System.out.println("last Child count"+childPointsCount);

					if (acceleration_at_point_two != 0)
						speed_at_point_one = (acceleration_at_point_two * timeToTravel) + speed_at_point_one;
					if (acceleration_at_point_two == 0)
						acceleration_at_point_two = acceleration_at_point_one;
					if (speed_at_point_two == 10)
						speed_at_point_two = speed_at_point_one;

					if(addEndPoint) {
						System.out.println("remaining dist2==>"+stepSizeInMeters);

						ElasticGranularModel endPoint = createGranularPoint(mongoGranularModel, end_latitude, end_longitude,
								speed_at_point_two, (timeGlobal + timeInSeconds), bearing, acceleration_at_point_two, true);
						allPoints.add(endPoint);
					}
			}
			
//			if (acceleration_at_point_one != 0 || remainingDist!=0 && i==numberOfSteps-1) {// this is to change speed based on acceleration & based on remaining dist 
//				//stepsize changed to last meters and this will be taken as begin point speed of upcoming if not speed provided further   
//				
//				if (i == (numberOfSteps-1)  && remainingDist!=0) {
//					stepSizeInMeters = remainingDist;
//				}
//				timeToTravel = stepSizeInMeters / speed_at_point_one;
//				if (acceleration_at_point_one != 0) speed_at_point_one = (acceleration_at_point_one * timeToTravel) + speed_at_point_one;
//
//			}
			
			if(i != (numberOfSteps-1)) {
				ElasticGranularModel newPoint = getNewPoints(mongoGranularModel, begin_latitude, begin_longitude, stepSizeInMeters, bearing,
						speed_at_point_one, ((timeToTravel * i) + timeGlobal), acceleration_at_point_one,false);
				allPoints.add(newPoint);
				begin_latitude =newPoint.getLat();
				begin_longitude = newPoint.getLng();
				bearing = getBearing(begin_latitude, begin_longitude, end_latitude, end_longitude);
			
				
			}

			// return js;
			// wholeObj.put("granular_poly", js);
			// sioc.sendEvent("granular", wholeObj);

		}
		// if(acceleration_at_point_two!=0) speed_at_point_one=
		// (acceleration_at_point_two*timeToTravel)+speed_at_point_one;
		//
		// if(addEndPoint) {
		// if(acceleration_at_point_two==0)
		// acceleration_at_point_two=acceleration_at_point_one;
		// if(speed_at_point_two==10) speed_at_point_two=speed_at_point_one;
		//
		// JSONObject endPoint = createJsonObject(carId, tripNumber, end_latitude,
		// end_longitude, speed_at_point_two, startAtTimeInSec,
		// (timeGlobal+timeInSeconds),bearing,acceleration_at_point_two);
		// allPoints.add(endPoint);
		// }
		// System.out.println("End Point -> " + allPoints.get(allPoints.size() - 1));
		childPointsCount=childPointsCount+1;

		return allPoints;
	}

	/**
	 * Get the bearing/heading between the two lat lng points
	 *
	 * @param begin_latitude
	 * @param begin_longitude
	 * @param end_latitude
	 * @param end_longitude
	 * @return
	 */
	double getBearing(double begin_latitude, double begin_longitude, double end_latitude, double end_longitude) {
		double lat = Math.abs(begin_latitude - end_latitude);
		double lng = Math.abs(begin_longitude - end_longitude);

		if (begin_latitude < end_latitude && begin_longitude < end_longitude) {
			return (float) (Math.toDegrees(Math.atan(lng / lat)));
		} else if (begin_latitude >= end_latitude && begin_longitude < end_longitude) {
			return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 90);
		} else if (begin_latitude >= end_latitude && begin_longitude >= end_longitude) {
			return (float) (Math.toDegrees(Math.atan(lng / lat)) + 180);
		} else if (begin_latitude < end_latitude && begin_longitude >= end_longitude) {
			return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 270);
		}
		return -1;
	}

	/**
	 * Get the new lat lng point d meters from the given lat lng. Bearing/Heading is
	 * also provided
	 *
	 * @param latitude
	 * @param longitude
	 * @param distanceInMetres
	 * @param bearing
	 * @return
	 */
	ElasticGranularModel getNewPoints(MongoGranularModel mongoGranularModel, double latitude, double longitude,
			double distanceInMetres, double bearing, double speedAtPointOne, double timeToTravel, double acceleration,boolean isParent) {
		double brngRad = toRadians(bearing);
		double latRad = toRadians(latitude);
		double lonRad = toRadians(longitude);
		int earthRadiusInMetres = 6371000;
		double distFrac = distanceInMetres / earthRadiusInMetres;

		double latitudeResult = asin(sin(latRad) * cos(distFrac) + cos(latRad) * sin(distFrac) * cos(brngRad));
		double a = atan2(sin(brngRad) * sin(distFrac) * cos(latRad), cos(distFrac) - sin(latRad) * sin(latitudeResult));
		double longitudeResult = (lonRad + a + 3 * PI) % (2 * PI) - PI;

		 System.out.println("next point after  "+distanceInMetres);
		// + toDegrees(longitudeResult));
		ElasticGranularModel jsonObj = createGranularPoint(mongoGranularModel, toDegrees(latitudeResult),
				toDegrees(longitudeResult), speedAtPointOne, timeToTravel, bearing, acceleration,isParent);
		return jsonObj;
	}

	ElasticGranularModel createGranularPoint(MongoGranularModel mongoGranularModel, double lat, double lng,
			double speed,  double timeGlobal,double bearing, double accl,boolean isParent) {
		ElasticGranularModel elasticGranularModel = new ElasticGranularModel();
		elasticGranularModel.setCarId(mongoGranularModel.getCarId());
		if(mongoGranularModel.getStepSize()!=null)elasticGranularModel.setStepSize(mongoGranularModel.getStepSize());
		if(mongoGranularModel.getTripNo()!=null)elasticGranularModel.setTripNo(mongoGranularModel.getTripNo());
		if(mongoGranularModel.getStartAtSec()!=null)elasticGranularModel.setStartAt(mongoGranularModel.getStartAtSec());
		elasticGranularModel.setRelativeTime(timeGlobal);
		double actual_time=mongoGranularModel.getStartAtSec() + timeGlobal;
		elasticGranularModel.setAcutualTime(actual_time);
		elasticGranularModel.setBearing(bearing);
		elasticGranularModel.setAcceleration(accl);
		elasticGranularModel.setLat(lat);
		elasticGranularModel.setLng(lng);
//		elasticGranularModel.setGeoPoint(new GeoPoint(lat, lng));
		elasticGranularModel.setChildId(childPointsCount);
		elasticGranularModel.setParent(isParent);
		elasticGranularModel.setSpeed(speed);
		elasticGranularModel.setCreatedAt(new Timestamp(System.currentTimeMillis()));
		elasticGranularModel.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
		elasticGranularModel.setV2xServer(mongoGranularModel.getV2xServer());
		elasticGranularModel.setGpsCanServer(mongoGranularModel.getGpsCanServer());
		elasticGranularModel.setRemoteIp(mongoGranularModel.getRemoteIp());
		elasticGranularModel.setRemotePass(mongoGranularModel.getRemotePass());
		elasticGranularModel.setRemotePath(mongoGranularModel.getRemotePath());
		elasticGranularModel.setRemoteUser(mongoGranularModel.getRemoteUser());
		elasticGranularModel.setParentUserId(mongoGranularModel.getParentUserId());
		elasticGranularModel.setEmailId(mongoGranularModel.getEmailId());
		elasticGranularModel.setName(mongoGranularModel.getName());
		
//		LOG.info("lat , lng childs with parent: "+lat+" , "+ lng);

		if(isParent)createGranularGPSPoint(lat, lng, String.valueOf(mongoGranularModel.getCarId()),speed);
//		createGranularGPSPoint(lat, lng, String.valueOf(mongoGranularModel.getCarId()),speed);

		return elasticGranularModel;
	}
	
	JSONObject createGranularGPSPoint(double lat, double lng, String fileName,Double speed) {
		
		
		
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("lat", lat);
		jsonObj.put("lng", lng);
		jsonObj.put("speed", speed);
		
		//****************************** for my testing ***************************
//		jsonObj.put("lat", String.valueOf(lat));
//		jsonObj.put("lng", String.valueOf(lng));
//		LOG.info("lat , lng parent: "+lat+" , "+ lng);
//		jsonObj.put("speed", speed);
		jsonArray.add(jsonObj);
//		System.out.println("points===>"+jsonObj);

		return jsonObj;
	}

	/**
	 * Get the formatted String value from double. Only for print/return as string
	 * purpose
	 * 
	 * @param value
	 * @return
	 */
	String getStringFromDouble(double value) {
		DecimalFormat df = new DecimalFormat("#");
		df.setMaximumFractionDigits(10);
		return df.format(value);
	}

	/**
	 * Get the distance between the two lat lng points, also considers elevation
	 * into account
	 *
	 * @param lat1
	 * @param lat2
	 * @param lon1
	 * @param lon2
	 * @param el1
	 * @param el2
	 * @return
	 */
	double getDistance(double lat1, double lat2, double lon1, double lon2, double el1, double el2) {

		final int R = 6371 * 1000; // Radius of the earth in meters

		double latDistance = toRadians(lat2 - lat1);
		double lonDistance = toRadians(lon2 - lon1);
		double a = sin(latDistance / 2) * sin(latDistance / 2)
				+ cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(lonDistance / 2) * sin(lonDistance / 2);
		double c = 2 * atan2(sqrt(a), sqrt(1 - a));
		double distance = R * c;

		double height = el1 - el2;

		distance = pow(distance, 2) + pow(height, 2);

		return sqrt(distance);
	}
}
