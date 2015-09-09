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
package com.guye.baffle.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaffleConfig {

	private Map<String, String> mKeyMapping = new HashMap<String, String>();

	private List<Object> mKeyKeeps = new ArrayList<Object>();

	BaffleConfig() {
	}

	/*
	 * 根据配置文件获取资源文件混淆后的名字，key不包括后缀。没有特殊配置返回null
	 */
	public String getMapping(String key) {
		String newkey = mKeyMapping.get(key);
		if (newkey != null) {
			if (newkey.length() == 0) {
				throw new IllegalArgumentException("error key config on : "
						+ key);
			}
			return newkey;
		} else {
			return null;
		}
	}

	public boolean isKeepKey(String key) {
	    for (Object object : mKeyKeeps) {
            if(object instanceof Pattern){
                Pattern pattern = (Pattern) object;
                if(pattern.matcher(key).matches()){
                    return true;
                }
            }else{
                if(key.equals(object)){
                    return true;
                }
            }
        }
        return false;
	}

	public void addKeep(Object keep) {
		mKeyKeeps.add(keep);
	}

	public void addMapping(String key, String map) {
		mKeyMapping.put(key, map);
	}

	public Map<String, String> getAllMapping() {
		return Collections.unmodifiableMap(mKeyMapping);
	}
}
