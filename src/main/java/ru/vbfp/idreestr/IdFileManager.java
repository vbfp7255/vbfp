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

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.Set;
import static ru.vbfp.idreestr.IdReestr.getNewProcessId;

/**
 *
 * @author VB
 */
public class IdFileManager {
    
    private static final String WORK_DIR = "D:/id-ocr";
    private static final String REPORT_DIR = "D:/id-report";
    private static final String SRC_DIR = "D:/id-src-pdf";
    private static final String XLSX_REPORT_NAME = "id-reestr.xlsx";
    
    private static final String PDF_DIR = "pdf";
    private static final String PDF_RENAMED_DIR = "pdf-renamed";
    private static final String JPG_DIR = "jpg";
    private static final String TXT_TESS_DIR = "txt-tess";
    private static final String TXT_LINGVO_DIR = "txt-lingvo";
    private static final String XLS_DIR = "xls-tess";
    private static final String XLS_LINGVO_DIR = "xls-lingvo";
    private static final String XLS_VSN_DIR = "xls-vsn";
    private static final String XLS_REPORT_DIR = "report-xls";
    private static final String PDF_REPORT_DIR = "report-pdf";
    private static final String IS_PROCESSED = "01process.st";
    private static final String IS_PDF_RENAMED = "02pdfrenamed.st";
    private static final String IS_PDF_IMAGES_EXP = "03pdfimagesexp.st";
    private static final String IS_OCR_IMAGES = "04ocrimages.st";
    private static final String IS_TXT_TO_XLS = "05txttoxls.st";
    
    
    private static Path currentStorage;
    private static Path workContainerPath;
    
    private ConcurrentSkipListMap<Integer, Path> idinsystem;

    public IdFileManager() {
        this.idinsystem = new ConcurrentSkipListMap<>();
    }
    protected static String getReportDirStr(){
        return REPORT_DIR;
    }
    protected static String getWorkDirStr(){
        return WORK_DIR;
    }
    protected static String getReportName(){
        return getNewProcessId() + XLSX_REPORT_NAME;
    }
    private void addPath(Path innerPath){
        pathIsNotReadWriteLink(innerPath);
        this.idinsystem.put(innerPath.hashCode(), innerPath);
    }
    
    private Set getPathEntrySet(){
        return this.idinsystem.entrySet();
    }
    
    private Integer getCountPath(){
        return this.idinsystem.size();
    }
    
