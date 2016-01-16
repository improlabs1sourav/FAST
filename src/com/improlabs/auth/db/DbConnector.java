package com.improlabs.auth.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public abstract class DbConnector {
	protected String hostName;
	protected String port;
	protected String dbName;
	protected String username;
	protected String password;

	private Connection connection;
	
	//private DbConnector(){}

	public Connection connectToDB(String hostName, String port, String username, String password, String dbName) {
		if (connection == null) {
			try {
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				String connectionUrl = "jdbc:mysql://" + hostName + ":" + port + "/" + dbName;
				connection = DriverManager.getConnection(connectionUrl, username, password);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return connection;

	}

	public boolean insertIntoDB(String query) {
		try {

			PreparedStatement ps = connection.prepareStatement(query);
			ps.executeUpdate();
			ps.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean updateIntoDB(String query) {
		try {

			PreparedStatement ps = connection.prepareStatement(query);
			ps.executeUpdate();
			ps.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void closeConnection() {
		try {
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
