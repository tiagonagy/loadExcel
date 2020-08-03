package com.excelLoad.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.excelLoad.dao.ExcelLoadDao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class ExcelLoadController implements Initializable {

	private Stage thisStage;
	@FXML
	private AnchorPane rootPane;
	@FXML
	private Button btnCarregarArquivo;
	@FXML
	private Button btnSalvar;
	@FXML
	private ListView<String> listViewExcel = new ListView<String>();
	@FXML
	private ListView<String> listViewTemp = new ListView<String>();

	private static List<Row> rows;
	private static String createTempTable;
	private static String insertTempTable;

	public ExcelLoadController() {
		thisStage = new Stage();

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/excelLoad/view/ExcelLoadView.fxml"));
			loader.setController(this);
			thisStage.setScene(new Scene(loader.load()));
			thisStage.setMaximized(false);
			thisStage.resizableProperty().setValue(Boolean.FALSE);
			thisStage.showAndWait();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}

	@FXML
	private void abrirArquivo(ActionEvent event) throws IOException {

		listViewTemp.getItems().clear();
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Selecione a planilha");
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XLS", "*.xls", "XLSX", "*.xlsx"));
		File file = fileChooser.showOpenDialog(null);

		if (file != null) {
			try {
				listViewExcel.setItems(FXCollections.observableArrayList(upload(file)));
			} catch (IOException e) {
				e.printStackTrace();
				btnSalvar.setDisable(true);

			}
		}
		btnSalvar.setDisable(false);
	}

	private static List<String> upload(File file) throws IOException {
		List<String> listaDadosPlanilha = new ArrayList<String>();

		// pegando o arquivo
		FileInputStream arquivo = new FileInputStream(file);
		// *.XLS
		HSSFWorkbook hworkbook;
		// *.XLSX
		XSSFWorkbook xworkbook;
		Sheet sheet;

		if (FilenameUtils.getExtension(file.toString()).equalsIgnoreCase("xls")) {
			hworkbook = new HSSFWorkbook(arquivo);
			// Selecionando a Aba
			sheet = hworkbook.getSheetAt(0);

		} else {
			xworkbook = new XSSFWorkbook(arquivo);
			// Selecionando a Aba
			sheet = xworkbook.getSheetAt(0);
		}

		// setando as linhas
		rows = (List<Row>) toList(sheet.iterator());

		// PERCORRENDO A LINHA
		rows.forEach(row -> {
			try {
				if (row.getCell(0).getCellType().toString() != "BLANK") {
					List<Cell> cells = (List<Cell>) toList(row.cellIterator());

					String linha = "";
					for (Cell dado : cells) {
						try {
							if (dado.getCellType().toString() == "BLANK") {
								break;
							}
							switch (dado.getCellType().toString()) {
							case "STRING":
								linha += "	" + dado.getStringCellValue();
								break;
							case "NUMERIC":
								linha += "	" + dado.getNumericCellValue();
								break;
							case "BOOLEAN":
								linha += "	" + dado.getBooleanCellValue();
								break;
							case "FORMULA":
								linha += "	" + dado.getCachedFormulaResultType().toString();
								break;
							case "DATE":
								linha += "	" + dado.getDateCellValue();
								break;
							}
						} catch (Exception e) {
							e.getMessage();
						}
					}
					if (linha != "") {
						listaDadosPlanilha.add(linha);
					}

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		return listaDadosPlanilha;
	}

	private static List<?> toList(Iterator<?> iterator) {
		return IteratorUtils.toList(iterator);

	}

	private static void criarTempExcel(List<Row> rows) {

		List<Cell> colunas = (List<Cell>) toList(rows.get(0).cellIterator());
		StringBuilder cTempTable = new StringBuilder();
		StringBuilder iTempTable = new StringBuilder();

		cTempTable.append("CREATE TEMPORARY TABLE tmpLoadExcel(");
		iTempTable.append("INSERT INTO tmpLoadExcel VALUES (");

		for (int i = 0; i < colunas.size(); i++) {

			switch (colunas.get(i).getCellType().toString()) {
			case "STRING":
				cTempTable.append(colunas.get(i).getAddress() + " CHAR(255),");
				iTempTable.append("?,");
				break;
			case "NUMERIC":
				cTempTable.append(colunas.get(i).getAddress() + " DECIMAL(10,5),");
				iTempTable.append("?,");
				break;
			case "BOOLEAN":
				cTempTable.append(colunas.get(i).getAddress() + " CHAR(01),");
				iTempTable.append("?,");
				break;
			case "FORMULA":
				cTempTable.append(colunas.get(i).getAddress() + " DECIMAL(10,5),");
				iTempTable.append("?,");
				break;
			case "DATE":
				cTempTable.append(colunas.get(i).getAddress() + " DATE,");
				iTempTable.append("?,");
				break;
			}
		}

		createTempTable = cTempTable.substring(0, cTempTable.length() - 1) + ");";
		insertTempTable = iTempTable.substring(0, iTempTable.length() - 1) + ");";

		System.out.println(createTempTable);
		System.out.println(insertTempTable);

		try {
			ExcelLoadDao.getInstance().createTempTable(createTempTable);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@FXML
	private void salvar() {
		criarTempExcel(rows);
		rows.forEach(row -> {
			try {
				if (row.getCell(0).getCellType().toString() != "BLANK") {
					List<Cell> cells = (List<Cell>) toList(row.cellIterator());
					try {
						ExcelLoadDao.getInstance().salvar(cells, insertTempTable);
					} catch (SQLException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				JOptionPane.showInputDialog(null, "Erro ao inserir:" + e.getMessage());
			}
		});
		JOptionPane.showMessageDialog(null, "Salvo com sucesso");
		btnSalvar.setDisable(true);

		try {
			listViewTemp.setItems(FXCollections.observableArrayList(ExcelLoadDao.getInstance().dadosTemp()));
			listViewExcel.getItems().clear();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
