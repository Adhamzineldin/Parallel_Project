package core;

import model.Cluster;
import model.Point;
import util.DistanceUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

public class KMeansParallel {

    private final KMeansConfig config;
    private final List<Point> points;
    private final List<Cluster> clusters;
    private final ForkJoinPool pool;

    public KMeansParallel(KMeansConfig config, List<Point> points) {
        this.config = config;
        this.points = points;
        this.clusters = new ArrayList<>();
        this.pool = ForkJoinPool.commonPool(); // uses all available cores
    }

    /**
     * Run the K-Means algorithm in parallel
     */
    public void run() {
        initializeClusters();

        boolean converged = false;
        int iteration = 0;

        while (!converged && iteration < config.getMaxIterations()) {
            // Clear points
            clusters.forEach(Cluster::clearPoints);

            // Parallel assignment of points
            pool.invoke(new KMeansAssignTask(points, 0, points.size(), clusters));

            // Parallel centroid recomputation
            converged = pool.invoke(new RecomputeCentroidsTask(clusters, 0, clusters.size(), this.config));

            iteration++;
        }

        System.out.println("Parallel K-Means finished in " + iteration + " iterations");
    }

    /**
     * Initialize clusters with random points from dataset as centroids
     */
    private void initializeClusters() {
        clusters.clear();
        List<Point> shuffled = new ArrayList<>(points);
        Collections.shuffle(shuffled, new Random());

        for (int i = 0; i < config.getK(); i++) {
            clusters.add(new Cluster(shuffled.get(i)));
        }
    }

    /**
     * Compute SSE (sum of squared errors) for current clustering
     */
    public double computeSSE() {
        double sse = 0.0;
        for (Cluster c : clusters) {
            Point centroid = c.getCentroid();
            for (Point p : c.getPoints()) {
                double dist = DistanceUtils.distance(p, centroid);
                sse += dist * dist;
            }
        }
        return sse;
    }

    public List<Cluster> getClusters() {
        return clusters;
    }

    

   
}
