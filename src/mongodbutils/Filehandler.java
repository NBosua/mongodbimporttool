/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mongodbutils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author NBosua
 */
public class Filehandler {

    MongodbConnection mc = null;

    public boolean processFile(String filePath, MongodbConnection mc, String strdbName, String strCollName) throws IOException {
        this.mc = mc;

        FileInputStream fileIn = null;
        try {
            fileIn = new FileInputStream(filePath);
            POIFSFileSystem fs = new POIFSFileSystem(fileIn);
            HSSFWorkbook wb = new HSSFWorkbook(fs);
            HSSFSheet sheet = wb.getSheetAt(0);

            String strValue = "";
            //Read in first row as field names
            Row rowH = sheet.getRow(sheet.getFirstRowNum());
            String fields[] = new String[sheet.getRow(0).getLastCellNum()];
            for (Cell cell : rowH) {
                strValue = getCellValue(cell);
                fields[cell.getColumnIndex()] = strValue;
            }

            //loop thru all cells with values
            int rowcount = 0;
            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue; //skip first row
                }
                JSONObject obj = new JSONObject();

                for (Cell cell : row) {
                    if (fields.length<cell.getColumnIndex()) continue; //only export column if we have header set
                    
                    strValue = getCellValue(cell);
                    if (!strValue.equals("")) {
                        if (strValue.contains("$date")) {
                            JSONParser parser = new JSONParser();
                            try {
                                obj.put(fields[cell.getColumnIndex()], parser.parse(strValue));
                            } catch (ParseException ex) {
                                Logger.getLogger(Filehandler.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        } else {
                            obj.put(fields[cell.getColumnIndex()], strValue);
                        }
                    }
                }
                rowcount += 1;
                mc.insertJSON(strdbName, strCollName, obj.toJSONString());
            }

            return true;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Filehandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Filehandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
            Logger.getLogger(Filehandler.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            if (fileIn != null) {
                fileIn.close();
            }
        }
        return false;
    }

    private String getCellValue(Cell cell) {

        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                return cell.getRichStringCellValue().getString();
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    Date dt = cell.getDateCellValue();

                    JSONObject obj = new JSONObject();
                    obj.put("$date", dt.getTime());
                    return obj.toString();

                    //return "" + cell.getDateCellValue();
                } else {
                    return "" + cell.getNumericCellValue();
                }
            case Cell.CELL_TYPE_BOOLEAN:
                return "" + cell.getBooleanCellValue();
            case Cell.CELL_TYPE_FORMULA:
                return cell.getCellFormula();
        }
        return "";
    }

}
