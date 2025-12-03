package gui;

import model.Cluster;
import model.Point;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.List;

/**
 * Panel for displaying 2D scatter plot of clusters
 * Shows points colored by cluster assignment and centroids
 */
public class ScatterPlotPanel extends JPanel {
    
    private List<Cluster> clusters;
    private ColorPalette colorPalette;
    private double minX, maxX, minY, maxY;
    private int dimensionX = 0; // First dimension for X axis
    private int dimensionY = 1; // Second dimension for Y axis
    private boolean showCentroids = true;
    private int pointSize = 5;
    private int centroidSize = 12;
    
    private static final int PADDING = 60;
    private static final Color BACKGROUND_COLOR = new Color(30, 30, 35); // Dark background
    private static final Color GRID_COLOR = new Color(50, 50, 55); // Dark grid
    private static final Color AXIS_COLOR = new Color(180, 180, 180); // Light gray for axes
    private static final Color TEXT_COLOR = new Color(220, 220, 220); // Light text
    
    public ScatterPlotPanel() {
        setBackground(BACKGROUND_COLOR);
        setPreferredSize(new Dimension(800, 600));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLoweredBevelBorder(),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
    }
    
    public void setClusters(List<Cluster> clusters) {
        this.clusters = clusters;
        if (clusters != null && !clusters.isEmpty()) {
            this.colorPalette = new ColorPalette(clusters.size());
            calculateBounds();
        }
        repaint();
    }
    
    public void setDimensions(int dimX, int dimY) {
        this.dimensionX = dimX;
        this.dimensionY = dimY;
        if (clusters != null) {
            calculateBounds();
            repaint();
        }
    }
    
    public void setShowCentroids(boolean show) {
        this.showCentroids = show;
        repaint();
    }
    
    private void calculateBounds() {
        if (clusters == null || clusters.isEmpty()) {
            return;
        }
        
        minX = Double.MAX_VALUE;
        maxX = Double.MIN_VALUE;
        minY = Double.MAX_VALUE;
        maxY = Double.MIN_VALUE;
        
        for (Cluster cluster : clusters) {
            // Check centroid
            Point centroid = cluster.getCentroid();
            if (centroid.getDimension() > dimensionX) {
                minX = Math.min(minX, centroid.getCoordinate(dimensionX));
                maxX = Math.max(maxX, centroid.getCoordinate(dimensionX));
            }
            if (centroid.getDimension() > dimensionY) {
                minY = Math.min(minY, centroid.getCoordinate(dimensionY));
                maxY = Math.max(maxY, centroid.getCoordinate(dimensionY));
            }
            
            // Check all points
            for (Point p : cluster.getPoints()) {
                if (p.getDimension() > dimensionX) {
                    minX = Math.min(minX, p.getCoordinate(dimensionX));
                    maxX = Math.max(maxX, p.getCoordinate(dimensionX));
                }
                if (p.getDimension() > dimensionY) {
                    minY = Math.min(minY, p.getCoordinate(dimensionY));
                    maxY = Math.max(maxY, p.getCoordinate(dimensionY));
                }
            }
        }
        
        // Add padding
        double rangeX = maxX - minX;
        double rangeY = maxY - minY;
        if (rangeX == 0) rangeX = 1;
        if (rangeY == 0) rangeY = 1;
        
        minX -= rangeX * 0.1;
        maxX += rangeX * 0.1;
        minY -= rangeY * 0.1;
        maxY += rangeY * 0.1;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (clusters == null || clusters.isEmpty()) {
            g.setColor(TEXT_COLOR);
            g.drawString("No data to display", getWidth() / 2 - 50, getHeight() / 2);
            return;
        }
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        int width = getWidth() - 2 * PADDING;
        int height = getHeight() - 2 * PADDING;
        
        // Draw grid lines for better readability
        g2d.setColor(GRID_COLOR);
        g2d.setStroke(new BasicStroke(1.0f));
        int gridLines = 5;
        for (int i = 1; i < gridLines; i++) {
            int x = PADDING + (width * i / gridLines);
            int y = PADDING + (height * i / gridLines);
            g2d.drawLine(x, PADDING, x, PADDING + height);
            g2d.drawLine(PADDING, y, PADDING + width, y);
        }
        
        // Draw axes with thicker lines
        g2d.setColor(AXIS_COLOR);
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.drawLine(PADDING, PADDING, PADDING, PADDING + height);
        g2d.drawLine(PADDING, PADDING + height, PADDING + width, PADDING + height);
        
        // Axis labels removed - no dimension numbers displayed
        
        // Draw points and centroids
        for (int i = 0; i < clusters.size(); i++) {
            Cluster cluster = clusters.get(i);
            Color pointColor = colorPalette.getColor(i);
            Color centroidColor = colorPalette.getCentroidColor(i);
            
            // Draw points with slight transparency and border for better visibility
            for (Point p : cluster.getPoints()) {
                if (p.getDimension() > Math.max(dimensionX, dimensionY)) {
                    int x = (int) (PADDING + (p.getCoordinate(dimensionX) - minX) / (maxX - minX) * width);
                    int y = (int) (PADDING + height - (p.getCoordinate(dimensionY) - minY) / (maxY - minY) * height);
                    
                    // Draw point with subtle border
                    g2d.setColor(new Color(pointColor.getRed(), pointColor.getGreen(), pointColor.getBlue(), 200));
                    g2d.fill(new Ellipse2D.Double(x - pointSize / 2, y - pointSize / 2, pointSize, pointSize));
                    g2d.setColor(new Color(pointColor.getRed() / 2, pointColor.getGreen() / 2, pointColor.getBlue() / 2));
                    g2d.setStroke(new BasicStroke(0.5f));
                    g2d.draw(new Ellipse2D.Double(x - pointSize / 2, y - pointSize / 2, pointSize, pointSize));
                }
            }
            
            // Draw centroid with better visibility
            if (showCentroids) {
                Point centroid = cluster.getCentroid();
                if (centroid.getDimension() > Math.max(dimensionX, dimensionY)) {
                    int cx = (int) (PADDING + (centroid.getCoordinate(dimensionX) - minX) / (maxX - minX) * width);
                    int cy = (int) (PADDING + height - (centroid.getCoordinate(dimensionY) - minY) / (maxY - minY) * height);
                    
                    // Draw centroid as a larger circle with light gray border for dark mode
                    g2d.setStroke(new BasicStroke(2.5f));
                    g2d.setColor(new Color(220, 220, 220)); // Light gray instead of white
                    g2d.draw(new Ellipse2D.Double(cx - centroidSize / 2 - 1, cy - centroidSize / 2 - 1, 
                                                 centroidSize + 2, centroidSize + 2));
                    g2d.setColor(centroidColor);
                    g2d.fill(new Ellipse2D.Double(cx - centroidSize / 2, cy - centroidSize / 2, 
                                                 centroidSize, centroidSize));
                }
            }
        }
        
        // Draw legend
        drawLegend(g2d, width, height);
    }
    
