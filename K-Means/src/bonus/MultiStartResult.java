package bonus;

import model.Cluster;

import java.util.List;

/**
     * Result class to hold the best clustering result
     */
    public class MultiStartResult {
        private final List<Cluster> clusters;
        private final double sse;
        private final int bestRestart;
        private final long totalTime;

        public MultiStartResult(List<Cluster> clusters, double sse, int bestRestart, long totalTime) {
            this.clusters = clusters;
            this.sse = sse;
            this.bestRestart = bestRestart;
            this.totalTime = totalTime;
        }

        public List<Cluster> getClusters() {
            return clusters;
        }

        public double getSSE() {
            return sse;
        }

        public int getBestRestart() {
            return bestRestart;
        }

        public long getTotalTime() {
            return totalTime;
        }
    }