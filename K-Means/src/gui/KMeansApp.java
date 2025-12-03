package gui;

import bonus.MultiStartKMeans;
import bonus.MultiStartResult;
import core.KMeansConfig;
import core.KMeansParallel;
import core.KMeansSequential;
import evaluation.SSECalculator;
import model.Cluster;
import model.DataSetLoader;
import model.Point;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main GUI application for K-Means Clustering
 * Provides visualization, controls, and results display
 */
public class KMeansApp extends JFrame {

    // UI Components
    private JComboBox<String> datasetCombo;
    private JSpinner kSpinner;
    private JSpinner maxIterationsSpinner;
    private JSpinner toleranceSpinner;
    private JRadioButton sequentialRadio;
    private JRadioButton parallelRadio;
    private JCheckBox kmeansPlusPlusCheck;
    private JCheckBox multiStartCheck;
    private JSpinner numRestartsSpinner;
    private JButton runButton;
    private JButton animateButton;
    private JButton stopButton;

    private ScatterPlotPanel scatterPlot;
    private JTable resultsTable;
    private DefaultTableModel tableModel;
    private JTextArea infoArea;

    // Data
    private List<Point> currentDataset;
    private List<Cluster> currentClusters;
    private CentroidAnimationController animationController;
    private ExecutorService executor;

