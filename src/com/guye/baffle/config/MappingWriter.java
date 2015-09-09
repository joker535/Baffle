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
package com.guye.baffle.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.guye.baffle.exception.BaffleException;
import com.guye.baffle.obfuscate.Obfuscater;

public class MappingWriter {

	private Map<String, String> mKeyMaping;

	public MappingWriter(Map<String, String> mKeyMaping) {
		this.mKeyMaping = mKeyMaping;
	}

	public void WriteToFile(File file) throws BaffleException, IOException {

		if (file != null) {
			if (mKeyMaping.size() != 0) {
				StringBuilder stringBuilder = new StringBuilder();
				stringBuilder.append("#mapping 文件\n\n")
						.append("#key 段的mapping\n\n").append("---map_key\n\n");
				for (Map.Entry<String, String> entry : mKeyMaping.entrySet()) {
					stringBuilder.append(entry.getKey()).append(",")
							.append(entry.getValue()).append("\n");
				}
				stringBuilder.append("\n---map_key");
				FileOutputStream fos = new FileOutputStream(file);
				fos.write(stringBuilder.toString().getBytes());
				fos.flush();
				fos.close();
			}
		} else {
			Logger.getLogger(Obfuscater.LOG_NAME).log(Level.CONFIG,
					"WriteToFile:file-->" + file);
		}

	}
}
