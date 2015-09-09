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

 *  @author guye
 *
 **/
package com.guye.baffle.decoder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;

import com.guye.baffle.obfuscate.ObfuscateHelper;
import com.guye.baffle.obfuscate.ObfuscateHelper.ObfuscateData;
import com.guye.baffle.util.LEDataOutputStream;

public class ArscFileGenerator {
	private ArscData arscData;
	private ObfuscateData obfuscateData;
	private static final Charset UTF_8_CHARSET = Charset.forName("UTF-8");

	public ArscFileGenerator(ArscData arscData, ObfuscateData obfuscateData) {
		this.arscData = arscData;
		this.obfuscateData = obfuscateData;
	}

	private StringBlock createStrings(StringBlock orgTableStrings,
			boolean isTableString) {
		try {
			ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
			LEDataOutputStream dataOutputStream = new LEDataOutputStream(
					arrayOutputStream);
			int count = orgTableStrings.getCount();
			int curOffset = 0;
			int[] offset = new int[count];
			String newStr;
			byte[] strData;
			byte[] l = new byte[2];
			byte[] l1 = new byte[2];
			int offsetLen = 1;
			int offsetDataLen = 1;
			for (int i = 0; i < count; i++) {
				if (isTableString) {
					newStr = getNewTableString(orgTableStrings.getString(i));
				} else {
					newStr = getNewKeyString(orgTableStrings.getString(i));
				}
				strData = newStr.getBytes(UTF_8_CHARSET);
				offset[i] = curOffset;
				if (newStr.length() < 128) {
					offsetLen = 1;
					l[0] = (byte) (0x7f & (newStr.length()));
				} else {
					offsetLen = 2;
					short len = (short) (newStr.length());
					l[0] = (byte) ((byte) ((len & 0xff00) >> 8) | 0x80);
					l[1] = (byte) (len & 0x00ff);
				}

				if (strData.length < 128) {
					l1[0] = (byte) (0x7f & (strData.length));
					offsetDataLen = 1;
				} else {
					offsetDataLen = 2;
					short len = (short) (strData.length);
					l1[0] = (byte) ((byte) ((len & 0xff00) >> 8) | 0x80);
					l1[1] = (byte) (len & 0x00ff);
				}
				dataOutputStream.write(l, 0, offsetLen);
				dataOutputStream.write(l1, 0, offsetDataLen);
				dataOutputStream.write(strData);
				dataOutputStream.write(0);
				curOffset += (offsetLen + offsetDataLen + strData.length + 1);
			}

			strData = arrayOutputStream.toByteArray();
			dataOutputStream.close();
			arrayOutputStream.close();

			return new StringBlock(offset, strData,
					orgTableStrings.getStyleOffset(),
					orgTableStrings.getStyle(), true);
		} catch (IOException e) {// not a disk IO option
			e.printStackTrace();
		}
		return null;
	}

	private String getNewKeyString(String orgString) {
		return obfuscateData.keyMaping.get(orgString);
	}

	private String getNewTableString(String orgString) {
		if (orgString.startsWith(ObfuscateHelper.RES_PROFIX)) {
			String[] names = orgString.split("/");
			if (names == null || names.length != 3) {
				throw new RuntimeException(); // TODO
			}
			String[] newNames = new String[3];
			newNames[0] = obfuscateData.resName;
			newNames[1] = obfuscateData.typeMaping.get(names[1]);
			if (newNames[1] == null) {
				throw new RuntimeException(); // TODO
			}
			int index = names[2].indexOf('.');
			String postfix = "";
			if (index > 0) {
				postfix = names[2].substring(index, names[2].length());
				names[2] = names[2].substring(0, index);
			}
			newNames[2] = obfuscateData.keyMaping.get(names[2]);
			if (newNames[2] == null) {
				throw new RuntimeException(); // TODO
			}
			String newString = new StringBuilder().append(newNames[0])
					.append('/').append(newNames[1]).append('/')
					.append(newNames[2]).append(postfix).toString();
			return newString;
		} else {
			return orgString;
		}
	}
	
	public  CRC32 createObfuscateFile( ArscData data, StringBlock tableBlock,
            StringBlock keyBlock, File file ) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        CRC32 cksum = new CRC32();
        CheckedOutputStream checkedOutputStream = new CheckedOutputStream(fileOutputStream, cksum);
        LEDataOutputStream out = new LEDataOutputStream(checkedOutputStream);
        
        int tableStrChange = data.getmTableStrings().getSize() - tableBlock.getSize();
        int keyStrChange = data.getmSpecNames().getSize() - keyBlock.getSize();
        data.getmHeader().chunkSize -=(tableStrChange + keyStrChange);
        data.getmHeader().write(out);
        out.writeInt(1);
        tableBlock.write(out);
        data.getmPkgHeader().header.chunkSize -=keyStrChange;
        data.getmPkgHeader().write(out);
        data.getTypeNames().write(out);
        keyBlock.write(out);
        
        byte[] buff = new byte[1024];
        FileInputStream in = new FileInputStream(data.getFile());
        in.skip(data.getmResIndex());
        int len ;
        while(((len = in.read(buff)) != -1)){
            out.write(buff , 0 , len);
        }
        
        in.close();
        out.close();
        checkedOutputStream.close();
        fileOutputStream.close();
        return cksum;
    }
}
