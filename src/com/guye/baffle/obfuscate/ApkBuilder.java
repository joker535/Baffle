/**
 *  Baffle Project
 *  The MIT License (MIT) Copyright (Baffle) 2015 guye
 *  
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 *  and associated documentation files (the "Software"), to deal in the Software 
 *  without restriction, including without limitation the rights to use, copy, modify, 
 *  merge, publish, distribute, sublicense, and/or sell copies of the Software, and to 
 *  permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *  
 *  The above copyright notice and this permission notice shall be included in all copies 
 *  or substantial portions of the Software.
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 *  INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 *  PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
 *  FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 *  ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 *  @author guye <ny0705@gmail.com>
 *
 **/
package com.guye.baffle.obfuscate;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import com.guye.baffle.util.ZipInfo;

public class ApkBuilder {
    
    private static final int BUFFER = 4086;
    private List<ZipInfo> zipinfos;
    private ZipInfo arscInfo ;
    private ObfuscateHelper obfuscater;

    public ApkBuilder(ObfuscateHelper obfuscater , List<ZipInfo> zipinfos, ZipInfo arscInfo ){
        this.zipinfos = zipinfos;
        this.arscInfo = arscInfo;
        this.obfuscater = obfuscater;
    }
    
    
    public void reBuildapk(String apkFilepath, String dir ) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(apkFilepath);  
        JarOutputStream out = new JarOutputStream(fileOutputStream); 
        File file;
        
        for (ZipInfo zipInfo : zipinfos) {
            
            try {
                file = new File(dir + zipInfo.getKey("res"));
                JarEntry jarEntry ;
                if(zipInfo.type == null){
                    jarEntry = new JarEntry(zipInfo.getKey(obfuscater.getResKey()));
                }else{
                    jarEntry = new JarEntry(getNewKey(zipInfo));
                }
               
                if(zipInfo != null && zipInfo.name.equals("resources.arsc")){
                    file = new File(dir + "resources.n.arsc");
                    jarEntry.setMethod(ZipEntry.STORED);
                    jarEntry.setSize(arscInfo.size);
                    jarEntry.setCompressedSize(arscInfo.size);
                    jarEntry.setCrc((long)arscInfo.crc);
                }else if(zipInfo!= null && (zipInfo.zipMethod == ZipEntry.STORED)){
                    jarEntry.setMethod(ZipEntry.STORED);
                    jarEntry.setSize(zipInfo.size);
                    jarEntry.setCompressedSize(zipInfo.size);
                    jarEntry.setCrc((long)zipInfo.crc);
                }
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));  
                jarEntry.setTime(file.lastModified());
                out.putNextEntry(jarEntry);  
                int count;  
                byte data[] = new byte[BUFFER];  
                while ((count = bis.read(data, 0, BUFFER)) != -1) {  
                    out.write(data, 0, count);  
                }  
                bis.close();  
            } catch (Exception e) {  
                throw new RuntimeException(e);  
            }  
            
        }
        
        out.close();
        fileOutputStream.close();
        
    }


    private String getNewKey( ZipInfo zipInfo ) {
        return obfuscater.getNewKey(zipInfo);
    }
}
