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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.guye.baffle.config.BaffleConfig;
import com.guye.baffle.decoder.ArscData;
import com.guye.baffle.decoder.StringBlock;
import com.guye.baffle.exception.BaffleException;
import com.guye.baffle.exception.BaffleRuntimeException;
import com.guye.baffle.util.ZipInfo;

public class ObfuscateHelper {
	private Map<String, Boolean> usedKey = new HashMap<String, Boolean>();

	public static final String RES_PROFIX = "res/";

	public static final String RES_KEY = "res";
	private static final String RES_KEY_KEY = "key_";

	private static final String DEFAULT_RES_KEY = "res";

	private Logger log = Logger.getLogger(Obfuscater.LOG_NAME);
	
	private Map<String, String> mTypeMaping = new HashMap<String, String>(16);
	private Map<String, String> mKeyMaping = new HashMap<String, String>(100);
	private Map<String, String> mWebpMapping;
	private BaffleConfig baffleConfig;
	private NameFactory factory;

	ObfuscateHelper(BaffleConfig config) throws BaffleException {
		baffleConfig = config;
		Map<String, String> map = baffleConfig.getAllMapping();
		log.log(Level.CONFIG, "ObfuscateHelper:---map>>>" + map.size());
		Set<Entry<String, String>> set = map.entrySet();
		for (Entry<String, String> entry : set) {
			if (usedKey.containsKey(entry.getValue())) {
				throw new BaffleException("duplicate map config : "
						+ entry.getKey() + "--->" + entry.getValue());
			}
			usedKey.put(entry.getValue(), Boolean.TRUE);
		}
		factory = new NameFactory();
	}

	public String createTypeName(String type) {
		return factory.createName(RES_KEY);
	}

	public String getResKey() {
		return DEFAULT_RES_KEY;
	}

	public String createKeyName(String key) {
		String newKey = baffleConfig.getMapping(key);
		if (newKey == null) {
			if (baffleConfig.isKeepKey(key)) {
				if (usedKey.containsKey(key)) {
					throw new BaffleRuntimeException("error keep key config :"
							+ key);
				} else {
					usedKey.put(key, Boolean.TRUE);
					return key;
				}
			} else {
				newKey = factory.createName(RES_KEY_KEY);
				while (usedKey.containsKey(newKey)) {
					newKey = factory.createName(RES_KEY_KEY);
				}
				usedKey.put(newKey, Boolean.TRUE);
				return newKey;
			}
		} else {
			return newKey;
		}
	}

	public void obfuscate(ArscData orgData) {
		int strPoolCount = orgData.getmTableStrings().getCount();
		String orgString;
		String[] names;
		String[] newNames;
		for (int i = 0; i < strPoolCount; i++) {
			orgString = orgData.getmTableStrings().getString(i);

			if (orgString.startsWith(RES_PROFIX)) {
			    log.log(Level.CONFIG,"orgResString:---->>" + orgString);
				names = orgString.split("/");
				if (names == null || names.length != 3) {
					throw new RuntimeException(); // TODO
				}
				newNames = new String[3];
				newNames[0] = getResKey();
				String typeName = mTypeMaping.get(names[1]);
				if (typeName == null) {
					newNames[1] = createTypeName(names[1]);
					mTypeMaping.put(names[1], newNames[1]);
				} else {
					newNames[1] = typeName;
				}
				int index = names[2].indexOf('.');
				if (index > 0) {
					names[2] = names[2].substring(0, index);
				}
				newNames[2] = mKeyMaping.get(names[2]);
				if (newNames[2] == null) {
					newNames[2] = createKeyName(names[2]);
				}
				mKeyMaping.put(names[2], newNames[2]);
			}
		}
		StringBlock keyStrings = orgData.getmSpecNames();
		int keyCount = keyStrings.getCount();
		String keyName;
		String newKey;
		for (int i = 0; i < keyCount; i++) {
			keyName = keyStrings.getString(i);
			if (!mKeyMaping.containsKey(keyName)) {
				newKey = createKeyName(keyName);
				mKeyMaping.put(keyName, newKey);
				log.log(Level.CONFIG,keyStrings.getString(i) + " ---> " + newKey);
			} else {
			    log.log(Level.CONFIG,keyStrings.getString(i) + " ---> "
						+ mKeyMaping.get(keyName));
			}
		}
	}

	String getNewKeyString(String orgString) {
		return mKeyMaping.get(orgString);
	}

	String getNewTableString(String orgString) {
		if (orgString.startsWith(RES_PROFIX)) {
			String webpOrgString = mWebpMapping.get(orgString);
			if(webpOrgString != null){
				orgString = webpOrgString;
			}
			String[] names = orgString.split("/");
			if (names == null || names.length != 3) {
				throw new RuntimeException(); // TODO
			}
			String[] newNames = new String[3];
			newNames[0] = getResKey();
			newNames[1] = mTypeMaping.get(names[1]);
			if (newNames[1] == null) {
				throw new RuntimeException(); // TODO
			}
			int index = names[2].indexOf('.');
			String postfix = "";
			if (index > 0) {
				postfix = names[2].substring(index, names[2].length());
				names[2] = names[2].substring(0, index);
			}
			newNames[2] = mKeyMaping.get(names[2]);
			if (newNames[2] == null) {
				throw new RuntimeException(); // TODO
			}
			String newString = new StringBuilder().append(newNames[0])
					.append('/').append(newNames[1]).append('/')
					.append(newNames[2]).append(postfix).toString();
			System.out.println(newString);
			return newString;
		} else {
			return orgString;
		}
	}

	public String getNewKey(ZipInfo zipInfo) {
		if (zipInfo.type == null) {
			return zipInfo.getKey(RES_KEY);
		} else {
			zipInfo.type = mTypeMaping.get(zipInfo.type);
			zipInfo.name = mKeyMaping.get(zipInfo.name);
			return zipInfo.getKey(getResKey());
		}
	}

	public ObfuscateData getObfuscateData() {
		ObfuscateData data = new ObfuscateData();
		data.resName = getResKey();
		data.typeMaping = Collections.unmodifiableMap(mTypeMaping);
		data.keyMaping = Collections.unmodifiableMap(mKeyMaping);
		return data;
	}

	public static class ObfuscateData {
		public String resName;
		public Map<String, String> typeMaping;
		public Map<String, String> keyMaping;
	}

	public void setWebpMapping(Map<String, String> map) {
		this.mWebpMapping = map;
		
	}
}
