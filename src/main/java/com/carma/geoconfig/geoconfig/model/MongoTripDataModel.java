package com.carma.geoconfig.geoconfig.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class MongoTripDataModel {
	public MongoTripDataModel() {
		
	}
	
	public MongoTripDataModel(String carId1,long tripNo1) {
		this.carId=carId1;
		this.tripNo=tripNo1;
	}
	
	public String carId;
	
	public long tripNo;


	public String getCarId() {
		return carId;
	}

	public void setCarId(String carId) {
		this.carId = carId;
	}

	public long getTripNo() {
		return tripNo;
	}

	public void setTripNo(long tripNo) {
		this.tripNo = tripNo;
	}
	
	
	
}
