package com.carma.geoconfig.geoconfig.service;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;

import com.carma.geoconfig.geoconfig.model.MongoGranularModel;

@Service
public class ConfigGeneratorService {
	String configData="{\"sys-settings\":{\"tgt_id\":\"12345\",\"tgt_type\":\"RSU\",\"trace_level\":\"2\",\"redirect_std_err\":\"0\",\"sys_mode\":\"1\",\"pkt_type\":\"0\",\"server_client_enable\":\"2\"},\"server-db\":{\"ldm_server\":\"0\",\"ldm_port\":\"5001\",\"v2x_server\":\"0\",\"v2x_port\":\"5003\",\"data_recv_server\":\"192.168.1.80\",\"data_recv_port\":\"5010\",\"data_mirror_server\":\"0\",\"data_mirror_port\":\"5011\",\"gps_can_server\":\"0\",\"gps_can_port\":\"5012\"},\"interface0\":{\"if_enable\":\"0\",\"if_index\":\"0\",\"if_name\":\"ath0\"},\"interface1\":{\"if_enable\":\"1\",\"if_index\":\"1\",\"if_name\":\"ath1\"},\"bsm\":{\"stream_enable\":\"1\",\"stream_name\":\"BSM\",\"stream_type\":\"0\",\"tx_enable\":\"1\",\"rx_enable\":\"1\",\"psid\":\"32\",\"if_index\":\"1\",\"service_channel\":\"172\",\"tx_service_channel\":\"172\"},\"rsa\":{\"stream_enable\":\"1\",\"stream_name\":\"RSA\",\"stream_type\":\"1\",\"tx_enable\":\"1\",\"rx_enable\":\"1\",\"psid\":\"40\",\"if_index\":\"1\",\"service_channel\":\"172\",\"tx_service_channel\":\"172\"}}";
	public JSONObject generateConfig(MongoGranularModel mongoGranularModel) throws ParseException {
	
		JSONObject wholeObj = (JSONObject) new JSONParser().parse(configData);
		JSONObject sysSettings=(JSONObject) wholeObj.get("sys-settings");
		JSONObject serverDbSetting=(JSONObject) wholeObj.get("server-db");
		sysSettings.put("tgt_id", String.valueOf(mongoGranularModel.getCarId()));
		serverDbSetting.put("ldm_server", mongoGranularModel.getV2xServer());
		serverDbSetting.put("ldm_port", "5001");
		serverDbSetting.put("v2x_server", mongoGranularModel.getV2xServer());
		serverDbSetting.put("v2x_port", "5003");
		serverDbSetting.put("gps_can_server", mongoGranularModel.getGpsCanServer());
		serverDbSetting.put("gps_can_port", "5012");
		wholeObj.put("sys-settings", sysSettings);
		wholeObj.put("server-db", serverDbSetting);
		return wholeObj;
	}

}
