package com.carma.geoconfig.geoconfig.service.utils;

import java.io.InputStream;

import org.springframework.stereotype.Service;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

//@Service
public class SshCommandUtil {

	public void scpCommand(String user,String host, String pass, String cmd) {
		// TODO Auto-generated method stub
//			String command1 = "sed -i 's/},/}\\n/g' /tmp/123 ; sed -i 's/\\[//g' /tmp/123 ; sed -i 's/]/\\n/g' /tmp/123";
		try {

			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			JSch jsch = new JSch();
			Session session = jsch.getSession(user, host, 22);
			session.setPassword(pass);
			session.setConfig(config);
			session.connect();
			System.out.println("command for file edit : "+cmd);

			Channel channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(cmd);
			channel.setInputStream(null);
			((ChannelExec) channel).setErrStream(System.err);

			InputStream in = channel.getInputStream();
			channel.connect();
			byte[] tmp = new byte[1024];
			while (true) {
				while (in.available() > 0) {
					int i = in.read(tmp, 0, 1024);
					if (i < 0)
						break;
					System.out.print(new String(tmp, 0, i));
				}
				if (channel.isClosed()) {
					System.out.println("exit-status: " + channel.getExitStatus());
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (Exception ee) {
				}
			}
			channel.disconnect();
			session.disconnect();
			System.out.println("DONE");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