    public KMeansApp() {
        super("K-Means Clustering Visualization");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Set dark mode colors
        getContentPane().setBackground(new Color(35, 35, 40));

        // Set better default font
        UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 12));
        UIManager.put("Button.font", new Font("Segoe UI", Font.PLAIN, 12));
        UIManager.put("TextField.font", new Font("Segoe UI", Font.PLAIN, 12));
        Color darkText = new Color(220, 220, 220);
        UIManager.put("Label.foreground", darkText);
        UIManager.put("Button.background", new Color(70, 130, 180));
        UIManager.put("Button.foreground", darkText);
        UIManager.put("RadioButton.foreground", darkText);
        UIManager.put("CheckBox.foreground", darkText);
        UIManager.put("ComboBox.foreground", darkText);
        UIManager.put("ComboBox.background", new Color(40, 40, 45));

        executor = Executors.newSingleThreadExecutor();

        createComponents();
        layoutComponents();

        applyDarkMode();

        pack();
        setLocationRelativeTo(null);
        setSize(1500, 950);
    }

    private void applyDarkMode() {
        Color darkBg = new Color(40, 40, 45);
        Color darkText = new Color(220, 220, 220);
        Color darkBorder = new Color(60, 60, 65);

        applyDarkModeRecursive(getContentPane(), darkBg, darkText, darkBorder);

        runButton.setBackground(new Color(70, 130, 180));
        runButton.setForeground(darkText);
        runButton.setOpaque(true);
        runButton.setBorderPainted(true);

        animateButton.setBackground(new Color(60, 179, 113));
        animateButton.setForeground(darkText);
        animateButton.setOpaque(true);
        animateButton.setBorderPainted(true);

        stopButton.setBackground(new Color(220, 20, 60));
        stopButton.setForeground(darkText);
        stopButton.setOpaque(true);
        stopButton.setBorderPainted(true);
    }

    private void applyDarkModeRecursive(Container container, Color bg, Color text, Color border) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                label.setForeground(text);
                label.setOpaque(false);
            } else if (comp instanceof JButton) {
                JButton btn = (JButton) comp;
                if (btn.getText() != null) {
                    String btnText = btn.getText();
                    if (btnText.equals("Run K-Means")) {
                        btn.setBackground(new Color(70, 130, 180));
                        btn.setForeground(text);
                    } else if (btnText.equals("Animate")) {
                        btn.setBackground(new Color(60, 179, 113));
                        btn.setForeground(text);
                    } else if (btnText.equals("Stop")) {
                        btn.setBackground(new Color(220, 20, 60));
                        btn.setForeground(text);
                    } else if (btnText.equals("Update Plot")) {
                        btn.setBackground(new Color(70, 130, 180));
                        btn.setForeground(text);
                    } else {
                        btn.setBackground(border);
                        btn.setForeground(text);
                    }
                }
                btn.setOpaque(true);
                btn.setBorderPainted(true);
            } else if (comp instanceof JSpinner) {
                JSpinner spinner = (JSpinner) comp;
                spinner.setBackground(bg);
                spinner.setForeground(text);
                spinner.setOpaque(true);
                if (spinner.getEditor() instanceof JSpinner.DefaultEditor) {
                    JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner.getEditor();
                    JTextField field = editor.getTextField();
                    field.setBackground(bg);
                    field.setForeground(text);
                    field.setCaretColor(text);
                    field.setOpaque(true);
                    field.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(100, 100, 100), 1),
                            BorderFactory.createEmptyBorder(2, 5, 2, 5)
                    ));
                }
                for (Component child : spinner.getComponents()) {
                    if (child instanceof JButton) {
                        ((JButton) child).setBackground(border);
                        ((JButton) child).setForeground(text);
                        ((JButton) child).setOpaque(true);
                    }
                }
            } else if (comp instanceof JComboBox) {
                JComboBox<?> combo = (JComboBox<?>) comp;
                combo.setBackground(bg);
                combo.setForeground(text);
                combo.setOpaque(true);
                combo.setRenderer(new DefaultListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                        Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                        c.setBackground(isSelected ? new Color(70, 130, 180) : bg);
                        c.setForeground(text);
                        if (c instanceof JComponent) {
                            ((JComponent) c).setOpaque(true);
                        }
                        return c;
                    }
                });
                for (Component child : combo.getComponents()) {
                    if (child instanceof JButton) {
                        ((JButton) child).setBackground(border);
                        ((JButton) child).setForeground(text);
                        ((JButton) child).setOpaque(true);
                    }
                }
            } else if (comp instanceof JRadioButton) {
                JRadioButton radio = (JRadioButton) comp;
                radio.setBackground(bg);
                radio.setForeground(text);
                radio.setOpaque(false);
            } else if (comp instanceof JCheckBox) {
                JCheckBox checkbox = (JCheckBox) comp;
                checkbox.setBackground(bg);
                checkbox.setForeground(text);
                checkbox.setOpaque(false);
            } else if (comp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) comp;
                scrollPane.setBackground(bg);
                scrollPane.getViewport().setBackground(bg);
                scrollPane.setOpaque(true);
                scrollPane.getViewport().setOpaque(true);
                scrollPane.getVerticalScrollBar().setBackground(bg);
                scrollPane.getHorizontalScrollBar().setBackground(bg);
                applyDarkModeRecursive(scrollPane.getViewport(), bg, text, border);
            } else if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                if (panel.getBorder() == null || !(panel.getBorder() instanceof javax.swing.border.TitledBorder)) {
                    panel.setBackground(bg);
                    panel.setOpaque(true);
                }
                applyDarkModeRecursive(panel, bg, text, border);
            } else if (comp instanceof JTextField) {
                JTextField field = (JTextField) comp;
                field.setBackground(bg);
                field.setForeground(text);
                field.setCaretColor(text);
                field.setOpaque(true);
            } else if (comp instanceof Container) {
                applyDarkModeRecursive((Container) comp, bg, text, border);
            }
        }
    }

    private void createComponents() {
        // Dataset selection with custom UI for dark mode
        datasetCombo = new JComboBox<>(new String[]{"Mall Customers", "Bank Customers"});
        datasetCombo.setBackground(new Color(40, 40, 45));
        datasetCombo.setForeground(new Color(220, 220, 220));
        datasetCombo.setOpaque(true);
        datasetCombo.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton button = super.createArrowButton();
                button.setBackground(new Color(60, 60, 65));
                button.setForeground(new Color(220, 220, 220));
                return button;
            }
        });
        datasetCombo.addActionListener(e -> loadDataset());

        // Configuration spinners
        kSpinner = new JSpinner(new SpinnerNumberModel(3, 2, 50, 1));
        maxIterationsSpinner = new JSpinner(new SpinnerNumberModel(100, 1, 10000, 10));
        toleranceSpinner = new JSpinner(new SpinnerNumberModel(0.0001, 0.0000000000001, 1.0, 0.0000001));
        JSpinner.NumberEditor toleranceEditor = new JSpinner.NumberEditor(toleranceSpinner, "0.0000000000000");
        toleranceSpinner.setEditor(toleranceEditor);

        // Implementation selection
        sequentialRadio = new JRadioButton("Sequential", true);
        parallelRadio = new JRadioButton("Parallel");
        ButtonGroup implGroup = new ButtonGroup();
        implGroup.add(sequentialRadio);
        implGroup.add(parallelRadio);

        // Initialization options
        kmeansPlusPlusCheck = new JCheckBox("Use k-means++ Initialization");

        // MultiStart options
        multiStartCheck = new JCheckBox("MultiStart (Multiple Restarts)");
        numRestartsSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 50, 1));
        numRestartsSpinner.setEnabled(false);
        multiStartCheck.addActionListener(e ->
                numRestartsSpinner.setEnabled(multiStartCheck.isSelected()));

        // Buttons
        runButton = new JButton("Run K-Means");
        runButton.setBackground(new Color(40, 40, 45));
        runButton.setForeground(Color.WHITE);
        runButton.setContentAreaFilled(false);
        runButton.setOpaque(true);
        runButton.setFocusPainted(false);
        runButton.setBorderPainted(true);
        runButton.addActionListener(e -> runKMeans());

        animateButton = new JButton("Animate");
        animateButton.setBackground(new Color(40, 40, 45));
        animateButton.setForeground(Color.WHITE);
        animateButton.setContentAreaFilled(false);
        animateButton.setOpaque(true);
        animateButton.setFocusPainted(false);
        animateButton.setBorderPainted(true);
        animateButton.addActionListener(e -> animateKMeans());

        stopButton = new JButton("Stop");
        stopButton.setBackground(new Color(40, 40, 45));
        stopButton.setForeground(Color.WHITE);
        stopButton.setContentAreaFilled(false);
        stopButton.setOpaque(true);
        stopButton.setFocusPainted(false);
        stopButton.setBorderPainted(true);
        stopButton.setEnabled(false);
        stopButton.addActionListener(e -> stopAnimation());

        // Visualization
        scatterPlot = new ScatterPlotPanel();

        // Results table
        String[] columnNames = {"Method", "K", "SSE", "Runtime (ms)", "Iterations", "Initialization"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        resultsTable = new JTable(tableModel);
        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultsTable.getSelectionModel().addListSelectionListener(e -> {
            int row = resultsTable.getSelectedRow();
            if (row >= 0) {
                // Could load the clusters for that result
            }
        });

        // Info area
        infoArea = new JTextArea(5, 30);
        infoArea.setEditable(false);
        infoArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        loadDataset();
    }

    private void layoutComponents() {
        // Control panel (left)
        JPanel controlPanel = createControlPanel();

        // Visualization panel (center)
        JPanel visPanel = new JPanel(new BorderLayout());
        visPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "2D Scatter Plot",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13)));
        visPanel.setBackground(new Color(30, 30, 35));
        visPanel.setForeground(new Color(220, 220, 220));
        visPanel.add(scatterPlot, BorderLayout.CENTER);

        // Dimension selection for plot
        JPanel dimPanel = new JPanel(new FlowLayout());
        dimPanel.setBackground(new Color(30, 30, 35));
        dimPanel.setOpaque(true);
        JLabel xLabel = new JLabel("X Dimension:");
        xLabel.setForeground(new Color(220, 220, 220));
        xLabel.setOpaque(false);
        dimPanel.add(xLabel);
        JSpinner xDimSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 20, 1));
        xDimSpinner.setBackground(new Color(40, 40, 45));
        xDimSpinner.setOpaque(true);
        if (xDimSpinner.getEditor() instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor) xDimSpinner.getEditor()).getTextField().setBackground(new Color(40, 40, 45));
            ((JSpinner.DefaultEditor) xDimSpinner.getEditor()).getTextField().setForeground(new Color(220, 220, 220));
            ((JSpinner.DefaultEditor) xDimSpinner.getEditor()).getTextField().setOpaque(true);
        }
        dimPanel.add(xDimSpinner);
        JLabel yLabel = new JLabel("Y Dimension:");
        yLabel.setForeground(new Color(220, 220, 220));
        yLabel.setOpaque(false);
        dimPanel.add(yLabel);
        JSpinner yDimSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 20, 1));
        yDimSpinner.setBackground(new Color(40, 40, 45));
        yDimSpinner.setOpaque(true);
        if (yDimSpinner.getEditor() instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor) yDimSpinner.getEditor()).getTextField().setBackground(new Color(40, 40, 45));
            ((JSpinner.DefaultEditor) yDimSpinner.getEditor()).getTextField().setForeground(new Color(220, 220, 220));
            ((JSpinner.DefaultEditor) yDimSpinner.getEditor()).getTextField().setOpaque(true);
        }
        dimPanel.add(yDimSpinner);
        JButton updatePlotButton = new JButton("Update Plot");
        updatePlotButton.setOpaque(true);
        updatePlotButton.setBackground(new Color(40, 40, 45));
        updatePlotButton.setForeground(Color.WHITE);
        updatePlotButton.setContentAreaFilled(false);
        updatePlotButton.setFocusPainted(false);
        updatePlotButton.setBorderPainted(true);
        updatePlotButton.addActionListener(e -> {
            int xDim = (Integer) xDimSpinner.getValue();
            int yDim = (Integer) yDimSpinner.getValue();
            scatterPlot.setDimensions(xDim, yDim);
        });
        dimPanel.add(updatePlotButton);
        visPanel.add(dimPanel, BorderLayout.SOUTH);

        // Results panel (right)
        JPanel resultsPanel = new JPanel(new BorderLayout());
        resultsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Results",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13)));
        resultsPanel.setBackground(new Color(40, 40, 45));
        resultsPanel.setForeground(new Color(220, 220, 220));
        resultsTable.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        resultsTable.setRowHeight(22);
        resultsTable.setBackground(new Color(50, 50, 55));
        resultsTable.setForeground(new Color(220, 220, 220));
        resultsTable.setGridColor(new Color(70, 70, 75));
        resultsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        resultsTable.getTableHeader().setBackground(new Color(60, 60, 65));
        resultsTable.getTableHeader().setForeground(new Color(220, 220, 220));
        resultsTable.getTableHeader().setOpaque(true);
        resultsTable.getTableHeader().setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            {
                setHorizontalAlignment(CENTER);
                setBackground(new Color(60, 60, 65));
                setForeground(new Color(220, 220, 220));
                setFont(new Font("Segoe UI", Font.BOLD, 11));
                setOpaque(true);
            }
        });
        resultsPanel.add(new JScrollPane(resultsTable), BorderLayout.CENTER);

        // Info panel
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Information",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13)));
        infoPanel.setBackground(new Color(40, 40, 45));
        infoPanel.setForeground(new Color(220, 220, 220));
        infoArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        infoArea.setBackground(new Color(35, 35, 40));
        infoArea.setForeground(new Color(220, 220, 220));
        infoArea.setCaretColor(new Color(220, 220, 220));
        infoPanel.add(new JScrollPane(infoArea), BorderLayout.CENTER);

        // Right side panel
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(resultsPanel, BorderLayout.CENTER);
        rightPanel.add(infoPanel, BorderLayout.SOUTH);

        // Main layout
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, controlPanel, visPanel);
        mainSplit.setDividerLocation(300);
        mainSplit.setResizeWeight(0.2);

        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, mainSplit, rightPanel);
        rightSplit.setDividerLocation(600);
        rightSplit.setResizeWeight(0.6);

        add(rightSplit, BorderLayout.CENTER);
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Configuration",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13)));
        panel.setBackground(new Color(40, 40, 45));
        panel.setForeground(new Color(220, 220, 220));
        panel.setPreferredSize(new Dimension(300, 650));

        panel.add(createLabeledComponent("Dataset:", datasetCombo));
        panel.add(Box.createVerticalStrut(10));

        panel.add(createLabeledComponent("Number of Clusters (K):", kSpinner));
        panel.add(Box.createVerticalStrut(10));

        panel.add(createLabeledComponent("Max Iterations:", maxIterationsSpinner));
        panel.add(Box.createVerticalStrut(10));

        panel.add(createLabeledComponent("Tolerance:", toleranceSpinner));
        panel.add(Box.createVerticalStrut(10));

        JPanel implPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        implPanel.setBackground(new Color(40, 40, 45));
        JLabel implLabel = new JLabel("Implementation:");
        implLabel.setForeground(new Color(220, 220, 220));
        implLabel.setOpaque(false);
        implPanel.add(implLabel);
        implPanel.add(sequentialRadio);
        implPanel.add(parallelRadio);
        panel.add(implPanel);
        panel.add(Box.createVerticalStrut(10));

        panel.add(kmeansPlusPlusCheck);
        panel.add(Box.createVerticalStrut(10));

        panel.add(Box.createVerticalStrut(5));
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(100, 100, 100));
        panel.add(separator);
        panel.add(Box.createVerticalStrut(5));
        panel.add(multiStartCheck);
        JPanel restartPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        restartPanel.setBackground(new Color(40, 40, 45));
        JLabel restartLabel = new JLabel("Number of Restarts:");
        restartLabel.setForeground(new Color(220, 220, 220));
        restartLabel.setOpaque(false);
        restartPanel.add(restartLabel);
        restartPanel.add(numRestartsSpinner);
        panel.add(restartPanel);
        panel.add(Box.createVerticalStrut(20));

        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        buttonPanel.add(runButton);
        buttonPanel.add(animateButton);
        buttonPanel.add(stopButton);
        panel.add(buttonPanel);

        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel createLabeledComponent(String label, JComponent component) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(new Color(40, 40, 45));
        JLabel labelComponent = new JLabel(label);
        labelComponent.setForeground(new Color(220, 220, 220));
        labelComponent.setOpaque(false);
        panel.add(labelComponent);
        panel.add(component);
        return panel;
    }

    private void loadDataset() {
        SwingUtilities.invokeLater(() -> {
            try {
                int selection = datasetCombo.getSelectedIndex();
                if (selection == 0) {
                    currentDataset = DataSetLoader.loadMallDataset();
                    infoArea.setText("Loaded Mall Customers dataset: " + currentDataset.size() + " points\n" +
                            "Dimensions: " + (currentDataset.isEmpty() ? 0 : currentDataset.get(0).getDimension()));
                } else {
                    currentDataset = DataSetLoader.loadBankDataset();
                    infoArea.setText("Loaded Bank Customers dataset: " + currentDataset.size() + " points\n" +
                            "Dimensions: " + (currentDataset.isEmpty() ? 0 : currentDataset.get(0).getDimension()));
                }

                if (!currentDataset.isEmpty()) {
                    int maxDim = currentDataset.get(0).getDimension() - 1;
                }

                currentClusters = null;
                scatterPlot.setClusters(null);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error loading dataset: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void runKMeans() {
        if (currentDataset == null || currentDataset.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please load a dataset first.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        runButton.setEnabled(false);
        stopButton.setEnabled(false);

        executor.execute(() -> {
            try {
                int k = (Integer) kSpinner.getValue();
                int maxIter = (Integer) maxIterationsSpinner.getValue();
                double tol = (Double) toleranceSpinner.getValue();
                boolean useParallel = parallelRadio.isSelected();
                boolean useKMeansPlusPlus = kmeansPlusPlusCheck.isSelected();
                boolean useMultiStart = multiStartCheck.isSelected();
                int numRestarts = (Integer) numRestartsSpinner.getValue();

                KMeansConfig config = new KMeansConfig(k, maxIter, tol);

                long startTime = System.currentTimeMillis();
                List<Cluster> clusters;
                double sse;
                long runtime;
                String method;
                String initType;

                int iterations = 0;
                if (useMultiStart) {
                    MultiStartKMeans multiStart = new MultiStartKMeans(config, currentDataset,
                            numRestarts, useKMeansPlusPlus, useParallel);
                    MultiStartResult result = multiStart.run();
                    clusters = result.getClusters();
                    sse = result.getSSE();
                    runtime = result.getTotalTime();
                    iterations = result.getIterations();
                    method = useParallel ? "MultiStart Parallel" : "MultiStart Sequential";
                    initType = useKMeansPlusPlus ? "k-means++" : "Random";

                    final int finalIterations = iterations;
                    final long finalRuntime = runtime;
                    final int finalNumRestarts = numRestarts;

                    SwingUtilities.invokeLater(() -> {
                        infoArea.append("\nMultiStart completed:\n");
                        infoArea.append("Best restart: " + result.getBestRestart() + "\n");
                        infoArea.append("Iterations: " + finalIterations + "\n");
                        infoArea.append("Average time per restart: " + (finalRuntime / finalNumRestarts) + " ms\n");
                    });
                } else {
                    if (useParallel) {
                        KMeansParallel kmeans = new KMeansParallel(config, currentDataset);
                        if (useKMeansPlusPlus) {
                            List<Cluster> initialClusters = bonus.KMeansPlusPlusInitializer.initializeClusters(currentDataset, k);
                            kmeans.setInitialClusters(initialClusters);
                        }
                        kmeans.run();
                        clusters = kmeans.getClusters();
                        sse = kmeans.computeSSE();
                        iterations = kmeans.getIterationsCompleted();
                    } else {
                        KMeansSequential kmeans = new KMeansSequential(config, currentDataset);
                        if (useKMeansPlusPlus) {
                            List<Cluster> initialClusters = bonus.KMeansPlusPlusInitializer.initializeClusters(currentDataset, k);
                            kmeans.setInitialClusters(initialClusters);
                        }
                        kmeans.run();
                        clusters = kmeans.getClusters();
                        sse = kmeans.computeSSE();
                        iterations = kmeans.getIterationsCompleted();
                    }

                    long endTime = System.currentTimeMillis();
                    runtime = endTime - startTime;
                    method = useParallel ? "Parallel" : "Sequential";
                    initType = useKMeansPlusPlus ? "k-means++" : "Random";
                }

                final List<Cluster> finalClusters = clusters;
                final double finalSSE = sse;
                final long finalRuntime = runtime;
                final int finalIterations = iterations;
                final String finalMethod = method;
                final String finalInitType = initType;

                SwingUtilities.invokeLater(() -> {
                    currentClusters = finalClusters;
                    scatterPlot.setClusters(finalClusters);

                    tableModel.addRow(new Object[]{
                            finalMethod,
                            k,
                            String.format("%.4f", finalSSE),
                            finalRuntime + " ms",
                            finalIterations,
                            finalInitType
                    });

                    infoArea.append("\n" + finalMethod + " completed:\n");
                    infoArea.append("SSE: " + String.format("%.4f", finalSSE) + "\n");
                    infoArea.append("Iterations: " + finalIterations + "\n");
                    infoArea.append("Runtime: " + finalRuntime + " ms\n");

                    runButton.setEnabled(true);
                });

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Error running K-Means: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    runButton.setEnabled(true);
                    e.printStackTrace();
                });
            }
        });
    }

    private void animateKMeans() {
        if (currentDataset == null || currentDataset.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please load a dataset first.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        runButton.setEnabled(false);
        animateButton.setEnabled(false);
        stopButton.setEnabled(true);

        executor.execute(() -> {
            try {
                int k = (Integer) kSpinner.getValue();
                int maxIter = (Integer) maxIterationsSpinner.getValue();
                double tol = (Double) toleranceSpinner.getValue();
                boolean useParallel = parallelRadio.isSelected();
                boolean useKMeansPlusPlus = kmeansPlusPlusCheck.isSelected();

                KMeansConfig config = new KMeansConfig(k, maxIter, tol);

                animationController = new CentroidAnimationController(config, currentDataset,
                        useParallel, useKMeansPlusPlus);

                animationController.runWithAnimation(new CentroidAnimationController.AnimationListener() {
                    @Override
                    public void onIterationComplete(List<Cluster> clusters, int iteration, double sse) {
                        try {
                            SwingUtilities.invokeAndWait(() -> {
                                scatterPlot.setClusters(clusters);
                                infoArea.setText("Iteration: " + iteration + "\nSSE: " + String.format("%.4f", sse));
                                scatterPlot.repaint();
                            });
                        } catch (Exception e) {
                            SwingUtilities.invokeLater(() -> {
                                scatterPlot.setClusters(clusters);
                                infoArea.setText("Iteration: " + iteration + "\nSSE: " + String.format("%.4f", sse));
                                scatterPlot.repaint();
                            });
                        }
                    }

                    @Override
                    public void onAnimationComplete(List<Cluster> finalClusters, double finalSSE, long totalTime, int iterations) {
                        SwingUtilities.invokeLater(() -> {
                            currentClusters = finalClusters;
                            scatterPlot.setClusters(finalClusters);

                            String method = useParallel ? "Animated Parallel" : "Animated Sequential";
                            String initType = useKMeansPlusPlus ? "k-means++" : "Random";

                            tableModel.addRow(new Object[]{
                                    method,
                                    k,
                                    String.format("%.4f", finalSSE),
                                    totalTime + " ms",
                                    iterations,
                                    initType
                            });

                            infoArea.setText("Animation Complete!\n" +
                                    "Final SSE: " + String.format("%.4f", finalSSE) + "\n" +
                                    "Iterations: " + iterations + "\n" +
                                    "Total Time: " + totalTime + " ms");

                            runButton.setEnabled(true);
                            animateButton.setEnabled(true);
                            stopButton.setEnabled(false);
                        });
                    }
                });

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Error animating K-Means: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    runButton.setEnabled(true);
                    animateButton.setEnabled(true);
                    stopButton.setEnabled(false);
                    e.printStackTrace();
                });
            }
        });
    }

    private void stopAnimation() {
        if (animationController != null) {
            animationController.stop();
        }
        SwingUtilities.invokeLater(() -> {
            runButton.setEnabled(true);
            animateButton.setEnabled(true);
            stopButton.setEnabled(false);
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                String lookAndFeel = javax.swing.UIManager.getSystemLookAndFeelClassName();
                javax.swing.UIManager.setLookAndFeel(lookAndFeel);
            } catch (Exception e) {
                e.printStackTrace();
            }

            new KMeansApp().setVisible(true);
        });
    }
}