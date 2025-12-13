package core;

import model.Cluster;
import model.Point;
import util.DistanceUtils;

import java.util.List;
import java.util.concurrent.RecursiveTask;

/**
     * RecursiveTask to recompute centroids in parallel
     */
public class RecomputeCentroidsTask extends RecursiveTask<Boolean> {
        private static final int THRESHOLD = 2; // process clusters in chunks of 10 for better granularity
        private final List<Cluster> clusters;
        private final int start;
        private final int end;
        private final KMeansConfig config;

        public RecomputeCentroidsTask(List<Cluster> clusters, int start, int end, KMeansConfig config) {
            this.clusters = clusters;
            this.start = start;
            this.end = end;
            this.config = config;
        }

        @Override
        protected Boolean compute() {
            if (end - start <= THRESHOLD) {
                boolean converged = true;
                for (int i = start; i < end; i++) {
                    Cluster c = clusters.get(i);
                    Point oldCentroid = c.getCentroid();
                    c.recomputeCentroid();
                    double movement = DistanceUtils.distance(oldCentroid, c.getCentroid());
                    if (movement > config.getTolerance()) {
                        converged = false;
                    }
                }
                return converged;
            } else {
                int mid = (start + end) / 2;
                RecomputeCentroidsTask left = new RecomputeCentroidsTask(clusters, start, mid, this.config);
                RecomputeCentroidsTask right = new RecomputeCentroidsTask(clusters, mid, end, this.config);
                left.fork();
                boolean rightResult = right.compute();
                boolean leftResult = left.join();
                return leftResult && rightResult;
            }
        }
    }