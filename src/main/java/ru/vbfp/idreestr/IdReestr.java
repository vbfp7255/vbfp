/*
 * Copyright 2018 VB.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import java.util.concurrent.ConcurrentSkipListMap;

import java.util.logging.Level;
import java.util.logging.Logger;

 
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.IndexedColors;


/**
 *
 * @author VB
 */
public class IdReestr {
    
    private static final String PDF_IMAGES = "D:/id-kms-exec/forPdfToJpeg/pdfimages.exe";
    private static final String PARAM_SPLITTER = " ";
    private static final String PDF_IMAGES_OPTION = "-j";
    private static final String TESSERACT_FILE = "\"C:/Program Files (x86)/Tesseract-OCR/tesseract.exe\"";
    private static final String TESSERACT_FILE_OPTION = "-l rus";
    
    private final IdFileManager idFM;
    
    IdReestr(){
        this.idFM = new IdFileManager();
    }
    
    
    public static void main(String[] args){
        IdReestr createdIdReestrIteration = new IdReestr();
        /*idReestrIteration.idFM.makeNewStorage();
        Path workStorage = idReestrIteration.idFM.getCurrentStorage();
        if( workStorage == null ){
            throw new RuntimeException("[ERROR] Can`t init work storage, getCurrentStorage() result is null");
        }
        System.out.println("New storage created " + workStorage.toString());
        //idReestrIteration.idFM.listFilesInWorkPdfDir();*/
        
        createdIdReestrIteration.idFM.fetchPdfFromSrcDir();
        int compareTo = 13;
        do{
            Path choiceCurrentStorage = createdIdReestrIteration.idFM.choiceCurrentStorage();
            Path get = Paths.get("D:/StopDoWhileNotDirMore");
            compareTo = get.compareTo(choiceCurrentStorage);
            Path currentStorage = createdIdReestrIteration.idFM.getCurrentStorage();
            System.out.println("****->>>>Now work in " + currentStorage.toString());
            imgToXls(createdIdReestrIteration);
        }while( compareTo != 0 );
        System.out.println("****->>>>Finished");
    }
    
