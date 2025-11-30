package core;

import model.Cluster;
import model.Point;
import util.DistanceUtils;

import java.util.List;
import java.util.concurrent.RecursiveAction;

/**
 * RecursiveAction to assign points to nearest cluster in parallel
 */
class KMeansAssignTask extends RecursiveAction {
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
    protected void compute() {
        if (end - start <= THRESHOLD) {
            for (int i = start; i < end; i++) {
                Point p = points.get(i);
                Cluster nearest = null;
                double minDist = Double.MAX_VALUE;

                for (Cluster c : clusters) {
                    double dist = DistanceUtils.distance(p, c.getCentroid());
                    if (dist < minDist) {
                        minDist = dist;
                        nearest = c;
                    }
                }

                synchronized (nearest) {
                    nearest.addPoint(p);
                }
            }
        } else {
            int mid = (start + end) / 2;
            KMeansAssignTask left = new KMeansAssignTask(points, start, mid, clusters);
            KMeansAssignTask right = new KMeansAssignTask(points, mid, end, clusters);
            invokeAll(left, right);
        }
    }
}