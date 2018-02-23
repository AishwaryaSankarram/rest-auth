package com.carma.geoconfig.geoconfig.service.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.carma.geoconfig.geoconfig.utils.PropUtils;

@Service
public class FileWriterUtil {
static String configPath=PropUtils.getVal("configPath");
static String gpsPath=PropUtils.getVal("gpsPath");
// static String configPath="/apps/configFiles/";
// static String gpsPath="/apps/gpsFiles/";
private static final Logger log = LoggerFactory.getLogger(FileWriterUtil.class);

	public void configFileWriter(String fileName,String lineContent,String host,String user,String to,String pass) 
			  throws IOException {
			    String str = lineContent+"\n";
			    BufferedWriter writer = new BufferedWriter(new FileWriter(configPath+fileName+".json"));
			    writer.write(str);
			     
			    writer.close();
			    
			    
				new ScpUtil().scpRemote(user, host, to, pass,configPath+fileName+".json");
			}
	
	
	public void gpsFileWriter(String fileName,String lineContent,String host,String user,String to,String pass) 
			  throws IOException {
			    String str = lineContent;
			    BufferedWriter writer = new BufferedWriter(new FileWriter(gpsPath+fileName));
			    writer.write(str);
			     
			    writer.close();
			    
//				String cmd="sed -i 's/},/}\\n/g' "+ to+fileName+" ; sed -i 's/\\[//g' "+ to+fileName+" ; sed -i 's/]/\\n/g' "+ to+fileName;
				new ScpUtil().scpRemote(user, host, to, pass,gpsPath+fileName);
//				System.out.println("scp command ==>"+cmd);
//				new ScpCommandUtil().scpCommand(user, host, pass, cmd);

//				new ScpUtil().scpRemote(user, host, to, pass,basePath+fileName+".json");
			}
}
