package model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataSetLoader {

    /**
     * Generic CSV loader: picks only numeric columns based on indexes
     */
    public static List<Point> loadCSV(String filePath, int[] numericColumns) {
        List<Point> points = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
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
            e.printStackTrace();
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
