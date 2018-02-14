package com.carma.geoconfig.geoconfig.model;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@Document
@JsonIgnoreProperties(ignoreUnknown = true)
public class MongoGranularModel{
	
	
	@Id
	@JsonIgnore
	public String id;
		
	@CreatedDate
	public Date createdAt;
	
	@LastModifiedDate
	public Date updatedAt;
	
	public long carId;
	
	public long tripNo;

	public int stepSize;
	
	public double startAtSec;
	
	public String v2xServer;
	
	public String gpsCanServer;
	
	public String remotePath;
	
	public String remoteIp;
	
	public String remotePass;
	
	public String remoteUser;
	
	public String emailId;
	
	public String name;
	
	@JsonIgnore
	public String parentUserId;

//	@CascadeSave
	@DBRef
	public List<MongoGranularChildModel> poly;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
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

	public int getStepSize() {
		return stepSize;
	}

	public void setStepSize(int stepSize) {
		this.stepSize = stepSize;
	}

	public double getStartAtSec() {
		return startAtSec;
	}

	public void setStartAtSec(double startAtSec) {
		this.startAtSec = startAtSec;
	}

	public List<MongoGranularChildModel> getPoly() {
		return poly;
	}

	public void setPoly(List<MongoGranularChildModel> poly) {
		this.poly = poly;
	}

	public String getV2xServer() {
		return v2xServer;
	}

	public void setV2xServer(String v2xServer) {
		this.v2xServer = v2xServer;
	}

	public String getGpsCanServer() {
		return gpsCanServer;
	}

	public void setGpsCanServer(String gpsCanServer) {
		this.gpsCanServer = gpsCanServer;
	}

	public String getRemotePath() {
		return remotePath;
	}

	public void setRemotePath(String remotePath) {
		this.remotePath = remotePath;
	}

	public String getRemotePass() {
		return remotePass;
	}

	public void setRemotePass(String remotePass) {
		this.remotePass = remotePass;
	}

	public String getRemoteUser() {
		return remoteUser;
	}

	public void setRemoteUser(String remoteUser) {
		this.remoteUser = remoteUser;
	}

	public String getRemoteIp() {
		return remoteIp;
	}

	public void setRemoteIp(String remoteIp) {
		this.remoteIp = remoteIp;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getParentUserId() {
		return parentUserId;
	}

	public void setParentUserId(String parentUserId) {
		this.parentUserId = parentUserId;
	}
	
	
}
