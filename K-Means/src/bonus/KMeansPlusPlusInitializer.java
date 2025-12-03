package bonus;

import model.Cluster;
import model.Point;
import util.DistanceUtils;
import util.RandomUtils;

import java.util.ArrayList;
import java.util.List;


public class KMeansPlusPlusInitializer {


    public static List<Point> initializeCentroids(List<Point> points, int k) {
        if (points == null || points.isEmpty()) {
            throw new IllegalArgumentException("Points list cannot be null or empty");
        }
        if (k <= 0) {
            throw new IllegalArgumentException("k must be positive");
        }
        if (k > points.size()) {
            throw new IllegalArgumentException("k cannot be greater than the number of points");
        }

        List<Point> centroids = new ArrayList<>();
        
        // Step 1: Choose first centroid uniformly at random
        int firstIndex = RandomUtils.nextInt(points.size());
        centroids.add(new Point(points.get(firstIndex).getCoordinates()));
        
        // Step 2: Choose remaining k-1 centroids using weighted probability
        for (int i = 1; i < k; i++) {
            Point nextCentroid = selectNextCentroid(points, centroids);
            centroids.add(nextCentroid);
        }
        
        return centroids;
    }
    
    private static Point selectNextCentroid(List<Point> points, List<Point> existingCentroids) {
        // Calculate minimum squared distance from each point to nearest existing centroid
        double[] minDistancesSquared = new double[points.size()];
        double totalDistanceSquared = 0.0;
        
        for (int i = 0; i < points.size(); i++) {
            Point p = points.get(i);
            double minDistSquared = Double.MAX_VALUE;
            
            // Find minimum distance to any existing centroid
            for (Point centroid : existingCentroids) {
                double dist = DistanceUtils.distance(p, centroid);
                double distSquared = dist * dist;
                if (distSquared < minDistSquared) {
                    minDistSquared = distSquared;
                }
            }
            
            minDistancesSquared[i] = minDistSquared;
            totalDistanceSquared += minDistSquared;
        }
        
        // Select a point with probability proportional to squared distance
        double randomValue = RandomUtils.nextDouble() * totalDistanceSquared;
        double cumulative = 0.0;
        
        for (int i = 0; i < points.size(); i++) {
            cumulative += minDistancesSquared[i];
            if (cumulative >= randomValue) {
                // Return a copy of the point to avoid reference issues
                return new Point(points.get(i).getCoordinates());
            }
        }
        
        // Fallback (shouldn't reach here, but just in case)
        int lastIndex = points.size() - 1;
        return new Point(points.get(lastIndex).getCoordinates());
    }
    
    public static List<Cluster> initializeClusters(List<Point> points, int k) {
        List<Point> centroids = initializeCentroids(points, k);
        List<Cluster> clusters = new ArrayList<>();
        
        for (Point centroid : centroids) {
            clusters.add(new Cluster(centroid));
        }
        
        return clusters;
    }
}
