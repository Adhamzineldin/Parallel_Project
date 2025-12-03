import gui.KMeansApp;

import javax.swing.*;

/**
 * Main entry point for K-Means Clustering Application
 * Launches the GUI application
 */
public class Main {
    
    
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                String lookAndFeel = javax.swing.UIManager.getSystemLookAndFeelClassName();
                javax.swing.UIManager.setLookAndFeel(lookAndFeel);
            } catch (Exception e) {
                // Use default look and feel if system L&F is not available
                e.printStackTrace();
            }

            new KMeansApp().setVisible(true);
        });
    }
}
