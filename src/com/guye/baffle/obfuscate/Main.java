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

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import com.guye.baffle.exception.BaffleException;
import com.guye.baffle.util.ConsoleHandler;

public class Main {
	public static void main(String[] args) throws IOException, BaffleException {
		Options opt = new Options();

		opt.addOption("c", "config", true, "config file path,keep or mapping");

		opt.addOption("o", "output", true, "output mapping writer file");

		opt.addOption("v", "verbose", false, "explain what is being done.");

		opt.addOption("h", "help", false, "print help for the command.");

		opt.getOption("c").setArgName("file list");

		opt.getOption("o").setArgName("file path");

		String formatstr = "baffle [-c/--config filepaths list ][-o/--output filepath][-h/--help] ApkFile TargetApkFile";

		HelpFormatter formatter = new HelpFormatter();
		CommandLineParser parser = new PosixParser();
		CommandLine cl = null;
		try {
			// 处理Options和参数
			cl = parser.parse(opt, args);

		} catch (ParseException e) {
			formatter.printHelp(formatstr, opt); // 如果发生异常，则打印出帮助信息
			return;
		}

		if (cl == null || cl.getArgs() == null || cl.getArgs().length == 0) {
			formatter.printHelp(formatstr, opt);
			return;
		}

		// 如果包含有-h或--help，则打印出帮助信息
		if (cl.hasOption("h")) {
			HelpFormatter hf = new HelpFormatter();
			hf.printHelp(formatstr, "", opt, "");
			return;
		}

		// 获取参数值，这里主要是DirectoryName
		String[] str = cl.getArgs();
		if (str == null || str.length != 2) {
			HelpFormatter hf = new HelpFormatter();
			hf.printHelp("not specify apk file or taget apk file", opt);
			return;
		}

		if (str[1].equals(str[0])) {
			HelpFormatter hf = new HelpFormatter();
			hf.printHelp(
					"apk file can not rewrite , please specify new target file",
					opt);
			return;
		}
		File apkFile = new File(str[0]);
		if (!apkFile.exists()) {
			HelpFormatter hf = new HelpFormatter();
			hf.printHelp("apk file not exists", opt);
			return;
		}

		File[] configs = null;
		if (cl.hasOption("c")) {
			String cfg = cl.getOptionValue("c");
			String[] fs = cfg.split(",");
			int len = fs.length;
			configs = new File[fs.length];
			for (int i = 0; i < len; i++) {
				configs[i] = new File(fs[i]);
				if (!configs[i].exists()) {
					HelpFormatter hf = new HelpFormatter();
					hf.printHelp("config file " + fs[i] + " not exists", opt);
					return;
				}
			}
		}

		File mappingfile = null;
        if (cl.hasOption("o")) {
            String mfile = cl.getOptionValue("o");
            mappingfile = new File(mfile);

            if (mappingfile.getParentFile() != null) {
                mappingfile.getParentFile().mkdirs();
            }

        }
		
		if(cl.hasOption('v')){
		    Logger.getLogger(Obfuscater.LOG_NAME).setLevel(Level.CONFIG);
		}else{
		    Logger.getLogger(Obfuscater.LOG_NAME).setLevel(Level.OFF);
		}
		
		Logger.getLogger(Obfuscater.LOG_NAME).addHandler(new ConsoleHandler());
		
		Obfuscater obfuscater = new Obfuscater(configs, mappingfile, apkFile,
				str[1]);

		obfuscater.obfuscate();
	}
}
