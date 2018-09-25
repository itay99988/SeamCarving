package seam_carving;

import java.util.Arrays;


/**
 * A class that implements various operations used on images that are in the form of Pixel[][] objects.
 * eg: transposing an image, computing the energy and cost map,
 * increasing and decreasing the width and height of an image using the seam carving algorithm.
 */
public class ImageOps {
	
	private static final double ENTROPY_WEIGHT = 0.5; // The ratio of the local entropy that affects the energy of the pixels.
	private static final boolean WITH_INTERPOLATION = true; // Determines whether to use interpolation blending when enlarging an image, or not.

	/**
	 * 
	 * @param image
	 * @return The image's width.
	 */
	public static int getWidth(Pixel[][] image) {
		return image[0].length;
	}


	/**
	 * 
	 * @param image
	 * @return The image's height.
	 */
	public static int getHeight(Pixel[][] image) {
		return image.length;
	}


	/**
	 * Transposes an image matrix.
	 * ie. 	if image is a m x n dimentional Pixel matrix, then this method will return a n x m dimentional Pixel matrix called transposed,
	 * 		where transposed[i][j] == image[j][i].
	 * @param image A 2-dimentional Pixel matrix.
	 * @return transposed.
	 */
	private static Pixel[][] transposeImage(Pixel[][] image) {
		int originalWidth = getWidth(image);
		int originalHeight = getHeight(image);

		Pixel[][] transposed = new Pixel[originalWidth][originalHeight];

		// The actual transposition.
		for(int x = 0; x < originalWidth; x++) {
			for(int y = 0; y < originalHeight; y++) {
				transposed[x][y] = image[y][x].returnCopy();
			}
		}
		
/*		for(int y = 0; y < getHeight(transposed); y++) {
			for(int x = 0; x < getWidth(transposed); x++) {
				if(transposed[y][x] == null ) {
					System.out.println("NULL AT: ("+x+","+y+").");
				}
			}
		}
		System.out.println("NO NULL.");
*/
		return transposed;
	}
	
	
	/**
	 * Computes H_xy - the local entropy of Pixel[y][x] over a 9x9 window.
	 * @param x The x coordinate of the pixel.
	 * @param y The y coordinate of the pixel.
	 * @param image
	 * @return 
	 */
	private static double computeLocalEntropy(int x, int y, Pixel[][] image) {
		int width = getWidth(image);
		int height = getHeight(image);
		int n = 0; // Number of neighbors.
		double h = 0;
		double p;
		double p_sum = 0;

		//calculate the sum of grayscale values used for the calculation of p_mn.
		for(int i = -4; i < 5; i++) {
			for(int j = -4; j < 5; j++) {
				if(x+i < 0 || x+i >= width || y+j < 0 || y+j >= height) { // If we're on a boundary.
					continue;
				}
				p_sum += image[y+j][x+i].grayscaleValue();
				n++;
			}
		}
		p_sum *= (81/n); // Normalize to a 9x9 window.
		
		// after that, we can compute H.
		for(int i = -4; i < 5; i++) {
			for(int j = -4; j < 5; j++) {
				if(x+i < 0 || x+i >= width || y+j < 0 || y+j >= height) { // If we're on a boundary.
					continue;
				}
				p = image[y+j][x+i].grayscaleValue()/p_sum; 
				if(p == 0){
					continue;
				}
				h -= p*Math.log(p)/Math.log(2); // Multiplied by log2(p)
			}
		}

		return h*(81/n); // Returned value is as described in the document, normalized to a 9x9 window.
	}
	
	
	/**
	 * Computes the pixel's average energy, calculating its derivative using its neighbors.
	 * @param x The x coordinate of the pixel.
	 * @param y The y coordinate of the pixel.
	 * @param image
	 * @param withLocalEntropy
	 * @return The average energy of Pixel[y][x].
	 */
	private static double computeEnergy(int x, int y, Pixel[][] image, boolean withLocalEntropy) {
		Pixel pixel = image[y][x]; // The pixel we want to compute its energy.
		int width = getWidth(image);
		int height = getHeight(image);
		double totalEnergy = 0;
		int n = 0; // Number of neighbors.
		double ret; // Returned value.
		
		for(int i = -1; i < 2; i++) {
			for(int j = -1; j < 2; j++) {
				if(x+i < 0 || x+i >= width || y+j < 0 || y+j >= height || (i == 0 && j == 0)) { // If we're on a boundary, or on the original pixel (i=0, j=0).
					continue;
				}
				totalEnergy += pixel.singleDerivative(image[y+j][x+i]);
				n++;
			}
		}
		if(withLocalEntropy) {
			ret = (1-ENTROPY_WEIGHT)*totalEnergy/n + ENTROPY_WEIGHT*computeLocalEntropy(x, y, image);
		}
		else {
			ret = totalEnergy/n; // Divide by the number of neighbors compared to - to get the average energy.
		}

		return ret;
	}


