package com.excelLoad.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;

import javafx.util.Callback;

public class ExcelLoadDao {

	private static Connection connection;
	private static ExcelLoadDao excelLoadDao;

	public static ExcelLoadDao getInstance() throws Exception {

		connection = SingleConnection.getConnection();

		if (excelLoadDao == null) {
			excelLoadDao = new ExcelLoadDao();
		}
		return excelLoadDao;
	}

	public void createTempTable(String t_load_excel) {
		PreparedStatement createTempTable = null;
		try {
			createTempTable = connection.prepareStatement(t_load_excel);
			createTempTable.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				createTempTable.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void salvar(List<Cell> cells, String tempTable) throws SQLException {

		PreparedStatement insert = connection.prepareStatement(tempTable);

		int i = 0;
		for (Cell dado : cells) {

			try {
				if (dado.getCellType().toString() == "BLANK") {
					break;
				}
				switch (dado.getCellType().toString()) {
				case "STRING":
					insert.setString(++i, dado.getStringCellValue());
					break;
				case "NUMERIC":
					insert.setDouble(++i, dado.getNumericCellValue());
					break;
				case "BOOLEAN":
					insert.setBoolean(++i, dado.getBooleanCellValue());
					break;
				case "FORMULA":
					insert.setDouble(++i, Double.valueOf(dado.getCachedFormulaResultType().toString()));
					break;
				case "DATE":
					insert.setDate(++i, (Date) dado.getDateCellValue());
					break;
				}
			} catch (Exception e) {
				e.getMessage();
			}
		}
		insert.execute();
	}

	public List<String> dadosTemp() {

		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		List<String> resultadoTemp = new ArrayList<String>();

		try {

			preparedStatement = connection.prepareStatement("SELECT * FROM tmpLoadExcel");
			resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {

				String linha = "";
				for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
					linha += " " + resultSet.getString(i);
				}
				resultadoTemp.add(linha);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {

				if (resultSet != null) {
					resultSet.close();
				}
				if (preparedStatement != null) {
					preparedStatement.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}

		return resultadoTemp;
	}
}
