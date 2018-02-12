package com.carma.geoconfig.geoconfig.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class MongoTripDataModel {
	public MongoTripDataModel() {
		
	}
	
	public MongoTripDataModel(long carId1,long tripNo1) {
		this.carId=carId1;
		this.tripNo=tripNo1;
	}
	
	public long carId;
	
	public long tripNo;

	public long getCarId() {
		return carId;
	}

	public void setCarId(long carId) {
		this.carId = carId;
	}

	public long getTripNo() {
		return tripNo;
	}

	public void setTripNo(long tripNo) {
		this.tripNo = tripNo;
	}
	
	
	
}
