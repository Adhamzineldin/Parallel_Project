package model;

import java.util.ArrayList;
import java.util.List;

public class Cluster {

    private Point centroid;
    private final List<Point> points;

    public Cluster(Point centroid) {
        this.centroid = centroid;
        this.points = new ArrayList<>();
    }

    public void addPoint(Point p) {
        points.add(p);
    }

    public void clearPoints() {
        points.clear();
    }

 
    public Point getCentroid() {
        return centroid;
    }

    
    public void setCentroid(Point centroid) {
        this.centroid = centroid;
    }
    
    public List<Point> getPoints() {
        return points;
    }

    public void recomputeCentroid() {
        if (points.isEmpty()) {
            return; 
        }

        int dim = centroid.getDimension();
        double[] newCoords = new double[dim];

        for (Point p : points) {
            for (int i = 0; i < dim; i++) {
                newCoords[i] += p.getCoordinate(i);
            }
        }
        
        for (int i = 0; i < dim; i++) {
            newCoords[i] /= points.size();
        }

        centroid = new Point(newCoords);
    }

    @Override
    public String toString() {
        return "Cluster{" +
                "centroid=" + centroid +
                ", points=" + points.size() + " points" +
                '}';
    }
}
