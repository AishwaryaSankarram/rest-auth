package com.carma.geoconfig.geoconfig.service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.springframework.stereotype.Service;

@Service
public class FileWriterUtil {
static String basePath="/apps/configFiles/";
static String gpsPath="/apps/gpsFiles/";

	public void configFileWriter(String fileName,String lineContent,String host,String user,String to,String pass) 
			  throws IOException {
			    String str = lineContent+"\n";
			    BufferedWriter writer = new BufferedWriter(new FileWriter(basePath+fileName+".json"));
			    writer.write(str);
			     
			    writer.close();
			    
			    
				new ScpUtil().scpRemote(user, host, to, pass,basePath+fileName+".json");
			}
	
	
	public void gpsFileWriter(String fileName,String lineContent,String host,String user,String to,String pass) 
			  throws IOException {
			    String str = lineContent;
			    BufferedWriter writer = new BufferedWriter(new FileWriter(gpsPath+fileName));
			    writer.write(str);
			     
			    writer.close();
			    
				String cmd="sed -i 's/},/}\\n/g' "+ to+fileName+" ; sed -i 's/\\[//g' "+ to+fileName+" ; sed -i 's/]/\\n/g' "+ to+fileName;
				new ScpUtil().scpRemote(user, host, to, pass,gpsPath+fileName);
//				System.out.println("scp command ==>"+cmd);
				new ScpCommandUtil().scpCommand(user, host, pass, cmd);

//				new ScpUtil().scpRemote(user, host, to, pass,basePath+fileName+".json");
			}
}
