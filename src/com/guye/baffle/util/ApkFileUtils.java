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
package com.guye.baffle.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.guye.baffle.decoder.MakeCsc;
import com.guye.baffle.webp.WebpIO;

public class ApkFileUtils {
    public static List<ZipInfo> unZipApk( File apkFile, String tempDir ) throws IOException {
        ZipFile in = new ZipFile(apkFile);
        File out = new File(tempDir);
        File outFile;
        BufferedOutputStream bos;
        InputStream input;
        int length = -1;
        byte[] buffer = new byte[8192];
        Enumeration<? extends ZipEntry> entries = in.entries();
        List<ZipInfo> zipInfos = new ArrayList<ZipInfo>(100);
        ZipInfo info;
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();

            if (entry.isDirectory() || entry.getName().startsWith("META-INF")) {
                continue;
            }
            outFile = new File(out, entry.getName());
            if (!outFile.getParentFile().exists()) {
                outFile.getParentFile().mkdirs();
            }
            bos = new BufferedOutputStream(new FileOutputStream(outFile));
            MessageDigest md5 = null;
            try {
                md5 = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                // do nothing
            }
            DigestOutputStream digestOutputStream = new DigestOutputStream(bos, md5);
            input = in.getInputStream(entry);
            while (true) {
                length = input.read(buffer);
                if (length == -1)
                    break;
                digestOutputStream.write(buffer, 0, length);
            }
            digestOutputStream.close();
            input.close();
            if (!entry.isDirectory()) {
                if((entry.getName().startsWith("res/")) &&  ((entry.getName().endsWith(".png") && !entry.getName().endsWith(".9.png")) || entry.getName().endsWith(".jpg") || entry.getName().endsWith(".jpeg"))){
                    File newOutfile = new File(outFile.getParentFile() , getName(outFile.getName()));
                    boolean hasChange = WebpIO.toWebp(outFile, newOutfile);
                    if(hasChange){
                        long crc = 0;
                        try {
                            crc = MakeCsc.getFileCRCCode(newOutfile);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        info = new ZipInfo(entry.getName(), entry.getMethod(), newOutfile.length(),
                                crc, toHex(md5.digest()));
                    }else{
                        info = new ZipInfo(entry.getName(), entry.getMethod(), entry.getSize(),
                                entry.getCrc(), toHex(md5.digest()));
                    }
                }else{
                    info = new ZipInfo(entry.getName(), entry.getMethod(), entry.getSize(),
                            entry.getCrc(), toHex(md5.digest()));
                }
                
                zipInfos.add(info);
            }
        }
        in.close();
        return zipInfos;
    }

    private static String getName( String name ) {
        name = name.substring(0,name.indexOf('.'));
        return name+".webp";
    }

    /**
     * md5 摘要转16进制
     * 
     * @param digest
     * @return
     */
    private static String toHex( byte[] digest ) {
        StringBuilder sb = new StringBuilder();
        int len = digest.length;

        String out = null;
        for (int i = 0; i < len; i++) {
            // out = Integer.toHexString(0xFF & digest[i] + 0xABCDEF); //加任意
            // salt
            out = Integer.toHexString(0xFF & digest[i]);// 原始方法
            if (out.length() == 1) {
                sb.append("0");// 如果为1位 前面补个0
            }
            sb.append(out);
        }
        return sb.toString();
    }
}