	/**
	 * 
	 * @param image A 2-dimentional Pixel matrix.
	 * @param withLocalEntropy
	 * @return
	 */
	private static double[][] computeEnergyMap(Pixel[][] image, boolean withLocalEntropy) {
		int width = getWidth(image);
		int height = getHeight(image);

		double[][] map = new double[height][width];
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				map[y][x] = computeEnergy(x, y, image, withLocalEntropy);
			}
		}
		return map;
	}

	/**
	 * Computes the cost of a single pixel, using dynamic programming.   
	 * @param x the x coordinate of the pixel.
	 * @param y the y coordinate of the pixel.
	 * @param image
	 * @param costMap
	 * @param energyMap
	 * @param withForwardEnergy - calculate the cell cost using the forward energy principle
	 */
	private static void computeCost(int x, int y, Pixel[][] image, double[][] costMap, double[][] energyMap, boolean withForwardEnergy) {
		int width = energyMap[0].length;
		if(y == 0) { //First row.
			costMap[0][x] = energyMap[0][x];
			return;
		}

		double option1, option2, option3;

		// Compute option1.
		if(x == 0) {
			option1 = Integer.MAX_VALUE;
		}
		else {
			option1 = costMap[y-1][x-1];
		}

		// Compute option2.
		option2 = costMap[y-1][x];

		// Compute option3.
		if(x == width-1) {
			option3 = Integer.MAX_VALUE;
		}
		else {
			option3 = costMap[y-1][x+1];
		}
		
		if(withForwardEnergy) {
			double cL=0, cU=0, cR=0;
			if(x == 0) {
				cL = 0;
				cU = 0;
				cR = image[y-1][x].singleDerivative(image[y][x+1]);
			}
			else if(x == width-1) {
				cL = image[y-1][x].singleDerivative(image[y][x-1]);
				cU = 0;
				cR = 0;
			}
			else {
				cL = image[y][x+1].singleDerivative(image[y][x-1]) + image[y-1][x].singleDerivative(image[y][x-1]);
				cU = image[y][x+1].singleDerivative(image[y][x-1]);
				cR = image[y][x+1].singleDerivative(image[y][x-1]) + image[y-1][x].singleDerivative(image[y][x+1]);
			}
			
			option1+=cL;
			option2+=cU;
			option3+=cR;
		}

		double cost = energyMap[y][x] + Math.min(Math.min(option1, option2), option3);
		costMap[y][x] = cost;
	}


	/**
	 * Computes the cost map of the image.
	 * @param energyMap
	 * @param withLocalEntropy
	 * @param withForwardEnergy - calculate the cost map using the forward energy principle
	 * @return cost map.
	 */
	private static double[][] computeCostMap(Pixel[][] image, boolean withLocalEntropy, boolean withForwardEnergy) {
		double[][] energyMap = computeEnergyMap(image, withLocalEntropy);
		int height = energyMap.length;
		int width = energyMap[0].length;
		double[][] costMap = new double[height][width];
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width; x++) {
				computeCost(x, y, image, costMap, energyMap, withForwardEnergy);
			}
		}

		return costMap;
	}


	private static double[] getSumsOfColumns(double[][] energyMap) {
		double[] sums = new double[energyMap[0].length];
		for(int i=0;i<sums.length;i++) {
			for(int j=0;j<energyMap.length;j++) {
				sums[i] += energyMap[j][i];
			}
		}
		return sums;
	}
	
	private static Pixel[][] straightSeamsRemoval(Pixel[][] image, int[] seamsIndices){
		
		int width = getWidth(image);
		int height = getHeight(image);
		Pixel[][] newImage = new Pixel[height][width-seamsIndices.length];
		int columnIndex=0;
		for(int newColumnIndex=0;newColumnIndex<getWidth(newImage);newColumnIndex++) {
			while(Services.arrayContains(seamsIndices,columnIndex)) {
				columnIndex++;
			}
			copyColumn(image, newImage, columnIndex, newColumnIndex);
			columnIndex++;
		}
		return newImage;
	}
	
	private static void copyColumn(Pixel[][] image, Pixel[][] newImage, int imageColumnIndex, int newImageColumnIndex) {
		for(int i=0;i<getHeight(image);i++) {
			newImage[i][newImageColumnIndex] = image[i][imageColumnIndex].returnCopy();
		}
	}
	
	/**
	 * Decreases the width of an image using straight seams.
	 * @param image
	 * @param newWidth
	 * @return
	 */
	public static Pixel[][] decreaseWidthStraightSeams(Pixel[][] image, int newWidth) {
		double[][] energyMap = computeEnergyMap(image,false);
		int width = getWidth(image);
		int deltaColumns = width - newWidth;
		//indices of the k min values
		int[] indicesForRemoval = Services.bottomN(getSumsOfColumns(energyMap),deltaColumns);
		Pixel[][] newImage = straightSeamsRemoval(image,indicesForRemoval);
		return newImage;
	}
	
	
	/**
	 * Decreases the height of an image using straight seams.
	 * @param image
	 * @param newHeight
	 * @return
	 */
	public static Pixel[][] decreaseHeightStraightSeams(Pixel[][] image, int newHeight) {
		Pixel[][] transposed = transposeImage(image);
		transposed = decreaseWidthStraightSeams(transposed, newHeight);
		return transposeImage(transposed);
	}
	
	
	/**
	 * Used for decreasing the size of an image - marks all pixels that belongs to a seam as null.
	 * @param image
	 * @param seam
	 */
	private static void markGeneralSeam(Pixel[][] image, Integer[] seam){
		int height = getHeight(image);
		for(int y = 0; y < height; y++) {
			image[y][seam[y]] = null;
		}
	}
	
	
	/**
	 * Finds and returns k different seams using a given cost map.
	 * @param costMap
	 * @param k
	 * @return
	 */
	private static Integer[][] findKSeams(double[][] costMap, int k) {
		
		int width = costMap[0].length;
		int height = costMap.length;
		Integer[][] seamsFound = new Integer[k][height];
		Integer[] curSeam;
		int minIndex = 0;
		
		// First find the k lowest cost indices at height: height-1, and sort from lowest to highest
		int[] arr = Services.bottomN(costMap[height-1], k);
		Integer[] arrInteger = new Integer[k];
		for(int i = 0; i < k; i++) {
			arrInteger[i] = (Integer)arr[i];
		}
		Arrays.sort(arrInteger);
		for(int i = 0; i < k; i++) {
			seamsFound[i][height-1] = (Integer)arrInteger[i];
		}
		
		
        for(int iter = 0; iter < k; iter++) {
			double[] valuesArr = new double[3];
        	minIndex = 0;
        	curSeam = new Integer[height];
        	curSeam[height-1] = seamsFound[iter][height-1];
        	for(int y = height-2; y >= 0; y--) {
        			if(curSeam[y+1] == 0) {
        				valuesArr[0] = Double.MAX_VALUE;
        				valuesArr[2] = costMap[y][curSeam[y + 1]+1];

        			}
        			else if(curSeam[y+1] == width - 1) {
        				valuesArr[0] = costMap[y][curSeam[y+1]-1];
        				valuesArr[2] = Double.MAX_VALUE;

        			}
        			else {
        				valuesArr[0] = costMap[y][curSeam[y+1]-1];
        				valuesArr[2] = costMap[y][curSeam[y+1]+1];
        			}
        			valuesArr[1] = costMap[y][curSeam[y+1]];
        			if(costMap[y][curSeam[y+1]] == Double.MAX_VALUE && curSeam[y+1]+1 < width) {
        				minIndex = curSeam[y+1]+1;
        			}
        			else {
                		minIndex = curSeam[y+1] + Services.bottomN(valuesArr,1)[0]-1;
        			}
        			curSeam[y] = minIndex;
            		//shift from minIndex - calculated with index of the minimum value of the values array
            		costMap[y][minIndex] = Double.MAX_VALUE;
        	}
    		costMap[height-1][curSeam[height-1]] = Double.MAX_VALUE;
        	seamsFound[iter] = curSeam;
        }
        return seamsFound;
        
		
	}
	
	/**
	 * Returns a new image without the marked seams (assuming already marked in the image).
	 * @param image
	 * @return
	 */
	private static Pixel[][] removeMarkedPixels(Pixel[][] image){
		int width = getWidth(image);
		int height = getHeight(image);
		Pixel[][] newImage = new Pixel[height][width-1];
		
		for(int y=0;y<height;y++) {
			for(int newImageIndex=0,imageIndex=0;newImageIndex<width-1;newImageIndex++,imageIndex++) {
				if(image[y][imageIndex] == null)
					imageIndex++;
				newImage[y][newImageIndex] = image[y][imageIndex].returnCopy();
			}
		}
		return newImage;
	}
	
	
	/**
	 * Adds or removes general seams from the image, using a generated cost map of the image.
	 * @param image
	 * @param newWidth
	 * @param withLocalEntropy
	 * @param withForwardEnergy
	 * @return
	 */
	private static Pixel[][] addOrRemoveVerticalSeam(Pixel[][] image, int newWidth, boolean withLocalEntropy, boolean withForwardEnergy){
		int width = getWidth(image);
		Pixel[][] newImage;
		Integer[] seam;
		Integer[][] kSeams = null; //Used for increasing image size.
		int deltaColumns = width - newWidth;
		boolean decreaseImage = deltaColumns > 0;
		if(deltaColumns == 0) {
			return image;
		}
		//first iteration
		double[][] costMap = computeCostMap(image,withLocalEntropy,withForwardEnergy);
		seam = findKSeams(costMap,1)[0];
		if(decreaseImage) {
			markGeneralSeam(image,seam);
			newImage = removeMarkedPixels(image);
		}
		else {
			deltaColumns *= -1;
			kSeams = findKSeams(costMap,deltaColumns);
			newImage = addVerticalSeam(image,kSeams[deltaColumns-1]);
		}
		deltaColumns--;
		
		while(deltaColumns > 0) {
			costMap = computeCostMap(newImage,withLocalEntropy, withForwardEnergy);
			seam = findKSeams(costMap,1)[0];
			if(decreaseImage) {
				markGeneralSeam(newImage,seam);
				newImage = removeMarkedPixels(newImage);
			}
			else { // increase image size
				newImage = addVerticalSeam(newImage,kSeams[deltaColumns-1]);
			}
			deltaColumns--;
		}
		
		return newImage;
	}
	
	
	/**
	 * Inserts one vertical seam to an image.
	 * @param image
	 * @param seam
	 * @return
	 */
	private static Pixel[][] addVerticalSeam(Pixel[][] image, Integer[] seam) {
		int width = getWidth(image);
		int height = getHeight(image);
		Pixel[][] newImage = new Pixel[height][width+1];
		
		for(int y = 0; y < height; y++) {
			for(int x = 0; x < width+1; x++) {
				if(WITH_INTERPOLATION) { // Blending added seam with adjacent pixels
					if(x <= seam[y]) { // Pixels that are before the seam
						newImage[y][x] = image[y][x].returnCopy();
					}
					else if (x == seam[y]+1 && x > 0 && x <= width-1) {  // pixels next to the seam
						int red, green, blue; //Finding the average color of adjacent pixels.
						red = (image[y][x-1].R() + image[y][x].R())/2;
						green = (image[y][x-1].G() + image[y][x].G())/2;
						blue = (image[y][x-1].B() + image[y][x].B())/2;
						Pixel pixel = new Pixel(red, green, blue);
						newImage[y][x] = pixel;
					}
					else { // Pixels after seam
						newImage[y][x] = image[y][x-1].returnCopy();
					}
				}
				else { // No iterpolation
					if(x <= seam[y]){ // Pixels that are before the seam
						newImage[y][x] = image[y][x].returnCopy();
					}
					else { // Pixels after seam
						newImage[y][x] = image[y][x-1].returnCopy();
					}
				}
			}
		}
		return newImage;
	}


	/**
	 * Increases or decreases the width of an image to newWidth.
	 * @param image
	 * @param newWidth
	 * @param withLocalEntropy
	 * @return The updated image with a width of newWidth.
	 * @param withForwardEnergy - calculate the cost map using the forward energy principle
	 */
	public static Pixel[][] changeWidthGeneralSeams(Pixel[][] image, int newWidth, boolean withLocalEntropy, boolean withForwardEnergy) {
		return addOrRemoveVerticalSeam(image, newWidth, withLocalEntropy, withForwardEnergy);
	}
	
	
	/**
	 * Increases or decreases the height of an image to newHeight.
	 * @param image
	 * @param newHeight
	 * @param addLocalEntropy
	 * @param withForwardEnergy - calculate the cost map using the forward energy principle
	 * @return The updated image with a height of newHeight.
	 */
	public static Pixel[][] changeHeightGeneralSeams(Pixel[][] image, int newHeight, boolean addLocalEntropy, boolean withForwardEnergy) {
		Pixel[][] transposed = transposeImage(image);
		transposed = addOrRemoveVerticalSeam(transposed, newHeight, addLocalEntropy, withForwardEnergy);
		return transposeImage(transposed);
	}
	
}
