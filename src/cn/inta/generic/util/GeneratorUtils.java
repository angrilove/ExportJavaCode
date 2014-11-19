/*
 * Copyright (C) 2014.
 * Write by Anshen
 * All Right Reseved.
 * 
 */
package cn.inta.generic.util;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class GeneratorUtils {

	private static Properties info;

	private static Connection connection;

	static {
		info = new Properties();
		try {
			info.load(new FileReader("jdbc.properties"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getProperty(String key) {
		return info.getProperty(key);
	}

	public static Connection getConnection() {
		try {
			Class.forName(info.getProperty("driver"));
			connection = DriverManager.getConnection(info.getProperty("url"),
					info);
			connection.setCatalog("information_schema");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return connection;
	}

	public static ResultSet query(PreparedStatement pstm) {
		try {
			pstm.execute();
			return pstm.getResultSet();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void closeConnection() {
		try {
			if (null != connection && !connection.isClosed()) {
				connection.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
