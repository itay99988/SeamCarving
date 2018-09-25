package seam_carving;

public class Pixel {

	//props
	private int red;
	private int green;
	private int blue;
	
	//const
	public Pixel(int red, int green, int blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
	}
	
	//funcs
	public void setR(int red) {
		this.red = red;
	}
	public void setG(int green) {
		this.green = green;
	}
	public void setB(int blue) {
		this.blue = blue;
	}
	
	public int R() {
		return this.red;
	}
	public int G() {
		return this.green;
	}
	public int B() {
		return this.blue;
	}
	
	public int RGBint() {
		int value = ((255 & 0xFF) << 24) | //alpha
	            ((red) << 16) | //red
	            ((green) << 8)  | //green
	            ((blue) << 0); //blue
		return value;
	}
	
	public double singleDerivative(Pixel other) {
		double sum = Math.abs(this.R() - other.R()) + Math.abs(this.G() - other.G()) + Math.abs(this.B() - other.B());
		return sum/3;
	}
	
	public double grayscaleValue(){
		return (this.red+this.green+this.blue)/3;
	}
	
	public Pixel returnCopy() {
		return new Pixel(this.red,this.green,this.blue);
	}
}
