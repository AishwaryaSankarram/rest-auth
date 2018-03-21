package com.carma.geoconfig.geoconfig.model;

import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

public class UserAddress {

	private GeoJsonPoint location;
	
	private String placeId;
	
	private String address;

	public GeoJsonPoint getLocation() {
		return location;
	}

	public void setLocation(GeoJsonPoint location) {
		this.location = location;
	}

	public String getPlaceId() {
		return placeId;
	}

	public void setPlaceId(String placeId) {
		this.placeId = placeId;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
	
	
}
