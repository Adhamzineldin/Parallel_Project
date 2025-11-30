package core;

public class KMeansConfig {

    private int k;                // number of clusters
    private int maxIterations;    // maximum iterations before stopping
    private double tolerance;     // minimum centroid movement to stop

    // Constructor
    public KMeansConfig(int k, int maxIterations, double tolerance) {
        if (k <= 0) throw new IllegalArgumentException("k must be positive");
        if (maxIterations <= 0) throw new IllegalArgumentException("maxIterations must be positive");
        if (tolerance < 0) throw new IllegalArgumentException("tolerance must be non-negative");

        this.k = k;
        this.maxIterations = maxIterations;
        this.tolerance = tolerance;
    }

    // Getters
    public int getK() {
        return k;
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    public double getTolerance() {
        return tolerance;
    }

    // Setters (optional, only if you want to change config after creation)
    public void setK(int k) {
        if (k <= 0) throw new IllegalArgumentException("k must be positive");
        this.k = k;
    }

    public void setMaxIterations(int maxIterations) {
        if (maxIterations <= 0) throw new IllegalArgumentException("maxIterations must be positive");
        this.maxIterations = maxIterations;
    }

    public void setTolerance(double tolerance) {
        if (tolerance < 0) throw new IllegalArgumentException("tolerance must be non-negative");
        this.tolerance = tolerance;
    }

    @Override
    public String toString() {
        return "KMeansConfig{" +
                "k=" + k +
                ", maxIterations=" + maxIterations +
                ", tolerance=" + tolerance +
                '}';
    }
}
