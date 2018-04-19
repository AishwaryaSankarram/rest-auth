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
	public String id;
		
	@CreatedDate
	public Date createdAt;
	
	@LastModifiedDate
	public Date updatedAt;
	
	public String carId;
	
	public Long tripNo;

	public Integer stepSize;
	
	public Double startAtSec;
	
	public String v2xServer;
	
	public String gpsCanServer;
	
	public String remotePath;
	
	public String remoteIp;
	
	public String remotePass;
	
	public String remoteUser;
	
	public String emailId;
	
	public String name;
	
	public String parentUserId;
	
	public String configFileName;
	
	public String geoFileName;
	
	public String command;
	
	public String carLabel;
	
	public String color;
	
	public String address;
	
	public String scenarioId;
	
	public boolean deleted;
	
	public Boolean useAsEv;

//	@CascadeSave
	
	public List<MongoGranularChildModel> poly;
	
	
	@DBRef
	public List<MongoGranularChildModel> granularPoints;
	
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

	

	public String getCarId() {
		return carId;
	}

	public void setCarId(String carId) {
		this.carId = carId;
	}

	public Long getTripNo() {
		return tripNo;
	}

	public void setTripNo(Long tripNo) {
		this.tripNo = tripNo;
	}

	public Integer getStepSize() {
		return stepSize;
	}

	public void setStepSize(Integer stepSize) {
		this.stepSize = stepSize;
	}

	public Double getStartAtSec() {
		return startAtSec;
	}

	public void setStartAtSec(Double startAtSec) {
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
	
	

	public String getConfigFileName() {
		return configFileName;
	}

	public void setConfigFileName(String configFileName) {
		this.configFileName = configFileName;
	}

	public String getGeoFileName() {
		return geoFileName;
	}

	public void setGeoFileName(String geoFileName) {
		this.geoFileName = geoFileName;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getCarLabel() {
		return carLabel;
	}

	public void setCarLabel(String carLabel) {
		this.carLabel = carLabel;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public List<MongoGranularChildModel> getGranularPoints() {
		return granularPoints;
	}

	public void setGranularPoints(List<MongoGranularChildModel> granularPoints) {
		this.granularPoints = granularPoints;
	}

	public String getScenarioId() {
		return scenarioId;
	}

	public void setScenarioId(String scenarioId) {
		this.scenarioId = scenarioId;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public Boolean getUseAsEv() {
		return useAsEv;
	}

	public void setUseAsEv(Boolean useAsEv) {
		this.useAsEv = useAsEv;
	}
	
	
	
	
}
