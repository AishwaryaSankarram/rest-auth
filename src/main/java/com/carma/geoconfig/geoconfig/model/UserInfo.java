package com.carma.geoconfig.geoconfig.model;

import javax.swing.JOptionPane;

public class UserInfo implements com.jcraft.jsch.UserInfo{

	public UserInfo() {
		
	}
	public UserInfo(String pass) {
		this.passwd=pass;
	}
	String passwd;

	  


		public String getPassword() {
	        return passwd;
	    }

	    public boolean promptYesNo(String str) {
	        return true;
	    }


	    public String getPassphrase() {
	        return null;
	    }

	    public boolean promptPassphrase(String message) {
	        return true;
	    }

	    public boolean promptPassword(String message) {
	      return true;
	    }

	    public void showMessage(String message) {
	        JOptionPane.showMessageDialog(null, message);
	    }


}
