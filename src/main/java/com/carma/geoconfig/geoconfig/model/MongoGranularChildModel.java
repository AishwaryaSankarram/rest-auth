package com.carma.geoconfig.geoconfig.model;



import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Document
@JsonIgnoreProperties(ignoreUnknown = true)
public class MongoGranularChildModel {

	@Id
	@JsonIgnore
	public String id;

	public Double speed;
	
	public double lat;
	
	public double lng;
		
	public Double acceleration;

	public long carId;

	public long tripNo;

	public double startAt;

	public double acutualTime;

	public double bearing;
	
	public boolean parent;
	
	public int childId;

	public double relativeTime;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Double getSpeed() {
		return speed;
	}

	public void setSpeed(Double speed) {
		this.speed = speed;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLng() {
		return lng;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}

	public Double getAcceleration() {
		return acceleration;
	}

	public void setAcceleration(Double acceleration) {
		this.acceleration = acceleration;
	}

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

	public double getStartAt() {
		return startAt;
	}

	public void setStartAt(double startAt) {
		this.startAt = startAt;
	}

	public double getAcutualTime() {
		return acutualTime;
	}

	public void setAcutualTime(double acutualTime) {
		this.acutualTime = acutualTime;
	}

	public double getBearing() {
		return bearing;
	}

	public void setBearing(double bearing) {
		this.bearing = bearing;
	}

	

	public int getChildId() {
		return childId;
	}

	public void setChildId(int childId) {
		this.childId = childId;
	}

	public double getRelativeTime() {
		return relativeTime;
	}

	public void setRelativeTime(double relativeTime) {
		this.relativeTime = relativeTime;
	}

	public boolean isParent() {
		return parent;
	}

	public void setParent(boolean parent) {
		this.parent = parent;
	}

	
	
}
