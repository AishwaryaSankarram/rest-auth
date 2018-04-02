package com.carma.geoconfig.geoconfig.service.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carma.geoconfig.geoconfig.model.UserInfo;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;


public class ScpUtil {
	FileInputStream fis = null;
    private static final Logger log = LoggerFactory.getLogger(ScpUtil.class);

	public void scpRemote(String user,String host, String to,String pass,String source) throws Exception {
		try {

//			System.out.println("----"+source);
			String from = source;
//			String user = PropUtils.getVal("remote_user");
//			String host = PropUtils.getVal("remote_host");
//			String to = PropUtils.getVal("remote_path");

			JSch jsch = new JSch();
			Session session = jsch.getSession(user, host, 22);

			// username and password will be given via UserInfo interface.
			com.jcraft.jsch.UserInfo ui = new UserInfo(pass);
		
			session.setUserInfo(ui);
			session.connect();

			boolean ptimestamp = true;

			// exec 'scp -t rfile' remotely
//			String fileEdit = " && sed -i 's/},/}\\n/g' /tmp/192.168.1.17__123456__4 && sed -i 's/\\[//g' /tmp/192.168.1.17__123456__4 && sed -i 's/]/\\n/g' /tmp/192.168.1.17__123456__4";
			String command = "scp -r " + (ptimestamp ? "-p" : "") + " -t " + to;
//			if(fileEdit!=null)command=command+fileEdit;
			Channel channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(command);
			log.info("command--->"+command);
			// get I/O streams for remote scp
			OutputStream out = channel.getOutputStream();
			InputStream in = channel.getInputStream();

			channel.connect();

			if (checkAck(in) != 0) {
//				System.exit(0);
			}

			File _lfile = new File(from);

			if (ptimestamp) {
				command = "T" + (_lfile.lastModified() / 1000) + " 0";
				// The access time should be sent here,
				// but it is not accessible with JavaAPI ;-<
				command += (" " + (_lfile.lastModified() / 1000) + " 0\n");
				out.write(command.getBytes());
				out.flush();
				if (checkAck(in) != 0) {
//					System.exit(0);
				}
			}

			// send "C0644 filesize filename", where filename should not include '/'
			long filesize = _lfile.length();
			command = "C0644 " + filesize + " ";
			if (from.lastIndexOf('/') > 0) {
				command += from.substring(from.lastIndexOf('/') + 1);
			} else {
				command += from;
			}
			command += "\n";
			out.write(command.getBytes());
			out.flush();
			if (checkAck(in) != 0) {
//				System.exit(0);
			}

			// send a content of lfile
			fis = new FileInputStream(from);
			byte[] buf = new byte[1024];
			while (true) {
				int len = fis.read(buf, 0, buf.length);
				if (len <= 0)
					break;
				out.write(buf, 0, len); // out.flush();
			}
			fis.close();
			fis = null;
			// send '\0'
			buf[0] = 0;
			out.write(buf, 0, 1);
			out.flush();
			if (checkAck(in) != 0) {
//				System.exit(0);
			}
			out.close();

//
//			((ChannelExec) channel).setCommand(command11);

			channel.disconnect();
			session.disconnect();

//			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			try {
				if (fis != null)
					fis.close();
			} catch (Exception ex) {
				ex.printStackTrace();
				throw new Exception("Scp failed");

			}
			throw new Exception("Scp failed");

		}
	}

	public int checkAck(InputStream in) throws IOException {
		int b = in.read();
		// b may be 0 for success,
		// 1 for error,
		// 2 for fatal error,
		// -1
		if (b == 0)
			return b;
		if (b == -1)
			return b;

		if (b == 1 || b == 2) {
			StringBuffer sb = new StringBuffer();
			int c;
			do {
				c = in.read();
				sb.append((char) c);
			} while (c != '\n');
			if (b == 1) { // error
				System.out.print(sb.toString());
			}
			if (b == 2) { // fatal error
				System.out.print(sb.toString());
			}
		}
		return b;
	}

}
