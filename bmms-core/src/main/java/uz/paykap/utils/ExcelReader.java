package uz.paykap.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class ExcelReader {

    public List<Map<String, String>> readExcel(String path) {
        try {
            File excelFile = new File(path);
            FileInputStream fis = null;
            fis = new FileInputStream(excelFile);
            // we create an XSSF Workbook object for our XLSX Excel File
            XSSFWorkbook workbook = new XSSFWorkbook(fis);
            // we get first sheet
            XSSFSheet sheet = workbook.getSheetAt(0);
            // we iterate on rows
            Iterator<Row> rowIt = sheet.iterator();
            List<Map<String, String>> rows = new ArrayList<>();
            List<String> keymap = new ArrayList<>();

            int countRow = 0;
            while (rowIt.hasNext()) {
                Row row = rowIt.next();
                // iterate on cells for the current row
                Iterator<Cell> cellIterator = row.cellIterator();
                Map<String, String> cells = new HashMap<>();
                int cellCount = 0;
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    if (countRow == 0) {
                        keymap.add(cell.toString());
                    } else {
                        cells.put(keymap.get(cellCount), cell.toString());
                    }
                    cellCount++;
                }
                rows.add(cells);
                countRow++;
            }
            workbook.close();
            fis.close();
            rows.removeAll(Collections.singleton(null));
            return rows;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
