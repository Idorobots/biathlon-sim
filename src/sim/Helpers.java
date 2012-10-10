package sim;


public class Helpers {

	public static double clamp(double i, double low, double high) {
		return Math.max(Math.min(i, high), low);
	}

	public static float clamp(float i, float low, float high) {
		return Math.max(Math.min(i, high), low);
	}

	public static int clamp(int i, int low, int high) {
		return Math.max(Math.min(i, high), low);
	}


}
