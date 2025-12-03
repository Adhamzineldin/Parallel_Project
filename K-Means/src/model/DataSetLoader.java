package model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataSetLoader {

    /**
     * Resolve file path - tries multiple locations
     */
    private static String resolveFilePath(String relativePath) {
        // Try current working directory
        File file = new File(relativePath);
        if (file.exists() && file.isFile()) {
            return file.getAbsolutePath();
        }
        
        // Try relative to project root (K-Means directory)
        file = new File("K-Means", relativePath);
        if (file.exists() && file.isFile()) {
            return file.getAbsolutePath();
        }
        
        // Try from parent directory (if running from project root)
        file = new File("..", relativePath);
        if (file.exists() && file.isFile()) {
            return file.getAbsolutePath();
        }
        
        // Try from current directory's parent
        String currentDir = System.getProperty("user.dir");
        if (currentDir != null) {
            File currentDirFile = new File(currentDir);
            // If we're in out/production/K-Means, go up to project root
            if (currentDirFile.getName().equals("K-Means") || 
                currentDirFile.getPath().contains("out")) {
                File projectRoot = currentDirFile;
                while (projectRoot != null && !new File(projectRoot, "data").exists()) {
                    projectRoot = projectRoot.getParentFile();
                }
                if (projectRoot != null) {
                    file = new File(projectRoot, relativePath);
                    if (file.exists() && file.isFile()) {
                        return file.getAbsolutePath();
                    }
                }
            } else {
                // Try directly from current directory
                file = new File(currentDir, relativePath);
                if (file.exists() && file.isFile()) {
                    return file.getAbsolutePath();
                }
                // Try K-Means subdirectory
                file = new File(currentDir, "K-Means" + File.separator + relativePath);
                if (file.exists() && file.isFile()) {
                    return file.getAbsolutePath();
                }
            }
        }
        
        // Return original path as fallback (will throw error if not found)
        return relativePath;
    }

    /**
     * Generic CSV loader: picks only numeric columns based on indexes
     */
    public static List<Point> loadCSV(String filePath, int[] numericColumns) {
        List<Point> points = new ArrayList<>();
        
        String resolvedPath = resolveFilePath(filePath);

        try (BufferedReader br = new BufferedReader(new FileReader(resolvedPath))) {
            String line = br.readLine(); // skip header

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] tokens = line.split(",");
                double[] coords = new double[numericColumns.length];

                for (int i = 0; i < numericColumns.length; i++) {
                    // Remove $ or k$ if present and parse as double
                    String val = tokens[numericColumns[i]].trim().replace("$","").replace("k","").replace("K","");
                    coords[i] = Double.parseDouble(val);
                }

                points.add(new Point(coords));
            }

        } catch (IOException e) {
            System.err.println("Error loading CSV file: " + resolvedPath);
            System.err.println("Current working directory: " + System.getProperty("user.dir"));
            System.err.println("Please ensure the data file exists at: " + resolvedPath);
            e.printStackTrace();
            throw new RuntimeException("Failed to load dataset from: " + resolvedPath, e);
        }

        return points;
    }

    /**
     * Preconfigured loader for the Mall Customers dataset
     */
    public static List<Point> loadMallDataset() {
        // Age=2, Annual Income=3, Spending Score=4
        int[] mallCols = {2, 3, 4};
        return loadCSV("data/Mall_Customers.csv", mallCols);
    }

    /**
     * Preconfigured loader for the Bank/Credit Card dataset
     */
    public static List<Point> loadBankDataset() {
        // Numeric columns: Age, Dependent_count, Months_on_book, Total_Relationship_Count,
        // Months_Inactive_12_mon, Contacts_Count_12_mon, Credit_Limit, Total_Revolving_Bal,
        // Avg_Open_To_Buy, Total_Amt_Chng_Q4_Q1, Total_Trans_Amt, Total_Trans_Ct,
        // Total_Ct_Chng_Q4_Q1, Avg_Utilization_Ratio
        int[] bankCols = {2, 4, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20};
        return loadCSV("data/BankChurners.csv", bankCols);
    }

    /**
     * Generate a synthetic 2D dataset with multiple clusters
     * Creates well-separated clusters for testing K-Means
     * 
     * @param numPoints Total number of points to generate
     * @param numClusters Number of distinct clusters to create
     * @param noiseLevel Amount of noise/variance (0.0 to 1.0)
     * @return List of 2D points
     */
    public static List<Point> generateSynthetic2DDataset(int numPoints, int numClusters, double noiseLevel) {
        List<Point> points = new ArrayList<>();
        java.util.Random random = new java.util.Random(42); // Fixed seed for reproducibility
        
        // Generate cluster centers spread across a 2D space
        double[][] clusterCenters = new double[numClusters][2];
        for (int i = 0; i < numClusters; i++) {
            // Spread centers in a grid-like pattern
            double angle = 2 * Math.PI * i / numClusters;
            double radius = 50 + (i % 3) * 30; // Vary distance from center
            clusterCenters[i][0] = 100 + radius * Math.cos(angle);
            clusterCenters[i][1] = 100 + radius * Math.sin(angle);
        }
        
        // Generate points around each cluster center
        int pointsPerCluster = numPoints / numClusters;
        int remainingPoints = numPoints % numClusters;
        
        for (int clusterIdx = 0; clusterIdx < numClusters; clusterIdx++) {
            int pointsInThisCluster = pointsPerCluster + (clusterIdx < remainingPoints ? 1 : 0);
            
            for (int i = 0; i < pointsInThisCluster; i++) {
                // Generate point with Gaussian distribution around cluster center
                double x = clusterCenters[clusterIdx][0] + random.nextGaussian() * (10 + noiseLevel * 20);
                double y = clusterCenters[clusterIdx][1] + random.nextGaussian() * (10 + noiseLevel * 20);
                
                points.add(new Point(x, y));
            }
        }
        
        // Shuffle points to mix clusters
        java.util.Collections.shuffle(points, random);
        
        return points;
    }

    /**
     * Generate a default synthetic 2D dataset (300 points, 4 clusters, moderate noise)
     */
    public static List<Point> generateSynthetic2DDataset() {
        return generateSynthetic2DDataset(300, 4, 0.3);
    }

    /**
     * Save a list of points to a CSV file
     * 
     * @param points The points to save
     * @param filePath The path where to save the CSV file
     * @param includeHeader Whether to include a header row
     */
    public static void savePointsToCSV(List<Point> points, String filePath, boolean includeHeader) {
        if (points == null || points.isEmpty()) {
            throw new IllegalArgumentException("Points list cannot be null or empty");
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Write header if requested
            if (includeHeader) {
                int dimensions = points.get(0).getDimension();
                StringBuilder header = new StringBuilder();
                for (int i = 0; i < dimensions; i++) {
                    if (i > 0) header.append(",");
                    header.append("Dimension_").append(i);
                }
                writer.write(header.toString());
                writer.newLine();
            }

            // Write data points
            for (Point p : points) {
                double[] coords = p.getCoordinates();
                StringBuilder line = new StringBuilder();
                for (int i = 0; i < coords.length; i++) {
                    if (i > 0) line.append(",");
                    line.append(coords[i]);
                }
                writer.write(line.toString());
                writer.newLine();
            }

        } catch (IOException e) {
            System.err.println("Error saving CSV file: " + filePath);
            e.printStackTrace();
            throw new RuntimeException("Failed to save dataset to: " + filePath, e);
        }
    }

    /**
     * Save synthetic dataset to CSV file in the data directory
     * 
     * @param numPoints Number of points
     * @param numClusters Number of clusters
     * @param noiseLevel Noise level
     * @param filename Output filename (e.g., "synthetic_2d.csv")
     */
    public static void generateAndSaveSynthetic2D(int numPoints, int numClusters, double noiseLevel, String filename) {
        List<Point> points = generateSynthetic2DDataset(numPoints, numClusters, noiseLevel);
        String filePath = "data/" + filename;
        savePointsToCSV(points, filePath, true);
        System.out.println("Generated and saved " + numPoints + " points to " + filePath);
    }

    /**
     * Example main to test loading
     */
    public static void main(String[] args) {
        List<Point> mallPoints = loadMallDataset();
        System.out.println("Mall dataset: " + mallPoints.size() + " points");
        System.out.println("Sample: " + mallPoints.get(0));

        List<Point> bankPoints = loadBankDataset();
        System.out.println("Bank dataset: " + bankPoints.size() + " points");
        System.out.println("Sample: " + bankPoints.get(0));
    }
}
