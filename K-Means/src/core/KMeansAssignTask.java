package core;

import model.Cluster;
import model.Point;
import util.DistanceUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RecursiveTask;

/**
 * RecursiveTask to assign points to nearest cluster in parallel using local-reduce-merge pattern
 * This eliminates race conditions by accumulating locally and merging at the end
 */
public class KMeansAssignTask extends RecursiveTask<Map<Integer, List<Point>>> {
    private static final int THRESHOLD = 1000; // chunk size
    private final List<Point> points;
    private final int start;
    private final int end;
    private final List<Cluster> clusters;

    public KMeansAssignTask(List<Point> points, int start, int end, List<Cluster> clusters) {
        this.points = points;
        this.start = start;
        this.end = end;
        this.clusters = clusters;
    }

    @Override
    protected Map<Integer, List<Point>> compute() {
        if (end - start <= THRESHOLD) {
            // LOCAL: Accumulate assignments locally in a map (cluster index -> list of points)
            Map<Integer, List<Point>> localAssignments = new HashMap<>();
            
            for (int i = start; i < end; i++) {
                Point p = points.get(i);
                int nearestClusterIndex = -1;
                double minDist = Double.MAX_VALUE;

                // Find nearest cluster
                for (int j = 0; j < clusters.size(); j++) {
                    Cluster c = clusters.get(j);
                    double dist = DistanceUtils.distance(p, c.getCentroid());
                    if (dist < minDist) {
                        minDist = dist;
                        nearestClusterIndex = j;
                    }
                }

                // Add to local accumulation (no synchronization needed)
                localAssignments.computeIfAbsent(nearestClusterIndex, k -> new ArrayList<>()).add(p);
            }
            
            return localAssignments;
        } else {
            // REDUCE: Split and recursively compute
            int mid = (start + end) / 2;
            KMeansAssignTask left = new KMeansAssignTask(points, start, mid, clusters);
            KMeansAssignTask right = new KMeansAssignTask(points, mid, end, clusters);
            
            left.fork();
            Map<Integer, List<Point>> rightResult = right.compute();
            Map<Integer, List<Point>> leftResult = left.join();
            
            // MERGE: Combine results from left and right subtasks
            return mergeAssignments(leftResult, rightResult);
        }
    }
    
    /**
     * Merge two assignment maps by combining lists for the same cluster index
     */
    private Map<Integer, List<Point>> mergeAssignments(Map<Integer, List<Point>> left, Map<Integer, List<Point>> right) {
        Map<Integer, List<Point>> merged = new HashMap<>(left);
        
        for (Map.Entry<Integer, List<Point>> entry : right.entrySet()) {
            int clusterIndex = entry.getKey();
            List<Point> points = entry.getValue();
            merged.computeIfAbsent(clusterIndex, k -> new ArrayList<>()).addAll(points);
        }
        
        return merged;
    }
    
    /**
     * Merge the final result into the actual clusters
     * This should be called after the task completes
     */
    public static void mergeIntoClusters(Map<Integer, List<Point>> assignments, List<Cluster> clusters) {
        for (Map.Entry<Integer, List<Point>> entry : assignments.entrySet()) {
            int clusterIndex = entry.getKey();
            List<Point> points = entry.getValue();
            
            if (clusterIndex >= 0 && clusterIndex < clusters.size()) {
                Cluster cluster = clusters.get(clusterIndex);
                for (Point p : points) {
                    cluster.addPoint(p);
                }
            }
        }
    }
}