package seam_carving;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
/**
 * This class is in charge of loading and saving images, also converting images to Pixel[][] objects and vice versa.
 */
public class ImageLib {

	public static Pixel[][] loadImage(String path) {
	   	 BufferedImage bImage = null;
	        try {
	            File initialImage = new File(path);
	            bImage = ImageIO.read(initialImage);
	            Pixel[][] pixels = convertImageToArray(bImage);
	            return pixels;
	        } catch (IOException e) {
	              System.out.println("Exception occured :" + e.getMessage());
	        }
	        return null;
	   }
	    
	    public static void saveImage(Pixel[][] pixels, String path) {
	    	BufferedImage bImage = new BufferedImage(pixels[0].length, pixels.length, BufferedImage.TYPE_INT_RGB);
	    	try {
	    	bImage = convertArrayToImage(pixels,bImage);
	    	File destImage = new File(path);
	    	ImageIO.write(bImage, "jpg", destImage);
	    	}
	    	catch (IOException e) {
	            System.out.println("Exception occured :" + e.getMessage());
	      }
	    }
	    
	    private static Pixel[][] convertImageToArray(BufferedImage image) {

	        final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
	        final int width = image.getWidth();
	        final int height = image.getHeight();
	        final boolean hasAlphaChannel = image.getAlphaRaster() != null;

	        Pixel[][] result = new Pixel[height][width];
	        if (hasAlphaChannel) {
	           final int pixelLength = 4;
	           for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
	              //int alpha = (((int) pixels[pixel] & 0xff)); // alpha
	              int blue = ((int) pixels[pixel + 1] & 0xff); // blue
	              int green = (((int) pixels[pixel + 2] & 0xff)); // green
	              int red = (((int) pixels[pixel + 3] & 0xff)); // red
	              result[row][col] = new Pixel(red,green,blue);
	              col++;
	              if (col == width) {
	                 col = 0;
	                 row++;
	              }
	           }
	        } else {
	           final int pixelLength = 3;
	           for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
	        	  int blue = ((int) pixels[pixel] & 0xff); // blue
	        	  int green = (((int) pixels[pixel + 1] & 0xff)); // green
	        	  int red = (((int) pixels[pixel + 2] & 0xff)); // red
	              result[row][col] = new Pixel(red,green,blue); 
	              col++;
	              if (col == width) {
	                 col = 0;
	                 row++;
	              }
	           }
	        }

	        return result;
	     }
	    
	    private static BufferedImage convertArrayToImage(Pixel[][] pixels, BufferedImage outputImage) {
	    	int[] outputImagePixelData = ((DataBufferInt)outputImage.getRaster().getDataBuffer()).getData() ;

	        final int width = outputImage.getWidth();
	        final int height = outputImage.getHeight();

	        for (int y=0, pos=0 ; y < height ; y++)
	            for (int x=0 ; x < width ; x++, pos++)
	                outputImagePixelData[pos] = pixels[y][x].RGBint();

	        return outputImage;
	    }
}

