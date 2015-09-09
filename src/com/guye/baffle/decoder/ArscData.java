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

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;

import org.apache.commons.io.input.CountingInputStream;

import com.guye.baffle.obfuscate.Obfuscater;
import com.guye.baffle.util.ConsoleHandler;
import com.guye.baffle.util.ExtDataInput;
import com.guye.baffle.util.LEDataInputStream;
import com.guye.baffle.util.LEDataOutputStream;

public class ArscData {
    private Header        mHeader;
    private StringBlock   mTableStrings;
    private StringBlock   mSpecNames;
    private StringBlock   mTypeNames;
    private int           mTypeStrStart;
    private int           mTypeStrEnd;
    private int           mPkgHeaderIndex;
    private int           mResIndex;
    private PackageHeader mPkgHeader;
    private File          mFile;

    public static ArscData decode( File file ) throws IOException {
        FileInputStream arscStream = new FileInputStream(file);
        CountingInputStream countIn;
        countIn = new CountingInputStream(arscStream);
        ExtDataInput in = new ExtDataInput(new LEDataInputStream(countIn));
        ArscData arscData =  readTable(file , countIn, in);
        countIn.close();
        arscStream.close();
        return arscData;
    }

    private static ArscData readTable(File file , CountingInputStream countIn, ExtDataInput in )
            throws IOException {
        ArscData arscData = new ArscData();
        arscData.mFile = file;
        arscData.mHeader = Header.read(in);
        int packageCount = in.readInt();
        if (packageCount != 1) {
            throw new UnsupportedOperationException("not support more then 1 package");
        }
        arscData.mTableStrings = StringBlock.read(in);
        arscData.mPkgHeaderIndex = (int) countIn.getByteCount();
        arscData.mPkgHeader = PackageHeader.read(in);
        arscData.mTypeStrStart = (int) countIn.getByteCount();
        arscData.mTypeNames = StringBlock.read(in);
        arscData.mTypeStrEnd =  (int) countIn.getByteCount();
        arscData.mSpecNames = StringBlock.read(in);
        arscData.mResIndex =  (int) countIn.getByteCount();
        return arscData;
    }

    public static class Header {
        public final short type;
        public final short headerSize;
        public int   chunkSize;

        public Header(short type, short headSize, int size) {
            this.type = type;
            this.headerSize = headSize;
            this.chunkSize = size;
        }

        public static Header read( ExtDataInput in ) throws IOException {
            short type;
            try {
                type = in.readShort();
            } catch (EOFException ex) {
                return new Header(TYPE_NONE, (short) 0, 0);
            }
            return new Header(type, in.readShort(), in.readInt());
        }

        public final static short TYPE_NONE = -1, TYPE_TABLE = 0x0002, TYPE_PACKAGE = 0x0200,
                TYPE_TYPE = 0x0202, TYPE_CONFIG = 0x0201;

        public void write( LEDataOutputStream out ) throws IOException {
            out.writeShort(type);
            out.writeShort(headerSize);
            out.writeInt(chunkSize);
        }
    }

    public static class PackageHeader {
        public Header header;
        public int    id;
        public byte[] name = new byte[256];
        public int    typeNameStrings, typeNameCount, specNameStrings, specNameCount;

        public static PackageHeader read( ExtDataInput in ) throws IOException {
            PackageHeader header = new PackageHeader();
            header.header = Header.read(in);
            header.id = (byte) in.readInt();
            in.readFully(header.name);
            header.typeNameStrings = in.readInt();
            header.typeNameCount = in.readInt();
            header.specNameStrings = in.readInt();
            header.specNameCount = in.readInt();
            return header;
        }

        public void write( LEDataOutputStream out ) throws IOException {
            header.write(out);
            out.writeInt(id);
            out.write(name);
            out.writeInt(typeNameStrings);
            out.writeInt(typeNameCount);
            out.writeInt(specNameStrings);
            out.writeInt(specNameCount);
        }
    }

    
    public Header getmHeader() {
        return mHeader;
    }

    public StringBlock getmTableStrings() {
        return mTableStrings;
    }

    public StringBlock getmSpecNames() {
        return mSpecNames;
    }

    public int getmTypeStrStart() {
        return mTypeStrStart;
    }

    public int getmTypeStrEnd() {
        return mTypeStrEnd;
    }

    public int getmPkgHeaderIndex() {
        return mPkgHeaderIndex;
    }

    public int getmResIndex() {
        return mResIndex;
    }

    public PackageHeader getmPkgHeader() {
        return mPkgHeader;
    }
    
    public StringBlock getTypeNames(){
        return mTypeNames;
    }
    
    public File getFile(){
        return mFile;
    }

    public CRC32 createObfuscateFile(StringBlock tableBlock,
            StringBlock keyBlock, File file ) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        CRC32 cksum = new CRC32();
        CheckedOutputStream checkedOutputStream = new CheckedOutputStream(fileOutputStream, cksum);
        LEDataOutputStream out = new LEDataOutputStream(checkedOutputStream);
        
        int tableStrChange = getmTableStrings().getSize() - tableBlock.getSize();
        int keyStrChange = getmSpecNames().getSize() - keyBlock.getSize();
        getmHeader().chunkSize -=(tableStrChange + keyStrChange);
        getmHeader().write(out);
        out.writeInt(1);
        tableBlock.write(out);
        getmPkgHeader().header.chunkSize -=keyStrChange;
        getmPkgHeader().write(out);
        getTypeNames().write(out);
        keyBlock.write(out);
        
        byte[] buff = new byte[1024];
        FileInputStream in = new FileInputStream(getFile());
        in.skip(getmResIndex());
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
