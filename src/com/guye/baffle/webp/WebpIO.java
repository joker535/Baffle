package com.guye.baffle.webp;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

import com.luciad.imageio.webp.WebPWriteParam;

public class WebpIO {
	public static boolean toWebp(File in , File out) {
		long orgSize = in.length();
		BufferedImage image;
		try{
		    image = ImageIO.read(in);
		}catch(Exception e){
		    e.printStackTrace();
		    return false;
		}
		
		if(image == null){
		    System.out.println(in.getAbsolutePath());
		    return false;
		}
		ImageWriter w = null;
		ImageTypeSpecifier type =
	            ImageTypeSpecifier.createFromRenderedImage(image);
	        Iterator<ImageWriter> iter = ImageIO.getImageWriters(type, "webp");

	        if (iter.hasNext()) {
	            w = iter.next();
	        } else {
	            w= null;
	        }
		if(w == null){
			return false;
		}
		// Obtain a WebP ImageWriter instance
		ImageWriter writer = ImageIO.getImageWritersByMIMEType("image/webp").next();

		// Configure encoding parameters
		WebPWriteParam writeParam = new WebPWriteParam(writer.getLocale());
		writeParam.setCompressionMode(WebPWriteParam.LOSSY_COMPRESSION);
		
		

		// Configure the output on the ImageWriter
		try {
            writer.setOutput(new FileImageOutputStream(out));
            // Encode
            writer.write(null, new IIOImage(image, null, null), writeParam);
        } catch (FileNotFoundException e) {
            
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            
            e.printStackTrace();
            return false;
        }


		
	
		long newSize = out.length();
		if(orgSize > newSize){
			in.delete();
			return true;
		}else{
			out.delete();
			return false;
		}
		
		
	}
	
	private static String getWebpName(String name) {
		name = name.substring(0,name.indexOf('.'));
		return name+".webp";
	}
}
