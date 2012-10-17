package sim;

import java.util.Random;

/**
 * A class containing several helper methods used across the simulation.
 */
public class Helpers {

    private static Random random = new Random();

    /**
     * Clamps a value <code>i</code> to the [<code>low</code>, <code>high</code>] range.
     * @param i The value to be clampped.
     * @param low The lower bound of the clamp range.
     * @param high The higher bound of the clamp range.
     * @return Returns the clampped value.
     */
    public static double clamp(double i, double low, double high) {
        return Math.max(Math.min(i, high), low);
    }

    /**
     * Ditto.
     */
    public static float clamp(float i, float low, float high) {
        return Math.max(Math.min(i, high), low);
    }

    /**
     * Ditto.
     */
    public static int clamp(int i, int low, int high) {
        return Math.max(Math.min(i, high), low);
    }

    /**
     * Returns a pseudorandom number with uniform distribution.
     * @return A uniformly distributed pseudorandom double value in [0, 1] range.
     */
    public static double rand() {
        return random.nextDouble();
    }

}
