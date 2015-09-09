package com.guye.baffle.ant;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import com.guye.baffle.exception.BaffleException;
import com.guye.baffle.obfuscate.Obfuscater;
import com.guye.baffle.util.AntLogHandle;

public class BaffleTask extends Task {

	private boolean mVerbose;
	private boolean mConfig;
	private boolean mHelp;
	private boolean mOutput;
	private String mConfigFilpaths;
	private String mOutputFilepath;
	private String mApkFilepath;
	private String mTargetFilepath;
	public static final String helper = "[--config(true/false) filepaths list] [--output(true/false) filepath] ApkFile TargetApkFile";

	public void setVerbose(boolean verbose) {
		this.mVerbose = verbose;
	}

	public void setConfig(boolean config) {
		this.mConfig = config;
	}

	public void setHelp(boolean help) {
		this.mHelp = help;
	}

	public void setOutput(boolean output) {
		this.mOutput = output;
	}

	public void setConfigFilepaths(String configfilepaths) {
		this.mConfigFilpaths = configfilepaths;
	}

	public void setOutputFilepath(String outputfilepath) {
		this.mOutputFilepath = outputfilepath;
	}

	public void setApkFilepath(String apkfilepath) {
		this.mApkFilepath = apkfilepath;
	}

	public void setTargetFilepath(String targetFilepath) {
		this.mTargetFilepath = targetFilepath;
	}

	@Override
	public void execute() throws BuildException {

		if (mVerbose) {
			Logger.getLogger(Obfuscater.LOG_NAME).setLevel(Level.CONFIG);
		} else {
			Logger.getLogger(Obfuscater.LOG_NAME).setLevel(Level.OFF);
		}

		Logger.getLogger(Obfuscater.LOG_NAME)
				.addHandler(new AntLogHandle(this));

		if (mApkFilepath == null || mTargetFilepath == null
				|| mApkFilepath.equals("") || mTargetFilepath.equals("")) {
			Logger.getLogger(Obfuscater.LOG_NAME).log(Level.CONFIG,
					"not specify apk file or taget apk file");
			return;
		}

		if (mApkFilepath.equals(mTargetFilepath)) {
			Logger.getLogger(Obfuscater.LOG_NAME)
					.log(Level.CONFIG,
							"apk file can not rewrite , please specify new target file");
			return;
		}

		File mApkFile = new File(mApkFilepath);
		if (!mApkFile.exists()) {
			Logger.getLogger(Obfuscater.LOG_NAME).log(Level.CONFIG,
					"apk file not exists");
			return;
		}

		if (mHelp) {
			Logger.getLogger(Obfuscater.LOG_NAME).log(Level.CONFIG, helper);
			return;
		}

		File[] mConfigFiles = null;
		if (mConfig) {
			String[] fs = mConfigFilpaths.split(",");
			int len = fs.length;
			mConfigFiles = new File[fs.length];
			for (int i = 0; i < len; i++) {
				mConfigFiles[i] = new File(fs[i]);
				if (!mConfigFiles[i].exists()) {
					Logger.getLogger(Obfuscater.LOG_NAME).log(Level.CONFIG,
							"config file " + fs[i] + " not exists");
					return;
				}
			}
		} else {
			mConfigFiles = null;
		}

		File mappingfile = null;
		if (mOutput) {
			mappingfile = new File(mOutputFilepath);
			if (mappingfile.getParentFile() != null) {
				mappingfile.getParentFile().mkdirs();
			}
		} else {
			mappingfile = null;
		}

		Obfuscater obfuscater = new Obfuscater(mConfigFiles, mappingfile,
				mApkFile, mTargetFilepath);

		try {
			obfuscater.obfuscate();
		} catch (IOException e) {
			getProject().fireBuildFinished(e);
		} catch (BaffleException e) {
			getProject().fireBuildFinished(e);
		}

	}
}
