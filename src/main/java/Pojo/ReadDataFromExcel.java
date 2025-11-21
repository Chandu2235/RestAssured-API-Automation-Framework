package Pojo;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReadDataFromExcel {
    
    private String filePath;
    
    public ReadDataFromExcel(String filePath) {
        this.filePath = filePath;
    }
    
    public ReadDataFromExcel() {
        // Default constructor
    }
    
    /**
     * Reads all data from specified sheet
     */
    public List<Map<String, String>> readExcelData(String sheetName) {
        List<Map<String, String>> data = new ArrayList<>();
        
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                System.out.println("Sheet '" + sheetName + "' not found");
                return data;
            }
            
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                System.out.println("No header row found");
                return data;
            }
            
            // Get headers
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(getCellValueAsString(cell));
            }
            
            // Read data rows
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    Map<String, String> rowData = new HashMap<>();
                    for (int j = 0; j < headers.size(); j++) {
                        Cell cell = row.getCell(j);
                        String cellValue = getCellValueAsString(cell);
                        rowData.put(headers.get(j), cellValue);
                    }
                    data.add(rowData);
                }
            }
            
        } catch (IOException e) {
            System.err.println("Error reading Excel file: " + e.getMessage());
        }
        
        return data;
    }
    
    /**
     * Reads specific cell value
     */
    public String getCellValue(String sheetName, int rowNum, int colNum) {
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            Sheet sheet = workbook.getSheet(sheetName);
            Row row = sheet.getRow(rowNum);
            Cell cell = row.getCell(colNum);
            
            return getCellValueAsString(cell);
            
        } catch (IOException e) {
            System.err.println("Error reading cell: " + e.getMessage());
            return "";
        }
    }
    
    /**
     * Helper method to convert cell value to string
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
    
    public static void main(String[] args) {
        // Example usage
        ReadDataFromExcel excelReader = new ReadDataFromExcel("path/to/your/excel/file.xlsx");
        
        // Read all data from a sheet
        List<Map<String, String>> testData = excelReader.readExcelData("TestData");
        
        // Print the data
        for (Map<String, String> row : testData) {
            System.out.println(row);
        }
        
        // Read specific cell
        String cellValue = excelReader.getCellValue("TestData", 1, 0);
        System.out.println("Cell value: " + cellValue);
    }
}
