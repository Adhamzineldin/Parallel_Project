package model;

import util.DistanceUtils;

import java.util.Arrays;

public class Point {

    private final double[] coordinates;


    public Point(double... coordinates) {
        if (coordinates.length == 0) {
            throw new IllegalArgumentException("Point must have at least one dimension.");
        }
        this.coordinates = Arrays.copyOf(coordinates, coordinates.length);
    }

    public double getCoordinate(int index) {
        return coordinates[index];
    }


    public void setCoordinate(int index, double value) {
        coordinates[index] = value;
    }

    public int getDimension() {
        return coordinates.length;
    }

    public double[] getCoordinates() {
        return Arrays.copyOf(coordinates, coordinates.length);
    }

    public double distanceTo(Point other) {
        return DistanceUtils.distance(this, other);
    }



    @Override
    public String toString() {
        return Arrays.toString(coordinates);
    }
}

