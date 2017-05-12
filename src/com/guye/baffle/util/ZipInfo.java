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

import java.util.logging.Level;
import java.util.logging.Logger;

import com.guye.baffle.obfuscate.ObfuscateHelper;
import com.guye.baffle.obfuscate.Obfuscater;

public class ZipInfo {
	public String type;
	public String name;
	private String orginName;
	public long size;
	public int zipMethod;
	public long crc;
	private String postfix;
	private String digest;

	public ZipInfo(String name, int method, long l, long crc,String digest) {
		zipMethod = method;
		this.size = l;
		this.crc = crc;
		this.setDigest(digest);
		setOrginName(name);
		if (name.startsWith(ObfuscateHelper.RES_PROFIX)) {
			String[] names = name.split("/");
			if (names == null || names.length != 3) {
				Logger.getLogger(Obfuscater.LOG_NAME).log(Level.CONFIG,
						"ZipInfo:names->" + name);
				throw new RuntimeException(); // TODO
			}

			int index = names[2].indexOf('.');
			String postfix = "";
			if (index > 0) {
				postfix = names[2].substring(index, names[2].length());
				names[2] = names[2].substring(0, index);
			}

			this.type = names[1];
			this.name = names[2];
			this.postfix = postfix;
		} else {
			this.name = name;
		}
	}

	public String getKey(String resKey) {
		if (type != null) {
			StringBuilder builder = new StringBuilder(resKey).append("/")
					.append(type).append("/").append(name).append(postfix);
			return builder.toString();
		} else {
			return name;
		}
	}

    public String getDigest() {
        return digest;
    }

    public void setDigest( String digest ) {
        this.digest = digest;
    }

    public String getOrginName() {
        return orginName;
    }

    public void setOrginName( String orginName ) {
        this.orginName = orginName;
    }

  
}