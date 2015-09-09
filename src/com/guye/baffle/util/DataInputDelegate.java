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
package com.guye.baffle.util;

import java.io.DataInput;
import java.io.IOException;

/**
 * @author Ryszard Wiśniewski <brut.alll@gmail.com>
 */
abstract public class DataInputDelegate implements DataInput {
    protected final DataInput mDelegate;

    public DataInputDelegate(DataInput delegate) {
        this.mDelegate = delegate;
    }

    public int skipBytes(int n) throws IOException {
        return mDelegate.skipBytes(n);
    }

    public int readUnsignedShort() throws IOException {
        return mDelegate.readUnsignedShort();
    }

    public int readUnsignedByte() throws IOException {
        return mDelegate.readUnsignedByte();
    }

    public String readUTF() throws IOException {
        return mDelegate.readUTF();
    }

    public short readShort() throws IOException {
        return mDelegate.readShort();
    }

    public long readLong() throws IOException {
        return mDelegate.readLong();
    }

    public String readLine() throws IOException {
        return mDelegate.readLine();
    }

    public int readInt() throws IOException {
        return mDelegate.readInt();
    }

    public void readFully(byte[] b, int off, int len) throws IOException {
        mDelegate.readFully(b, off, len);
    }

    public void readFully(byte[] b) throws IOException {
        mDelegate.readFully(b);
    }

    public float readFloat() throws IOException {
        return mDelegate.readFloat();
    }

    public double readDouble() throws IOException {
        return mDelegate.readDouble();
    }

    public char readChar() throws IOException {
        return mDelegate.readChar();
    }

    public byte readByte() throws IOException {
        return mDelegate.readByte();
    }

    public boolean readBoolean() throws IOException {
        return mDelegate.readBoolean();
    }
}
