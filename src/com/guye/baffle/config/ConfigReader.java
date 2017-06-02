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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.guye.baffle.exception.BaffleException;
import com.guye.baffle.obfuscate.Obfuscater;

public class ConfigReader {
	private static final String COMMENT_PROFIX = "#";
	private static final String FRAGMENT_START_FLAG = "----";

	private static final String FRAGMENT_KEEP_KEY = "keep_key";
	private static final String FRAGMENT_KEEP_IMAGE = "keep_image";
	private static final String FRAGMENT_MAP_KEY = "map_key";

	private static final int STATUS_NULL = 0;
	private static final int STATUS_IN_FARGMENT = 1;

	private Logger log = Logger.getLogger(Obfuscater.LOG_NAME);
	private int status = 0;
	private String fragmentType;

	public BaffleConfig read(File[] configFiles) throws BaffleException {
		BaffleConfig baffleConfig = new BaffleConfig();

		FileInputStream fileInputStream;
		BufferedReader reader;
		String line;
		if (configFiles != null) {
			for (File file : configFiles) {
				try {
					fileInputStream = new FileInputStream(file);
					reader = new BufferedReader(new InputStreamReader(
							fileInputStream));
					while ((line = reader.readLine()) != null) {
						if (line.startsWith(COMMENT_PROFIX)
								|| line.length() == 0) {
							continue;
						} else if (line.startsWith(FRAGMENT_START_FLAG)) {
							String fragmentName = line
									.substring(FRAGMENT_START_FLAG.length(),
											line.length());
							if (status == STATUS_NULL) {
								status = STATUS_IN_FARGMENT;
								fragmentType = fragmentName;
								log.log(Level.CONFIG,"fragmentType::"
										+ fragmentType);
							} else {
								if (!fragmentType.equals(fragmentName)) {
									throw new BaffleException(
											"error config or map config file :"
													+ file.getName()
													+ " on line  : " + line);
								} else {
									status = STATUS_NULL;
									fragmentType = null;
								}
							}
						} else {
							if (status == STATUS_NULL) {
								throw new BaffleException(
										"error config or map config file :"
												+ file.getName()
												+ " on line : " + line);
							} else {
								dealLine(line, baffleConfig);
							}
						}

					}
					reader.close();
					fileInputStream.close();
				} catch (IOException e) {
					throw new BaffleException(e);
				}
			}
		}
		return baffleConfig;

	}

	private void dealLine(String line, BaffleConfig baffleConfig)
			throws BaffleException {
		if (FRAGMENT_KEEP_KEY.equals(fragmentType)) {
		    if(qulifiedMapValue(line)){
		        baffleConfig.addKeep(line);
		    }else{
		        try {
                    baffleConfig.addKeep(Pattern.compile(line));
                } catch (PatternSyntaxException e) {
                   throw new BaffleException("error keep java Pattern : " + line, e);
                }
		    }
		} else if (FRAGMENT_KEEP_IMAGE.equals(fragmentType)) {
            if(qulifiedMapValue(line)){
                baffleConfig.addKeepImage(line);
            }else{
                try {
                    baffleConfig.addKeepImage(Pattern.compile(line));
                } catch (PatternSyntaxException e) {
                   throw new BaffleException("error keep java Pattern : " + line, e);
                }
            }
        } else if (FRAGMENT_MAP_KEY.equals(fragmentType)) {
			String[] strs = line.split(",");
			if (strs.length != 2) {
				throw new BaffleException(
						"error config or map config on line : " + line);
			}
			if (qulifiedMapValue(strs[1])) {
				baffleConfig.addMapping(strs[0], strs[1]);
			} else {
				throw new BaffleException(
						"error map config about mapvalue on line : " + line);
			}

		}
	}

	private boolean qulifiedMapValue(String mapValue) {
		int length = 0;
		if (mapValue.charAt(0) >= 'a' && mapValue.charAt(0) <= 'z') {
			length++;
			for (int i = 1; i < mapValue.length(); i++) {
				if (mapValue.charAt(i) >= 'a' && mapValue.charAt(i) <= 'z'
						|| mapValue.charAt(i) >= '0'
						&& mapValue.charAt(i) <= '9'
						|| mapValue.charAt(i) == '_') {
					length++;
				} else {
					break;
				}
			}
		}
		if (length == mapValue.length()) {
			return true;
		} else {
			return false;
		}

	}

}
