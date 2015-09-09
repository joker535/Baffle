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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.guye.baffle.util.ExtDataInput;
import com.guye.baffle.util.LEDataOutputStream;

public class StringBlock {


    public static StringBlock read(ExtDataInput reader) throws IOException {
	    
	    int chunkType ;
	    int alignDataCount = 0;
	    while((chunkType = reader.readInt()) == 0){
	        alignDataCount += 1;
	    }
	    
//		reader.skipCheckInt(CHUNK_TYPE);
		int chunkSize = reader.readInt();
		int stringCount = reader.readInt();
		int styleOffsetCount = reader.readInt();
		int flags = reader.readInt();
		int stringsOffset = reader.readInt();
		int stylesOffset = reader.readInt();

		StringBlock block = new StringBlock();
		block.mAlignDataCount = alignDataCount;
		block.m_isUTF8 = (flags & UTF8_FLAG) != 0;
		block.m_stringOffsets = reader.readIntArray(stringCount);
		block.m_stringOwns = new int[stringCount];
		for (int i = 0; i < stringCount; i++) {
			block.m_stringOwns[i] = -1;
		}
		if (styleOffsetCount != 0) {
			block.m_styleOffsets = reader.readIntArray(styleOffsetCount);
		}
		{
			int size = ((stylesOffset == 0) ? chunkSize : stylesOffset)
					- stringsOffset;
			if ((size % 4) != 0) {
				throw new IOException("String data size is not multiple of 4 ("
						+ size + ").");
			}
			block.m_strings = new byte[size];
			reader.readFully(block.m_strings);
		}
		if (stylesOffset != 0) {
			int size = (chunkSize - stylesOffset);
			if ((size % 4) != 0) {
				throw new IOException("Style data size is not multiple of 4 ("
						+ size + ").");
			}
			block.m_styles = reader.readIntArray(size / 4);
		}

		return block;
	}

	/**
	 * Returns number of strings in block.
	 */
	public int getCount() {
		return m_stringOffsets != null ? m_stringOffsets.length : 0;
	}

	/**
	 * Returns raw string (without any styling information) at specified index.
	 */
	public String getString(int index) {
		if (index < 0 || m_stringOffsets == null
				|| index >= m_stringOffsets.length) {
			return null;
		}
		int offset = m_stringOffsets[index];
		int length;

		if (!m_isUTF8) {
			length = getShort(m_strings, offset) * 2;
			offset += 2;
		} else {
			offset += getVarint(m_strings, offset)[1];
			int[] varint = getVarint(m_strings, offset);
			offset += varint[1];
			length = varint[0];
		}
		return decodeString(offset, length);
	}

	/**
	 * Not yet implemented.
	 * 
	 * Returns string with style information (if any).
	 */
	public CharSequence get(int index) {
		return getString(index);
	}

	/**
	 * Finds index of the string. Returns -1 if the string was not found.
	 */
	public int find(String string) {
		if (string == null) {
			return -1;
		}
		for (int i = 0; i != m_stringOffsets.length; ++i) {
			int offset = m_stringOffsets[i];
			int length = getShort(m_strings, offset);
			if (length != string.length()) {
				continue;
			}
			int j = 0;
			for (; j != length; ++j) {
				offset += 2;
				if (string.charAt(j) != getShort(m_strings, offset)) {
					break;
				}
			}
			if (j == length) {
				return i;
			}
		}
		return -1;
	}

	// /////////////////////////////////////////// implementation
	private StringBlock() {
	}

	/**
	 * Returns style information - array of int triplets, where in each triplet:
	 * * first int is index of tag name ('b','i', etc.) * second int is tag
	 * start index in string * third int is tag end index in string
	 */
	private int[] getStyle(int index) {
		if (m_styleOffsets == null || m_styles == null
				|| index >= m_styleOffsets.length) {
			return null;
		}
		int offset = m_styleOffsets[index] / 4;
		int style[];
		{
			int count = 0;
			for (int i = offset; i < m_styles.length; ++i) {
				if (m_styles[i] == -1) {
					break;
				}
				count += 1;
			}
			if (count == 0 || (count % 3) != 0) {
				return null;
			}
			style = new int[count];
		}
		for (int i = offset, j = 0; i < m_styles.length;) {
			if (m_styles[i] == -1) {
				break;
			}
			style[j++] = m_styles[i++];
		}
		return style;
	}