    private void readListInWorkDir() {
        Path workPath = Paths.get(WORK_DIR);
        long countInDir = 0L;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(workPath)) {
        for (Path entry : stream) {
            currentStorage = entry;
            if( !isStorageProcessed(entry) ){
                setProcessStady();
            }
            addPath(entry);
            countInDir++;
        }
        } catch (IOException | DirectoryIteratorException e) {
            e.printStackTrace();
            System.out.println("[ERROR] Can`t read count files in work directory");
        }
    }
    
    private static void pathIsNotReadWriteLink(Path innerWorkPath){
        if ( !Files.isReadable(innerWorkPath) ){
            System.out.println("[ERROR] File exist and it is not a Readable: " + innerWorkPath.toString());
            throw new RuntimeException("[ERROR] File exist and it is not a Readable: " + innerWorkPath.toString());
        }
        if ( !Files.isWritable(innerWorkPath) ){
            System.out.println("[ERROR] File exist and it is not a Writable: " + innerWorkPath.toString());
            throw new RuntimeException("[ERROR] File exist and it is not a Writable: " + innerWorkPath.toString());
        }
        if ( Files.isSymbolicLink(innerWorkPath) ){
            System.out.println("[ERROR] File exist and it is not a SymbolicLink: " + innerWorkPath.toString());
            throw new RuntimeException("[ERROR] File exist and it is a SymbolicLink: " + innerWorkPath.toString());
        }
    }
    
    private void initAndCheck(){
        Path workPath = Paths.get(WORK_DIR);
        if( !Files.exists(workPath, LinkOption.NOFOLLOW_LINKS)){
            try {
                Files.createDirectory(workPath);
            } catch (IOException ex) {
                ex.printStackTrace();
                System.out.println("[ERROR] Can`t create work directory: " + workPath.toString());
            }
        }
        if ( !Files.isDirectory(workPath, LinkOption.NOFOLLOW_LINKS) ){
            System.out.println("[ERROR] File exist and it is not a directory: " + workPath.toString());
            throw new RuntimeException("[ERROR] File exist and it is not a directory: " + workPath.toString());
        }
        workContainerPath = workPath;
    }
    
    
    protected void makeNewStorage(){
        initAndCheck();
        
        readListInWorkDir();
        /*if( this.idinsystem.isEmpty() ){
            currentStorage = createProcessStorage();
            addPath(currentStorage);
            if( !isStorageProcessed(currentStorage) ){
                setProcessStady();
            }
        }*/
        currentStorage = createProcessStorage();
        addPath(currentStorage);
        setProcessStady();
        //if txtToXls
        
    }
    protected void makeAnotherStorage(){
        currentStorage = createProcessStorage();
        addPath(currentStorage);
        if( !isStorageProcessed(currentStorage) ){
            setProcessStady();
        }
}
    private Path createProcessStorage(){
        String newStoragePath = getNewProcessId();
        int testInWorkDirCount = getCountPath();
            newStoragePath = newStoragePath + "-id-" + Integer.toString(testInWorkDirCount);
            Path workPath = Paths.get(WORK_DIR,newStoragePath);
            if( !Files.exists(workPath, LinkOption.NOFOLLOW_LINKS)){
                try {
                    Files.createDirectory(workPath);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    System.out.println("[ERROR] Can`t create work directory for Storage: " + workPath.toString());
                }

        }
        return workPath;
    }
    private Boolean isContainerDir(Path innerPath){
        Path wDir = Paths.get(WORK_DIR);
        if( innerPath.compareTo(wDir) == 0 ){
            return true;
        }
        return false;
    }
    private Boolean isTxtToXls(Path innerPath){
        Path isProcessed = Paths.get(innerPath.toString(),IS_TXT_TO_XLS);
        return Files.exists(isProcessed, LinkOption.NOFOLLOW_LINKS);
    }
    private Boolean isStoragePdfRenamed(Path innerPath){
        Path isProcessed = Paths.get(innerPath.toString(),IS_PDF_RENAMED);
        return Files.exists(isProcessed, LinkOption.NOFOLLOW_LINKS);
    }
    private Boolean isStoragePdfImagesExp(Path innerPath){
        Path isProcessed = Paths.get(innerPath.toString(),IS_PDF_IMAGES_EXP);
        return Files.exists(isProcessed, LinkOption.NOFOLLOW_LINKS);
    }
    private Boolean isStorageOcrImages(Path innerPath){
        Path isProcessed = Paths.get(innerPath.toString(),IS_OCR_IMAGES);
        return Files.exists(isProcessed, LinkOption.NOFOLLOW_LINKS);
    }
    private Boolean isStorageProcessed(Path innerPath){
        Path isProcessed = Paths.get(innerPath.toString(),IS_PROCESSED);
        return Files.exists(isProcessed, LinkOption.NOFOLLOW_LINKS);
    }
    private Path getCheckProcessStadyPath(){
        Path isProcessed = Paths.get(currentStorage.toString(),IS_PROCESSED);
        if( Files.exists(isProcessed, LinkOption.NOFOLLOW_LINKS) ){
            pathIsNotReadWriteLink(isProcessed);
            if( !Files.isDirectory(isProcessed, LinkOption.NOFOLLOW_LINKS) ){
                return isProcessed;
            }
        }
        throw new IllegalStateException("[ERROR]Not linked file " + isProcessed.toString()
        + " need access for read, write");
    }
    protected void setTxtToXls(){
        
        Path isProcessed = Paths.get(currentStorage.toString(),IS_TXT_TO_XLS);

        try {
            if( !Files.exists(isProcessed, LinkOption.NOFOLLOW_LINKS) ){
                Files.createFile(isProcessed);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("[ERROR] Can`t createFile " + isProcessed.toString());
        }
    }
    protected void setOCRImages(){
        
        Path isProcessed = Paths.get(currentStorage.toString(),IS_OCR_IMAGES);

        try {
            if( !Files.exists(isProcessed, LinkOption.NOFOLLOW_LINKS) ){
                Files.createFile(isProcessed);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("[ERROR] Can`t createFile " + isProcessed.toString());
        }
    }
    private void setPdfRenamed(){
        
        Path isProcessed = Paths.get(currentStorage.toString(),IS_PDF_RENAMED);

        try {
            if( !Files.exists(isProcessed, LinkOption.NOFOLLOW_LINKS) ){
                Files.createFile(isProcessed);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("[ERROR] Can`t createFile " + isProcessed.toString());
        }
    }
    protected void setImagesExp(){
        
        Path isProcessed = Paths.get(currentStorage.toString(),IS_PDF_IMAGES_EXP);

        try {
            if( !Files.exists(isProcessed, LinkOption.NOFOLLOW_LINKS) ){
                Files.createFile(isProcessed);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("[ERROR] Can`t createFile " + isProcessed.toString());
        }
    }
    
    private void setProcessStady(){
        
        Path isProcessed = Paths.get(currentStorage.toString(),IS_PROCESSED);

        try {
            Files.createFile(isProcessed);
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("[ERROR] Can`t createFile " + isProcessed.toString());
        }
        //JPG_DIR
        getDirForJpg();
        //PDF_DIR
        getDirForPdf();
        //PDF_REPORT_DIR
        getDirForPdfReport();
        //TXT_TESS_DIR
        getDirForTxtTesseract();
        //XLS_DIR
        getDirForXls();
        //PDF_RENAMED_DIR
        getDirForPdfRenamed();
    }
    protected Path getDirForXls(){
       return checkOrCreateSubWorkDir(XLS_DIR);
    }
    protected Path getDirForTxtTesseract(){
       return checkOrCreateSubWorkDir(TXT_TESS_DIR);
    }
    protected Path getDirForPdfReport(){
       return checkOrCreateSubWorkDir(PDF_REPORT_DIR);
    }
    protected Path getDirForPdf(){
       return checkOrCreateSubWorkDir(PDF_DIR);
    }
    protected Path getDirForJpg(){
       return checkOrCreateSubWorkDir(JPG_DIR);
    }
    protected Path getDirForPdfRenamed(){
       return checkOrCreateSubWorkDir(PDF_RENAMED_DIR);
    }
    protected Path getCurrentStorage(){
        return currentStorage;
    }
    protected Path choiceCurrentStorage(){
        Path workPath = Paths.get(WORK_DIR);
        long countInDir = 0L;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(workPath)) {
        for (Path entry : stream) {
            
            if( !isStorageProcessed(entry) ){
                System.out.println("[Warning] not processed " + entry.toString());
                currentStorage = entry;
                setProcessStady();
                return currentStorage;
            }
            if( !isTxtToXls(entry) ){
                currentStorage = entry;
                return currentStorage;
            }
            if( !isStoragePdfRenamed(entry) ){
                currentStorage = entry;
                return currentStorage;
            }
            if( !isStoragePdfImagesExp(entry) ){
                currentStorage = entry;
                return currentStorage;
            }
            if( !isStorageOcrImages(entry) ){
                currentStorage = entry;
                return currentStorage;
            }
        }
        } catch (IOException | DirectoryIteratorException e) {
            e.printStackTrace();
            System.out.println("[ERROR] Can`t read count files in work directory");
        }
        return Paths.get("D:/StopDoWhileNotDirMore");
    }
    private Path checkOrCreateOperationDir(String subDirName){
         Path forCheckOrCreateDir = Paths.get(subDirName);
        if( Files.exists(forCheckOrCreateDir, LinkOption.NOFOLLOW_LINKS) ){
            pathIsNotReadWriteLink(forCheckOrCreateDir);
            if( Files.isDirectory(forCheckOrCreateDir, LinkOption.NOFOLLOW_LINKS) ){
                return forCheckOrCreateDir;
            }
        }
        try {
            Files.createDirectory(forCheckOrCreateDir);
            pathIsNotReadWriteLink(forCheckOrCreateDir);
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("[ERROR] Can`t createDirectory " + forCheckOrCreateDir.toString());
        }
        return forCheckOrCreateDir;
    }
    private Path checkOrCreateSubWorkDir(String subDirName){
         Path forCheckOrCreateDir = Paths.get(currentStorage.toString(),subDirName);
        if( Files.exists(forCheckOrCreateDir, LinkOption.NOFOLLOW_LINKS) ){
            pathIsNotReadWriteLink(forCheckOrCreateDir);
            if( Files.isDirectory(forCheckOrCreateDir, LinkOption.NOFOLLOW_LINKS) ){
                return forCheckOrCreateDir;
            }
        }
        try {
            Files.createDirectory(forCheckOrCreateDir);
            pathIsNotReadWriteLink(forCheckOrCreateDir);
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("[ERROR] Can`t createDirectory " + forCheckOrCreateDir.toString());
        }
        return forCheckOrCreateDir;
    }
    protected ArrayList<Path> listFilesInWorkPdfRenamedDir() {
        ArrayList<Path> listToReturn = new ArrayList<>();
        Path workPath = getDirForPdfRenamed();
        System.out.println("Storage contained in PDF-RENAMED directory " + workPath.toString());
        System.out.println("files: ");
        int count = 0;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(workPath,"*.{pdf}")) {
        for (Path entry : stream) {
            System.out.println(entry.toString());
            count++;
            listToReturn.add(entry);
        }
        if( count == 0 ){
            System.out.println("Directory is Empty, put some pdf files into " + workPath.toString());
        }
        } catch (IOException | DirectoryIteratorException e) {
            e.printStackTrace();
            System.out.println("[ERROR] Can`t read count files in work directory");
        }
        return listToReturn;
    }
    protected ArrayList<Path> listFilesInWorkJpegDir() {
        ArrayList<Path> listToReturn = new ArrayList<>();
        Path workPath = getDirForJpg();
        System.out.println("Storage contained in JPG directory " + workPath.toString());
        System.out.println("files: ");
        int count = 0;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(workPath,"*.{jpg}")) {
        for (Path entry : stream) {
            System.out.println(entry.toString());
            count++;
            listToReturn.add(entry);
        }
        if( count == 0 ){
            System.out.println("Directory is Empty, put some jpeg files into " + workPath.toString());
        }
        } catch (IOException | DirectoryIteratorException e) {
            e.printStackTrace();
            System.out.println("[ERROR] Can`t read count files in work directory");
        }
        return listToReturn;
    }
    
    protected ArrayList<Path> listFilesInSubDirTxtTesseractInAllStorages() {
        ArrayList<Path> listToReturn = new ArrayList<>();
        Path workPath = Paths.get(WORK_DIR);
        Path choicedStorage;
        long countInDir = 0L;
        try (DirectoryStream<Path> streamStorages = Files.newDirectoryStream(workPath)) {
        for (Path entryStorages : streamStorages) {
            choicedStorage = entryStorages;
            Path storagePath = Paths.get(entryStorages.toString(),TXT_TESS_DIR);
            int countFilesTxt = 0;
            try (DirectoryStream<Path> streamFiles = Files.newDirectoryStream(storagePath,"*.{txt}")) {
            for (Path entryFiles : streamFiles) {
                //System.out.println(entryFiles.toString());
                countFilesTxt++;
                listToReturn.add(entryFiles);
            }
            if( countFilesTxt == 0 ){
                System.out.println("listFilesInSubDirTxtTesseractInAllStorages() Directory is Empty, put some txt files into " + storagePath.toString());
            }
            } catch (IOException | DirectoryIteratorException e) {
                e.printStackTrace();
                System.out.println("[ERROR] Can`t read count files in work directory");
            }
            
            countInDir++;
        }
        } catch (IOException | DirectoryIteratorException e) {
            e.printStackTrace();
            System.out.println("[ERROR] Can`t read count files in work directory");
        }
        
        //Path workPath = getDirForTxtTesseract();
        //System.out.println("Storage contained in TXT_TESS directory " + workPath.toString());
        //System.out.println("files: ");
        
        return listToReturn;
    }
    
    protected ArrayList<Path> listFilesInWorkTxtTesseractDir() {
        ArrayList<Path> listToReturn = new ArrayList<>();
        Path workPath = getDirForTxtTesseract();
        System.out.println("Storage contained in TXT_TESS directory " + workPath.toString());
        System.out.println("files: ");
        int count = 0;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(workPath,"*.{txt}")) {
        for (Path entry : stream) {
            System.out.println(entry.toString());
            count++;
            listToReturn.add(entry);
        }
        if( count == 0 ){
            System.out.println("Directory is Empty, put some txt files into " + workPath.toString());
        }
        } catch (IOException | DirectoryIteratorException e) {
            e.printStackTrace();
            System.out.println("[ERROR] Can`t read count files in work directory");
        }
        return listToReturn;
    }
    
    protected void fetchPdfFromSrcDir(){
        Path pdfSrcDir = checkOrCreateOperationDir(SRC_DIR);
        System.out.println("Storage contained in PDF directory " + pdfSrcDir.toString());
        System.out.println("files: ");
        int count = 0;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(pdfSrcDir,"*.{pdf}")) {
        for (Path entry : stream) {
            
            makeNewStorage();
            Path workStorage = getCurrentStorage();
            if( workStorage == null ){
                throw new RuntimeException("[ERROR] Can`t init work storage, getCurrentStorage() result is null");
            }
            System.out.println("New storage created " + workStorage.toString());
        
            System.out.println(entry.toString());
            count++;
            //if !pdfRenamed then rename
            Path dirForRename = getDirForPdfRenamed();
            Path destinationPath = Paths.get(dirForRename.toString(), getNewProcessId() + "-id-" + count + ".pdf");
            copyFileFromSrcToDest(entry, destinationPath);
            setPdfRenamed();
            // Add to job pdf renamed file
            writeStady(entry
                    + "|||||"
                    + destinationPath
                    + "|||||" 
                    + getNewProcessId()
            );
            
            
        }
        if( count == 0 ){
            System.out.println("Directory is Empty, put some pdf files into " + pdfSrcDir.toString());
        }
        } catch (IOException | DirectoryIteratorException e) {
            e.printStackTrace();
            System.out.println("[ERROR] Can`t read count files in work directory");
        }
    }
    
    protected void listFilesInWorkPdfDir() {
        Path workPath = getDirForPdf();
        System.out.println("Storage contained in PDF directory " + workPath.toString());
        System.out.println("files: ");
        int count = 0;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(workPath,"*.{pdf}")) {
        for (Path entry : stream) {
            System.out.println(entry.toString());
            count++;
            //if !pdfRenamed then rename
            Path dirForRename = getDirForPdfRenamed();
            Path destinationPath = Paths.get(dirForRename.toString(), getNewProcessId() + "-id-" + count + ".pdf");
            copyFileFromSrcToDest(entry, destinationPath);
            setPdfRenamed();
            writeStady("[STADYPDFRENAME][SRCFILE]" + entry
                    + "[DSTFILE]"
                    + destinationPath
                    + "[COPYFILE][STARTAT]" 
                    + getNewProcessId()
            );
        }
        if( count == 0 ){
            System.out.println("Directory is Empty, put some pdf files into " + workPath.toString());
        }
        } catch (IOException | DirectoryIteratorException e) {
            e.printStackTrace();
            System.out.println("[ERROR] Can`t read count files in work directory");
        }
    }
    private void writeStady(String strSatdy){
        Path checkProcessStadyPath = getCheckProcessStadyPath();
        List<String> lines = new ArrayList<>();
        try {
            lines.addAll(Files.readAllLines(checkProcessStadyPath, Charset.forName("UTF-8")));
        } catch (IOException ex) {
            ex.getMessage();
            ex.printStackTrace();
        }
        lines.add(strSatdy);
        try {
            Files.write(checkProcessStadyPath, lines, Charset.forName("UTF-8"));
        } catch (IOException ex) {
            ex.getMessage();
            ex.printStackTrace();
        }
    }
    private void copyFileFromSrcToDest(Path srcPath, Path destPath){
        try {
            Files.copy(srcPath, destPath, REPLACE_EXISTING, COPY_ATTRIBUTES);
        } catch (UnsupportedOperationException ex) {
            ex.printStackTrace();
            System.out.println("[ERROR] Can`t copy files from "
            + srcPath.toString()
            + " to " + destPath.toString());
        } catch (FileAlreadyExistsException ex) {
            ex.printStackTrace();
            System.out.println("[ERROR] Can`t copy files from "
            + srcPath.toString()
            + " to " + destPath.toString());
        } catch (DirectoryNotEmptyException ex) {
            ex.printStackTrace();
            System.out.println("[ERROR] Can`t copy files from "
            + srcPath.toString()
            + " to " + destPath.toString());
        } catch (SecurityException ex) {
            ex.printStackTrace();
            System.out.println("[ERROR] Can`t copy files from "
            + srcPath.toString()
            + " to " + destPath.toString());
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("[ERROR] Can`t copy files from "
            + srcPath.toString()
            + " to " + destPath.toString());
        }
    }
}
