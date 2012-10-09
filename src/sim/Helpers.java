package sim;


public class Helpers {

	public static double clamp(double i, double low, double high) {
		return Math.max(Math.min(i, high), low);
	}
}
