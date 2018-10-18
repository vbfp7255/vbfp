/*
 * Copyright 2018 vbfp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.vbfp.idreestr;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author vbfp
 */
public class IdReporter {
    private ArrayList<Path> filesForReport;
    public IdReporter(ArrayList<Path> filesInWorkTxtTesseractDir) {
        filesForReport = filesInWorkTxtTesseractDir;
    }
    
    protected void processFileFromList(){
        for (Path elementFile : this.filesForReport) {
            String strFileName = elementFile.toString();
            System.out.println(strFileName);
        }
    }
    
    protected void reportWriterXlsx(){
        Path excelFile = Paths.get(IdFileManager.getWorkDirStr(),IdFileManager.getReportName());
        try {
            if( !Files.exists(excelFile, LinkOption.NOFOLLOW_LINKS ) ){
                Files.createFile(excelFile);
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
        
        String sheetName = "Reestr";
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet(sheetName) ;

        for (int r=0;r < 5; r++ )
        {
            XSSFRow row = sheet.createRow(r);
            //iterating c number of columns
            for (int c=0;c < 5; c++ )
            {
                XSSFCell cell = row.createCell(c);
                cell.setCellValue("Cell "+r+" "+c);
            }
        }

        Iterator<Row> itr = sheet.iterator();
        
        try (FileOutputStream fOutStream = new FileOutputStream(excelFile.toFile())){
            wb.write(fOutStream);
        } catch (FileNotFoundException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
        

    }
    
}