	private String decodeString(int offset, int length) {
		try {
			return (m_isUTF8 ? UTF8_DECODER : UTF16LE_DECODER).decode(
					ByteBuffer.wrap(m_strings, offset, length)).toString();
		} catch (CharacterCodingException ex) {
			LOGGER.log(Level.WARNING, null, ex);
			return null;
		}
	}

	private static final int getShort(byte[] array, int offset) {
		return (array[offset + 1] & 0xff) << 8 | array[offset] & 0xff;
	}

	private static final int getShort(int[] array, int offset) {
		int value = array[offset / 4];
		if ((offset % 4) / 2 == 0) {
			return (value & 0xFFFF);
		} else {
			return (value >>> 16);
		}
	}

	private static final int[] getVarint(byte[] array, int offset) {
		int val = array[offset];
		boolean more = (val & 0x80) != 0;
		val &= 0x7f;

		if (!more) {
			return new int[] { val, 1 };
		} else {
			return new int[] { val << 8 | array[offset + 1] & 0xff, 2 };
		}
	}

	public boolean touch(int index, int own) {
		if (index < 0 || m_stringOwns == null || index >= m_stringOwns.length) {
			return false;
		}
		if (m_stringOwns[index] == -1) {
			m_stringOwns[index] = own;
			return true;
		} else if (m_stringOwns[index] == own) {
			return true;
		} else {
			return false;
		}
	}

	public StringBlock(int[] strOffset , byte[] str , int[] styleOffset , int[] style , boolean isUTF8){
	    m_stringOffsets = strOffset;
	    m_strings = str ; 
	    m_styleOffsets = styleOffset==null?new int[0]:styleOffset;
	    m_styles = style==null?new int[0]:style;
	    m_isUTF8 = isUTF8;
	}
	
	public void write(LEDataOutputStream out) throws IOException{
	    if(m_styleOffsets == null){
	        m_styleOffsets = new int[0];
	    }
	    if(m_styles == null){
	        m_styles = new int[0];
	    }
	    int modeSize = m_strings.length % 4;
	    if(modeSize != 0){
            modeSize = 4 - modeSize;
	    }

	    int align = mAlignDataCount;
	    while(align-- != 0){
	        out.writeInt(0);
	    }
	    out.writeInt(CHUNK_TYPE);
	    out.writeInt(getSize());
	    out.writeInt(m_stringOffsets.length);
	    out.writeInt(m_styleOffsets.length);
	    out.writeInt(m_isUTF8?UTF8_FLAG:0);
	    out.writeInt(28 + m_stringOffsets.length * 4 + m_styleOffsets.length *4);
	    out.writeInt(m_styleOffsets.length == 0 ? 0 : 28 + m_stringOffsets.length * 4 + m_styleOffsets.length *4 + m_strings.length + modeSize);
	    out.writeIntArray(m_stringOffsets);
	    if(m_styleOffsets != null && m_styleOffsets.length != 0){
	        out.writeIntArray(m_styleOffsets);
	    }
	    out.write(m_strings);
	    
	  
	    for (int i = 0; i < modeSize; i++) {
            out.write(0);
        }
	    
	    if(m_styles != null && m_styles.length != 0){
	        out.writeIntArray(m_styles);
	    }
	    out.flush();
	}
	
	public int getSize() {
	    int size = 28;
	    size +=( m_stringOffsets.length * 4);
	    if(m_styleOffsets != null && m_styleOffsets.length != 0){
	        size += (m_styleOffsets.length * 4);
	    }
	    size += m_strings.length;
	    int modeSize = m_strings.length % 4;
        if(modeSize != 0){
            modeSize = 4 - modeSize;
            size += modeSize;
        }
	    if(m_styles != null && m_styles.length != 0){
	        size += (m_styles.length * 4);
	    }
	    return size;
	}
	
	public int[] getStyleOffset() {
        return m_styleOffsets;
    }

    public int[] getStyle() {
        return m_styles;
    }
    
    public int getAlignCount() {
        return mAlignDataCount;
    }
    
    private int mAlignDataCount;
    private int[] m_stringOffsets;
	private byte[] m_strings;
	private int[] m_styleOffsets;
	private int[] m_styles;
	private boolean m_isUTF8;
	private int[] m_stringOwns;
	private static final CharsetDecoder UTF16LE_DECODER = Charset.forName(
			"UTF-16LE").newDecoder();
	private static final CharsetDecoder UTF8_DECODER = Charset.forName("UTF-8")
			.newDecoder();
	private static final Logger LOGGER = Logger.getLogger(StringBlock.class
			.getName());
	private static final int CHUNK_TYPE = 0x001C0001;
	private static final int UTF8_FLAG = 0x00000100;
   
    
}
