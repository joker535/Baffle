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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ApkFileUtils {
	public static List<ZipInfo> unZipApk(File apkFile, String tempDir)
			throws IOException {
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
			input = in.getInputStream(entry);
			while (true) {
				length = input.read(buffer);
				if (length == -1)
					break;
				bos.write(buffer, 0, length);
			}
			if (!entry.isDirectory()) {
				info = new ZipInfo(entry.getName(), entry.getMethod(),
						entry.getSize(), entry.getCrc());
				zipInfos.add(info);
			}
			bos.close();
			input.close();
		}
		in.close();
		return zipInfos;
	}
}
