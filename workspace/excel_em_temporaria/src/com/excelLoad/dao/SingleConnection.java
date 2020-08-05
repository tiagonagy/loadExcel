package com.excelLoad.dao;

import java.sql.Connection;
import java.sql.DriverManager;

public class SingleConnection {

	private static String url = "jdbc:mysql://localhost:3306/posjava?useTimezone=true&serverTimezone=UTC";
	private static String user = "root";
	private static String password = "1234";
	private static Connection connection = null;

	static {
		conectar();
	}

	public SingleConnection() {
		conectar();
	}

	private static void conectar() {

		try {
			if (connection == null) {
				Class.forName("com.mysql.cj.jdbc.Driver");
				connection = DriverManager.getConnection(url, user, password);
				System.out.println("Conectado");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Erro ao conectar ao BD");
		}

	}

	public static Connection getConnection() {
		return connection;
	}
}
