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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;

import com.guye.baffle.config.BaffleConfig;
import com.guye.baffle.config.ConfigReader;
import com.guye.baffle.config.MappingWriter;
import com.guye.baffle.decoder.ArscData;
import com.guye.baffle.decoder.StringBlock;
import com.guye.baffle.exception.BaffleException;
import com.guye.baffle.util.ApkFileUtils;
import com.guye.baffle.util.LEDataOutputStream;
import com.guye.baffle.util.OS;
import com.guye.baffle.util.ZipInfo;

public class Obfuscater {

    public static final String LOG_NAME = "BAFFLE";
	private static final Charset UTF_8_CHARSET = Charset.forName("UTF-8");

	private List<ZipInfo> mZipinfos;
	private ObfuscateHelper mObfuscateHelper;
	private ArscData mArscData;

	private BaffleConfig mBaffleConfig;

	private MappingWriter mMappingWrite;

	private File[] mConfigFiles;

	private File mApkFile;

	private String mTarget;

	private File mMappingFile;

    private File mRepeatFile;
	
	private Logger log = Logger.getLogger(LOG_NAME);
	
	private Map<String, String> mWebpMapping = new HashMap<>(1000);

	
	public Obfuscater(File[] configs, File mappingFile, File repeatFile, File apkFile,
			String target) {
		mConfigFiles = configs;
		mApkFile = apkFile;
		mTarget = target;
		mRepeatFile = repeatFile;
		mMappingFile = mappingFile;
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
					newStr = mObfuscateHelper.getNewTableString(orgTableStrings
							.getString(i));
				} else {
					newStr = mObfuscateHelper.getNewKeyString(orgTableStrings
							.getString(i));
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

	public void obfuscate() throws IOException, BaffleException {

		String tempDir = System.getProperty("java.io.tmpdir") + File.separator
				+ "old" + File.separator;
		log.log(Level.CONFIG,"tempDir:::" + tempDir);
		File temp = new File(tempDir);
		OS.rmdir(temp);
		temp.mkdirs();

		// read keep and mapping config
		mBaffleConfig = new ConfigReader().read(mConfigFiles);

		mObfuscateHelper = new ObfuscateHelper(mBaffleConfig);
		
		// unzip apk or ap_ file
		List<ZipInfo> zipinfos = ApkFileUtils.unZipApk(mApkFile, tempDir , mWebpMapping);
		
		if(mRepeatFile != null){
		    PrintStream printStream = new PrintStream(mRepeatFile);
		    List<ZipInfo> sortedZipinfo = new ArrayList<ZipInfo>(zipinfos.size());
	        sortedZipinfo.addAll(zipinfos);

	        Collections.sort(sortedZipinfo, new Comparator<ZipInfo>() {

	            @Override
	            public int compare( ZipInfo o1, ZipInfo o2 ) {
	                return o1.getDigest().compareTo(o2.getDigest());
	            }
	        });
	        
	        int size = zipinfos.size();
	        int index = 0 ;
	        ZipInfo info = null;
	        info = sortedZipinfo.get(index);
	        Map<String, List<ZipInfo>> map = new HashMap<String, List<ZipInfo>>();
	        while(index < size-1){
	            if(info.getDigest().equals(sortedZipinfo.get(index+1).getDigest())){
	                List<ZipInfo> infos = map.get(info.getDigest());
	                if(infos == null){
	                    infos = new ArrayList<ZipInfo>();
	                    map.put(info.getDigest(), infos);
	                    infos.add(info);
	                }
	                infos.add(sortedZipinfo.get(index+1));
	                index+=1;
	                if(index >= size){
	                    break;
	                }
	                info = sortedZipinfo.get(index);
	            }else{
	                index+=1;
	                if(index >= size){
	                    break;
	                }
	                info = sortedZipinfo.get(index);
	            }
	        }
	        Set<Entry<String, List<ZipInfo>>> entries = map.entrySet();
	        for (Entry<String, List<ZipInfo>> entry : entries) {
	            printStream.println("md5:" +entry.getKey());
	            for (ZipInfo z : entry.getValue()) {
	                printStream.println("\t" +z.getOrginName());
	            }
	            printStream.println("----------");
	        }
	        
	        printStream.close();
		}
		
		// decode arsc file
		mArscData = ArscData.decode(new File(tempDir + "resources.arsc"));

		// do obfuscate
		mObfuscateHelper.obfuscate(mArscData);
		
		mObfuscateHelper.setWebpMapping(mWebpMapping);
		
		// write mapping file
		if (mMappingFile != null) {
			mMappingWrite = new MappingWriter(
					mObfuscateHelper.getObfuscateData().keyMaping);
			mMappingWrite.WriteToFile(mMappingFile);
		} else {
		    log.log(Level.CONFIG , "not specific mapping file");
		}

		StringBlock tableBlock = createStrings(mArscData.getmTableStrings(),
				true);
		StringBlock keyBlock = createStrings(mArscData.getmSpecNames(), false);
		File arscFile = new File(tempDir + "resources.n.arsc");
		CRC32 arscCrc = mArscData.createObfuscateFile(tableBlock, keyBlock,
				arscFile);

		mZipinfos = zipinfos;

		ZipInfo arscInfo = new ZipInfo("resources.arsc", ZipEntry.STORED,
				arscFile.length(), arscCrc.getValue() , "");

		try {
			new ApkBuilder(mObfuscateHelper, mZipinfos, arscInfo).reBuildapk(
					mTarget, tempDir);
		} catch (IOException e) {
			OS.rmfile(mTarget);
			throw e;
		}

		OS.rmdir(temp);
	}

}
