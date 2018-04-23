package com.carma.geoconfig.geoconfig.model;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Document
@JsonIgnoreProperties(ignoreUnknown = true)
public class Scenario {
	
	private String name;
	
	private UserAddress userAddress;
	
	@DBRef
	private List<MongoGranularModel> cars;

	@Id
	@JsonIgnore
	private String id;
	
	private String scenarioId;
	
	private boolean deleted;
	
	@CreatedDate @NotNull
	private Date createdAt;
	
	@LastModifiedDate @NotNull
	private Date updatedAt;
	
	private String parentUserId;
	
	public List<MapData> mapDataList;

	public List<MapData> getMapDataList() {
		return mapDataList;
	}

	public void setMapDataList(List<MapData> mapDataList) {
		this.mapDataList = mapDataList;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public UserAddress getUserAddress() {
		return userAddress;
	}

	public void setUserAddress(UserAddress userAddress) {
		this.userAddress = userAddress;
	}

	public List<MongoGranularModel> getCars() {
		return cars;
	}

	public void setCars(List<MongoGranularModel> cars) {
		this.cars = cars;
	}

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

	public String getParentUserId() {
		return parentUserId;
	}

	public void setParentUserId(String parentUserId) {
		this.parentUserId = parentUserId;
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

	

}
