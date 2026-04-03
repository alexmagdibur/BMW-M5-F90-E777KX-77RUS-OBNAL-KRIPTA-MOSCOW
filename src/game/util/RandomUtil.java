package game.util;

import java.util.Random;

public class RandomUtil {

    private static final Random RANDOM = new Random();

    public static int nextInt(int min, int max) {
        return min + RANDOM.nextInt(max - min + 1);
    }

    public static double nextDouble(double min, double max) {
        return min + RANDOM.nextDouble() * (max - min);
    }
}