    private void drawLegend(Graphics2D g2d, int width, int height) {
        int legendX = PADDING + width - 160;
        int legendY = PADDING + 20;
        int legendWidth = 150;
        int legendHeight = Math.min(350, clusters.size() * 22 + 40);
        
        // Draw legend background with dark mode styling
        g2d.setColor(new Color(40, 40, 45, 250));
        g2d.fillRoundRect(legendX, legendY, legendWidth, legendHeight, 10, 10);
        g2d.setColor(new Color(120, 120, 120));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(legendX, legendY, legendWidth, legendHeight, 10, 10);
        
        // Draw legend title
        g2d.setFont(g2d.getFont().deriveFont(Font.BOLD, 13f));
        g2d.setColor(TEXT_COLOR);
        g2d.drawString("Clusters", legendX + 15, legendY + 22);
        
        // Draw separator line
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.setColor(new Color(100, 100, 100));
        g2d.drawLine(legendX + 10, legendY + 30, legendX + legendWidth - 10, legendY + 30);
        
        // Draw cluster items with better formatting
        g2d.setFont(g2d.getFont().deriveFont(Font.PLAIN, 11f));
        int itemY = legendY + 48;
        for (int i = 0; i < clusters.size() && itemY < legendY + legendHeight - 10; i++) {
            Color c = colorPalette.getColor(i);
            // Draw color box with border
            g2d.setColor(c);
            g2d.fillRect(legendX + 12, itemY - 10, 18, 12);
            g2d.setColor(new Color(150, 150, 150));
            g2d.setStroke(new BasicStroke(1.0f));
            g2d.drawRect(legendX + 12, itemY - 10, 18, 12);
            
            // Draw text in light color for dark mode
            g2d.setColor(TEXT_COLOR);
            String label = String.format("C%d: %d pts", i, clusters.get(i).getPoints().size());
            g2d.drawString(label, legendX + 35, itemY);
            itemY += 22;
        }
    }
}
