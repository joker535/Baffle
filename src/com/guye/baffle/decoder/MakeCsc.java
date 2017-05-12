package com.guye.baffle.decoder;

/**
 * Created by linuxsir on 15/8/12.
 */

import java.io.File;
import java.io.FileInputStream;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

public class MakeCsc {

    public static long getFileCRCCode(File file) throws Exception {

        FileInputStream fileinputstream = new FileInputStream(file);
        CRC32 crc32 = new CRC32();
        for (CheckedInputStream checkedinputstream =
             new CheckedInputStream(fileinputstream, crc32);
             checkedinputstream.read() != -1;
                ) {
        }
        return crc32.getValue();


    }
}
