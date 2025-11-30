package util;

import model.Point;

public class DistanceUtils {

    // Compute Euclidean distance between two points
    public static double distance(Point a, Point b) {
        if (a.getDimension() != b.getDimension()) {
            throw new IllegalArgumentException("Points must have the same dimension.");
        }

        double sum = 0;
        for (int i = 0; i < a.getDimension(); i++) {
            double diff = a.getCoordinate(i) - b.getCoordinate(i);
            sum += diff * diff;
        }

        return Math.sqrt(sum);
    }
}
