package gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Color palette for visualizing clusters
 * Uses soft, pastel colors that are easy on the eyes
 */
public class ColorPalette {
    
    // Soft, pastel colors that are easy on the eyes
    private static final Color[] BASE_COLORS = {
        new Color(102, 194, 165),  // Soft teal
        new Color(252, 141, 98),   // Soft coral
        new Color(141, 160, 203),  // Soft blue
        new Color(231, 138, 195),  // Soft pink
        new Color(166, 216, 84),   // Soft green
        new Color(255, 217, 47),   // Soft yellow
        new Color(229, 196, 148),  // Soft beige
        new Color(179, 179, 179),  // Soft gray
        new Color(253, 174, 97),  // Soft orange
        new Color(190, 186, 218),  // Soft lavender
        new Color(255, 255, 179),  // Soft cream
        new Color(251, 128, 114),  // Soft salmon
        new Color(128, 177, 211),  // Soft sky blue
        new Color(253, 180, 98),   // Soft peach
        new Color(179, 222, 105),  // Soft lime
        new Color(252, 205, 229),  // Soft rose
        new Color(217, 217, 217),  // Light gray
        new Color(188, 128, 189),  // Soft purple
        new Color(204, 235, 197),  // Soft mint
        new Color(255, 237, 111)   // Soft lemon
    };
    
    private final List<Color> colors;
    
    public ColorPalette(int numClusters) {
        colors = new ArrayList<>();
        
        if (numClusters <= BASE_COLORS.length) {
            // Use base colors directly
            for (int i = 0; i < numClusters; i++) {
                colors.add(BASE_COLORS[i % BASE_COLORS.length]);
            }
        } else {
            // Generate additional soft colors using HSV with lower saturation
            for (int i = 0; i < numClusters; i++) {
                if (i < BASE_COLORS.length) {
                    colors.add(BASE_COLORS[i]);
                } else {
                    // Generate soft pastel color using HSV
                    float hue = (float) (i * 0.618034 % 1.0); // Golden ratio for distribution
                    float saturation = 0.4f + (i % 3) * 0.1f; // Vary saturation between 0.4-0.6
                    float brightness = 0.85f + (i % 2) * 0.1f; // Vary brightness between 0.85-0.95
                    colors.add(Color.getHSBColor(hue, saturation, brightness));
                }
            }
        }
    }
    
    public Color getColor(int clusterIndex) {
        if (clusterIndex < 0 || clusterIndex >= colors.size()) {
            return new Color(200, 200, 200); // Soft gray fallback
        }
        return colors.get(clusterIndex);
    }
    
    public Color getCentroidColor(int clusterIndex) {
        Color baseColor = getColor(clusterIndex);
        // Make centroids darker and more visible with a border
        return new Color(
            Math.max(0, Math.min(255, baseColor.getRed() - 40)),
            Math.max(0, Math.min(255, baseColor.getGreen() - 40)),
            Math.max(0, Math.min(255, baseColor.getBlue() - 40))
        );
    }
    
    public int getSize() {
        return colors.size();
    }
}
