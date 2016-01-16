package com.improlabs.auth.db;

public class SessionDB extends DbConnector {
	
	
	public SessionDB(String hostName, String port, String username, String password, String dbName)
	{
		this.hostName=hostName;
		this.port=port;
		this.username=username;
		this.password=password;
		this.dbName=dbName;
	}
	public void select()
	{
		
	}

}
