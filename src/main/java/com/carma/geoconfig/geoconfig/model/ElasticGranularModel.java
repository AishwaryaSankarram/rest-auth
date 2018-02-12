package com.carma.geoconfig.geoconfig.model;

import java.sql.Timestamp;

import org.springframework.data.annotation.Id;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ElasticGranularModel {
	@Id
	public String id;
	
	public Timestamp createdAt;

	public Timestamp updatedAt;
	
	public long carId;

	public int stepSize;

	public double startAt;
	
	public Double speed;

	public double lat;

	public double lng;

	public Double acceleration;

	public long tripNo;

	public double acutualTime;

	public double bearing;

	public double relativeTime;
	
	public boolean parent;
	
	public int childId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

	public Timestamp getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Timestamp updatedAt) {
		this.updatedAt = updatedAt;
	}

	public long getCarId() {
		return carId;
	}

	public void setCarId(long carId) {
		this.carId = carId;
	}

	public int getStepSize() {
		return stepSize;
	}

	public void setStepSize(int stepSize) {
		this.stepSize = stepSize;
	}

	public double getStartAt() {
		return startAt;
	}

	public void setStartAt(double startAt) {
		this.startAt = startAt;
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

	public long getTripNo() {
		return tripNo;
	}

	public void setTripNo(long tripNo) {
		this.tripNo = tripNo;
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

	public int getChildId() {
		return childId;
	}

	public void setChildId(int childId) {
		this.childId = childId;
	}
	
	
}
