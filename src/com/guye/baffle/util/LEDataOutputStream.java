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

import java.io.IOException;
import java.io.OutputStream;

public class LEDataOutputStream {

	OutputStream stream;

	public LEDataOutputStream(OutputStream out) {
		stream = out;
	}

	public void writeIntArray(int[] ints) throws IOException {
		for (int i = 0; i < ints.length; i++) {
			writeInt(ints[i]);
		}
	}

	public void write(int b) throws IOException {
		stream.write(b);
	}

	public void write(byte[] b) throws IOException {
		stream.write(b);
	}

	public void write(byte[] b, int off, int len) throws IOException {
		stream.write(b, off, len);
	}

	public void writeShort(int v) throws IOException {
		stream.write((v >>> 0) & 0xFF);
		stream.write((v >>> 8) & 0xFF);
	}

	public void writeInt(int v) throws IOException {
		stream.write((v >>> 0) & 0xFF);
		stream.write((v >>> 8) & 0xFF);
		stream.write((v >>> 16) & 0xFF);
		stream.write((v >>> 24) & 0xFF);
	}

	public void close() throws IOException {
		stream.close();
	}

	public void flush() throws IOException {
		stream.flush();
	}

}
