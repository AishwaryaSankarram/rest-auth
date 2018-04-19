package com.carma.geoconfig.geoconfig.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

public class MapData {

	private String signalId;
	private String intersection_arch;
	private GeoJsonPoint pos;
	private String placeId;
	
	
	public String getSignalId() {
		return signalId;
	}
	public void setSignalId(String signalId) {
		this.signalId = signalId;
	}
	public String getIntersection_arch() {
		return intersection_arch;
	}
	public void setIntersection_arch(String intersection_arch) {
		this.intersection_arch = intersection_arch;
	}
	public GeoJsonPoint getPos() {
		return pos;
	}
	public void setPos(GeoJsonPoint pos) {
		this.pos = pos;
	}
	public String getPlaceId() {
		return placeId;
	}
	public void setPlaceId(String placeId) {
		this.placeId = placeId;
	}

	
}
