package sim;

import java.util.Random;

public class Helpers {

    private static Random random = new Random();
    
    public static double clamp(double i, double low, double high) {
        return Math.max(Math.min(i, high), low);
    }


    public static float clamp(float i, float low, float high) {
        return Math.max(Math.min(i, high), low);
    }


    public static int clamp(int i, int low, int high) {
        return Math.max(Math.min(i, high), low);
    }
    
    public static double rand() {
        return random.nextDouble(); 
    }

}
