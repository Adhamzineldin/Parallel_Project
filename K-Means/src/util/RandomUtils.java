package util;

import java.util.Random;

public final class RandomUtils {

    private static Random random = new Random(42); // default seed

    private RandomUtils() {}

    public static void setSeed(long seed) {
        random = new Random(seed);
    }

    public static Random getRandom() {
        return random;
    }

    public static int nextInt(int bound) {
        return random.nextInt(bound);
    }

    public static double nextDouble() {
        return random.nextDouble();
    }
}
