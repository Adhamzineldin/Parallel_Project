package evaluation;

import model.Cluster;
import model.Point;
import util.DistanceUtils;

import java.util.List;

public class SSECalculator {
    
    
    public static double computeSSE(List<Cluster> clusters) {
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
}