    //
    private static void imgToXls(IdReestr idReestrIteration){
        ArrayList<Path> listFilesInWorkPdfRenamedDir = idReestrIteration.idFM.listFilesInWorkPdfRenamedDir();
        ArrayList<Path> chekDirJpg = idReestrIteration.idFM.listFilesInWorkJpegDir();
        if( chekDirJpg.isEmpty() ){
            if( !listFilesInWorkPdfRenamedDir.isEmpty() ){
                String cmdToRun = PDF_IMAGES
                        + PARAM_SPLITTER
                        + PDF_IMAGES_OPTION
                        + PARAM_SPLITTER
                        + listFilesInWorkPdfRenamedDir.get(0).toString()
                        + PARAM_SPLITTER
                        + idReestrIteration.idFM.getDirForJpg().toString() + "\\"
                        ;

                RunPdfToJpeg(cmdToRun);
            }
        }
        idReestrIteration.idFM.setImagesExp();
        ArrayList<Path> listFilesInWorkJpegDir = idReestrIteration.idFM.listFilesInWorkJpegDir();
        ArrayList<Path> filesInWorkTxtTesseractDir = idReestrIteration.idFM.listFilesInWorkTxtTesseractDir();
        if( filesInWorkTxtTesseractDir.isEmpty() ){
            if( !listFilesInWorkJpegDir.isEmpty() ){
                for (Path pathJpegName : listFilesInWorkJpegDir) {
                    String strFileName = pathJpegName.getFileName().toString();
                    String strFileNameNotExt = strFileName.replaceAll(".jpg", "");
                    Path dstPath = Paths.get(idReestrIteration.idFM.getDirForTxtTesseract().toString(), 
                            strFileNameNotExt);
                    String cmdToRun = TESSERACT_FILE
                            + PARAM_SPLITTER
                            + pathJpegName
                            + PARAM_SPLITTER
                            + dstPath.toString()
                            + PARAM_SPLITTER
                            + TESSERACT_FILE_OPTION
                            ;
                    RunTesseract(cmdToRun);
                }
            }
        }
        idReestrIteration.idFM.setOCRImages();
        ArrayList<Path> filesTesseractDir = idReestrIteration.idFM.listFilesInWorkTxtTesseractDir();
        for (Path pathTessTxtName : filesTesseractDir) {
            Path dirForXls = idReestrIteration.idFM.getDirForXls();
            Path fileName = pathTessTxtName.getFileName();
            String strFileName = fileName.toString();
            String strFileNameNotExt = strFileName.replaceAll(".txt", "");
            Path fileXlsName = Paths.get(dirForXls.toString(), strFileNameNotExt);
            List<String> linesReaded = new ArrayList<>();
            try {
                linesReaded.addAll(Files.readAllLines(pathTessTxtName, Charset.forName("UTF-8")));
            } catch (IOException ex) {
                ex.getMessage();
                ex.printStackTrace();
            }
            linesReaded.add(pathTessTxtName.toString());
            writeFromTesseractTxtToXls(linesReaded, fileXlsName);
        }
        idReestrIteration.idFM.setTxtToXls();
    }
    private static HSSFCellStyle getSampleStyle(HSSFWorkbook workbook) {
        // Font
        HSSFFont font = workbook.createFont();
        font.setBold(false);
        font.setItalic(false);
 
        // Font Height
        font.setFontHeightInPoints((short) 12);
 
        // Font Color
        font.setColor(IndexedColors.BLACK.index);
 
        // Style
        HSSFCellStyle style = workbook.createCellStyle();
        style.setFont(font);
 
        return style;
    }
    public static void writeFromTesseractTxtToXls(List<String> innerLines, Path xlsFile){
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Style Demo");
 
        HSSFRow row = sheet.createRow(0);
        HSSFCell cell = row.createCell(0);
        int rowNum = 0;
        for (String innerLine : innerLines) {
            rowNum++;
            row = sheet.createRow(rowNum);
            cell = row.createCell(0);
            cell.setCellValue(innerLine);
            HSSFCellStyle style = getSampleStyle(workbook);
            cell.setCellStyle(style);
        }
        //
        
        
 
        
        String newFilePath = xlsFile.toString() + "-" + getNewProcessId() + ".xls";
        Path pathNewFile = Paths.get(newFilePath);
        File file = new File(newFilePath);
        try {
            if( !Files.exists(pathNewFile, LinkOption.NOFOLLOW_LINKS ) ){
                Files.createFile(pathNewFile);
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
        
        FileOutputStream outFile;
        try {
            outFile = new FileOutputStream(file);
            try {
                workbook.write(outFile);
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
                ex.printStackTrace();
            }
        } catch (FileNotFoundException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
        
        System.out.println("Created file: " + file.getAbsolutePath());
    }
    public static void readXlsToConsole() throws IOException {
        // Read XSL file
        FileInputStream inputStream = new FileInputStream(new File("D://vbfpid/employee.xls"));
 
        // Get the workbook instance for XLS file
        HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
 
        // Get first sheet from the workbook
        HSSFSheet sheet = workbook.getSheetAt(0);
 
        // Get iterator to all the rows in current sheet
        Iterator<Row> rowIterator = sheet.iterator();
 
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            // Get iterator to all cells of current row
            Iterator<Cell> cellIterator = row.cellIterator();
 
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
 
                // Change to getCellType() if using POI 4.x
                CellType cellType = cell.getCellType();
 
                switch (cellType) {
                case _NONE:
                    System.out.print("");
                    System.out.print("\t");
                    break;
                case BOOLEAN:
                    System.out.print(cell.getBooleanCellValue());
                    System.out.print("\t");
                    break;
                case BLANK:
                    System.out.print("");
                    System.out.print("\t");
                    break;
                case FORMULA:
                    // Formula
                    System.out.print(cell.getCellFormula());
                    System.out.print("\t");
                     
                    FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
                    // Print out value evaluated by formula
                    System.out.print(evaluator.evaluate(cell).getNumberValue());
                    break;
                case NUMERIC:
                    System.out.print(cell.getNumericCellValue());
                    System.out.print("\t");
                    break;
                case STRING:
                    System.out.print(cell.getStringCellValue());
                    System.out.print("\t");
                    break;
                case ERROR:
                    System.out.print("!");
                    System.out.print("\t");
                    break;
                }
 
            }
            System.out.println("");
        }
    }
    public static String getNewProcessId(){
        long currentDateTime = System.currentTimeMillis();
      
       //creating Date from millisecond
       Date currentDate = new Date(currentDateTime);
      
       //printing value of Date
       //System.out.println("current Date: " + currentDate);
      
       DateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss");
       
      
       //formatted value of current Date
       return df.format(currentDate);
    }
    public static void RunPdfToJpeg(String strCmd){
        try {
            Process p = Runtime.getRuntime().exec(strCmd);
            int waitFor = 0;
            try {
                waitFor = p.waitFor();
            } catch (InterruptedException ex) {
               System.out.println(ex.getMessage());
               ex.printStackTrace();
            }
            OutputStream outputStream = p.getOutputStream();
            
            System.out.println("Command " + strCmd + " end with " + Integer.toString(waitFor));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            //Logger.getLogger(IdReestr.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public static void RunTesseract(String strCmd){
        try {
            Process p = Runtime.getRuntime().exec(strCmd);
            int waitFor = 0;
            try {
                waitFor = p.waitFor();
            } catch (InterruptedException ex) {
               System.out.println(ex.getMessage());
               ex.printStackTrace();
            }
            OutputStream outputStream = p.getOutputStream();
            
            System.out.println("Command " + strCmd + " end with " + Integer.toString(waitFor));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            //Logger.getLogger(IdReestr.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
}
