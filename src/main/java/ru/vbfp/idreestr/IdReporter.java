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

import java.nio.file.Path;
import java.util.ArrayList;

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
    
}