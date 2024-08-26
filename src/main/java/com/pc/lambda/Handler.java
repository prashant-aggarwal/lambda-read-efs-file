package com.pc.lambda;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Handler implements RequestHandler<String, String> {

	private static Gson gson = null;
	private static LambdaLogger logger = null;

	public String handleRequest(String filePath, Context context) {

		logger = context.getLogger();
		gson = new GsonBuilder().setPrettyPrinting().create();

		logger.log("\nEvent => " + gson.toJson(filePath));
		logger.log("\nContext => " + gson.toJson(context));

		Path newFilePath = Paths.get(filePath);
		try {
			if (Files.exists(newFilePath)) {
				logger.log("\nFile Available: " + newFilePath.toAbsolutePath().toString());

				readExcelFile(filePath);
				logger.log("\nFile read successfully: " + filePath);
			} else {
				logger.log("\nFile Unavailable: " + newFilePath.toAbsolutePath().toString());
			}
		} catch (Exception e) {
			logger.log("\nException while performing file operations: " + e.getMessage());
		}

		logger.log("\n\n");
		return "Processed successfully";
	}

	private void readExcelFile(String filePath) throws FileNotFoundException, IOException {
		try (FileInputStream file = new FileInputStream(new File(filePath))) {
			logger.log("\nFile Input Stream Created");

			// Create Workbook instance holding reference to .xlsx file
			try (XSSFWorkbook workbook = new XSSFWorkbook(file)) {

				logger.log("\nWorkbook Opened");
				
				// Get first/desired sheet from the workbook
				XSSFSheet sheet = workbook.getSheetAt(0);

				logger.log("\nIteration Started");

				// Iterate through each rows one by one
				Iterator<Row> rowIterator = sheet.iterator();
				while (rowIterator.hasNext()) {
					Row row = rowIterator.next();
					// For each row, iterate through all the columns
					Iterator<Cell> cellIterator = row.cellIterator();

					while (cellIterator.hasNext()) {
						Cell cell = cellIterator.next();
						// Check the cell type and format accordingly
						switch (cell.getCellType()) {
						case Cell.CELL_TYPE_NUMERIC:
							logger.log(cell.getNumericCellValue() + "\t");
							break;
						case Cell.CELL_TYPE_STRING:
							logger.log(cell.getStringCellValue() + "\t");
							break;
						}
					}
					logger.log("\n ");
				}
				logger.log("\nIteration Ended");
			}
		}
	}
}
