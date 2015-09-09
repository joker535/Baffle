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

import java.util.HashMap;
import java.util.Map;

public class NameFactory {
    
    private static char chara = 'a';
    
    private Map<String, String> mUsingName = new HashMap<String, String>(16);
    
    public String createName(String type){
        String name = mUsingName.get(type);
        if(name == null){
            name = String.valueOf(chara);
            mUsingName.put(type, name);
            return name;
        }else{
            char[] chars = name.toCharArray();
            StringBuilder stringBuilder = new StringBuilder(chars.length);
            if(add(chars, chars.length -1)){
                stringBuilder.append(chara);
            }
            stringBuilder.append(chars);
            mUsingName.put(type, stringBuilder.toString());
            return stringBuilder.toString();
        }
    }
    
    private boolean add(char[] c, int index){
        if(c[index] < 'z'){
            c[index]++;
            return false;
        }else{
            c[index] = chara;
            if(index > 0){
                return add(c , index-1);
            }else{
                return true;
            }
        }
    }
    
    public static void main( String[] args ) {
        byte[] s = new byte[]{0x72,0x65,0x73,0x2f ,0x64,0x72 ,0x61,0x77,0x61,0x62,0x6c,0x65};
    }
}
