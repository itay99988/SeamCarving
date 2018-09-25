package seam_carving;

public class SeamCarvingMain {

	public static void main(String[] args) {
		//Interpreting input (assuming legal)
		String inputPath = args[0];
		String outputPath = args[4];
		int numOfColumns = Integer.parseInt(args[1]);
		int numOfRows = Integer.parseInt(args[2]);
		boolean withLocalEntropy = args[3].equals("1");
		boolean withForwardEnergy = args[3].equals("2");
		//Seam carving
		String filename = inputPath.substring(inputPath.lastIndexOf("/")+1, inputPath.length());
		if(filename.length() == inputPath.length()) {
			filename = inputPath.substring(inputPath.lastIndexOf("\\")+1, inputPath.length());
		}
		System.out.println("Loading image: "+filename+".");
		Pixel[][] image = ImageLib.loadImage(inputPath);
		System.out.println("Image loaded successfully.");
		System.out.print("Energy type: ");
		if(withLocalEntropy) {
			System.out.println("Regular energy with local entropy.");
		}
		else if(withForwardEnergy) {
			System.out.println("Forward energy.");
		}
		else {
			System.out.println("Regular energy.");
		}
		System.out.println("Changing dimensions from:"+ImageOps.getWidth(image)+"x"+ImageOps.getHeight(image)+" to: "+numOfColumns+"x"+numOfRows+"...");
		Pixel[][] newImage = ImageOps.changeWidthGeneralSeams(image, numOfColumns, withLocalEntropy, withForwardEnergy);
		//Pixel[][] newImage = ImageOps.decreaseWidthStraightSeams(image, numOfColumns);
		System.out.println("Width changed to "+numOfColumns+".");
		newImage = ImageOps.changeHeightGeneralSeams(newImage, numOfRows, withLocalEntropy, withForwardEnergy);
		//newImage = ImageOps.decreaseHeightStraightSeams(newImage, numOfRows);
		System.out.println("Height changed to "+numOfRows+".");
		ImageLib.saveImage(newImage,outputPath);
		
		System.out.println("Done.");
		System.out.println("Seam carved image saved at: "+outputPath+".");
	}
}