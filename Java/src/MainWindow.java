import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XChartPanel;

/**
 * Main application window for the Spectrometer GUI.
 * Provides menu-driven interface for spectrometer control, data visualization, and file operations.
 */
public class MainWindow extends JFrame {

    // ==================== CONSTANTS ====================
    
    private static final int WINDOW_WIDTH = 1000;
    private static final int WINDOW_HEIGHT = 600;
    private static final int LEFT_PANEL_WIDTH = 250;
    
    // ==================== UI COMPONENTS ====================
    
    private DefaultListModel<String> measurementListModel;
    private JList<String> measurementList;
    private JPanel centerPanel;
    
    // ==================== DATA ====================
    
    private Spectrometer spectrometer;
    private Map<String, MeasurementSet> measurementSets = new HashMap<>();
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Constructs the main window and initializes all UI components.
     */
    public MainWindow() {
        initializeWindow();
        createMenuBar();
        createLeftPanel();
        createCenterPanel();
    }
    
    /**
     * Sets up basic window properties.
     */
    private void initializeWindow() {
        setTitle("Spectrometer Control Software");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
    }

    // ==================== MENU BAR CREATION ====================

    /**
     * Creates the main menu bar with File, Measurement, and View menus.
     */
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        menuBar.add(createFileMenu());
        menuBar.add(createMeasurementMenu());
        menuBar.add(createViewMenu());
        
        setJMenuBar(menuBar);
    }

    /**
     * Creates the File menu with load, save, and exit options.
     */
    private JMenu createFileMenu() {
        JMenu fileMenu = new JMenu("File");
        
        JMenuItem loadItem = new JMenuItem("Load Measurement");
        JMenuItem saveItem = new JMenuItem("Save Measurement");
        JMenuItem exitItem = new JMenuItem("Exit");
        
        loadItem.addActionListener(e -> loadMeasurementFromFile());
        saveItem.addActionListener(e -> saveMeasurementToFile());
        exitItem.addActionListener(e -> System.exit(0));
        
        fileMenu.add(loadItem);
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        return fileMenu;
    }

    /**
     * Creates the Measurement menu with connect, configure, and measure options.
     */
    private JMenu createMeasurementMenu() {
        JMenu measurementMenu = new JMenu("Measurement");
        
        JMenuItem connectItem = new JMenuItem("Connect");
        JMenuItem configureItem = new JMenuItem("Configure");
        JMenuItem measureItem = new JMenuItem("Measure");
        
        // Initially disable configure and measure until connected
        configureItem.setEnabled(false);
        measureItem.setEnabled(false);
        
        connectItem.addActionListener(e -> connectToSpectrometer(configureItem, measureItem));
        configureItem.addActionListener(e -> configureSpectrometer());
        measureItem.addActionListener(e -> performMeasurement());
        
        measurementMenu.add(connectItem);
        measurementMenu.addSeparator();
        measurementMenu.add(configureItem);
        measurementMenu.add(measureItem);
        
        return measurementMenu;
    }

    /**
     * Creates the View menu with spectrum and absorption visualization options.
     */
    private JMenu createViewMenu() {
        JMenu viewMenu = new JMenu("View");
        
        JMenuItem spectrumItem = new JMenuItem("Spectrum");
        JMenuItem absorptionItem = new JMenuItem("Absorption");
        
        spectrumItem.addActionListener(e -> showSpectrumPlot());
        absorptionItem.addActionListener(e -> showAbsorptionPlot());
        
        viewMenu.add(spectrumItem);
        viewMenu.add(absorptionItem);
        
        return viewMenu;
    }

    // ==================== LEFT PANEL ====================

    /**
     * Creates the left panel containing the list of measurements.
     */
    private void createLeftPanel() {
        measurementListModel = new DefaultListModel<>();
        measurementList = new JList<>(measurementListModel);
        measurementList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        setupKeyboardShortcuts();
        setupMouseListeners();
        
        JScrollPane scrollPane = new JScrollPane(measurementList);
        scrollPane.setPreferredSize(new Dimension(LEFT_PANEL_WIDTH, 0));
        
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Measurements"));
        leftPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(leftPanel, BorderLayout.WEST);
    }
    
    /**
     * Sets up keyboard shortcuts for the measurement list.
     */
    private void setupKeyboardShortcuts() {
        InputMap im = measurementList.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = measurementList.getActionMap();
        
        im.put(KeyStroke.getKeyStroke("DELETE"), "deleteMeasurement");
        am.put("deleteMeasurement", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                deleteSelectedMeasurement();
            }
        });
    }
    
    /**
     * Sets up mouse listeners for double-click actions.
     */
    private void setupMouseListeners() {
        measurementList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showMeasurementDetails();
                }
            }
        });
    }

    // ==================== CENTER PANEL ====================

    /**
     * Creates the center panel for visualization.
     */
    private void createCenterPanel() {
        centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("Visualization"));
        
        JLabel placeholder = new JLabel("No plot yet", SwingConstants.CENTER);
        centerPanel.add(placeholder, BorderLayout.CENTER);
        
        add(centerPanel, BorderLayout.CENTER);
    }

    // ==================== FILE OPERATIONS ====================

    /**
     * Loads a measurement set from a file.
     */
    private void loadMeasurementFromFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Load Measurement");
        
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        java.io.File file = chooser.getSelectedFile();
        try {
            MeasurementSet set = MeasurementSet.loadFromFile(file.getAbsolutePath());
            
            if (!validateMeasurementSet(set)) {
                return;
            }

            String name = set.getName();
            if (name == null || name.trim().isEmpty()) {
                name = file.getName();
            }

            measurementSets.put(name, set);
            addMeasurement(name);
            
            showInfoDialog("Load successful", "Loaded measurement:\n" + name);
            
        } catch (Exception ex) {
            handleError("Failed to load file", ex);
        }
    }
    
    /**
     * Validates a loaded measurement set.
     */
    private boolean validateMeasurementSet(MeasurementSet set) {
        if (set.getMeasurements().isEmpty()) {
            showErrorDialog("Invalid file", "Selected file does not contain any measurement data.");
            return false;
        }
        
        // Validate data format (expect 6 channels for AS726x)
        for (double[] measurement : set.getMeasurements()) {
            if (measurement.length != 6) {
                showErrorDialog("Invalid file", 
                    "File has wrong data format (expected 6 channels).");
                return false;
            }
        }
        
        return true;
    }

    /**
     * Saves the selected measurement to a file.
     */
    private void saveMeasurementToFile() {
        String selectedName = getSelectedMeasurement();
        if (selectedName == null) {
            showErrorDialog("Error", "No measurement selected.");
            return;
        }

        MeasurementSet set = measurementSets.get(selectedName);
        if (set == null) {
            showErrorDialog("Error", "No data available for the selected measurement.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Measurement");
        chooser.setSelectedFile(new java.io.File(selectedName + ".txt"));

        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            java.io.File file = chooser.getSelectedFile();
            try {
                set.saveToFile(file.getAbsolutePath());
                showInfoDialog("Save successful", "Saved to:\n" + file.getAbsolutePath());
            } catch (Exception ex) {
                handleError("Failed to save file", ex);
            }
        }
    }

    // ==================== SPECTROMETER OPERATIONS ====================

    /**
     * Connects to the spectrometer.
     */
    private void connectToSpectrometer(JMenuItem configureItem, JMenuItem measureItem) {
        try {
            spectrometer = new Spectrometer();
            showInfoDialog("Connection successful", 
                "Connected to " + spectrometer.getPortName());
            configureItem.setEnabled(true);
            measureItem.setEnabled(true);
        } catch (Exception ex) {
            spectrometer = null;
            handleError("Connection error", ex);
        }
    }

    /**
     * Opens the configuration dialog for spectrometer settings.
     */
    private void configureSpectrometer() {
        if (!checkConnection()) return;

        Map<String, Object> currentParams = new HashMap<>();
        ConfigureDialog dialog = new ConfigureDialog(this, currentParams);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            Map<String, Object> params = dialog.getParameters();
            applyConfiguration(params);
        }
    }
    
    /**
     * Applies configuration parameters to the spectrometer.
     */
    private void applyConfiguration(Map<String, Object> params) {
        int integrationTime = (int) params.get("int");
        int gain = (int) params.get("gain");
        int avg = (int) params.get("avg");
        int numberOfMeasurements = (int) params.get("count");
        String mode = params.get("mode").toString();
        int lightInt = (int) params.get("light");
        
        spectrometer.configure(
            integrationTime, gain, avg, mode, numberOfMeasurements, lightInt
        );
    }

    /**
     * Performs a measurement with current spectrometer settings.
     */
    private void performMeasurement() {
        if (!checkConnection()) return;

        String baseName = JOptionPane.showInputDialog(
            this, "Enter measurement name:", "New Measurement", JOptionPane.PLAIN_MESSAGE
        );

        if (baseName == null || baseName.trim().isEmpty()) {
            return;
        }

        try {
            spectrometer.measure(baseName.trim());
            MeasurementSet set = spectrometer.getMeasurementSet();
            String fullName = set.getName();
            
            addMeasurement(fullName);
            measurementSets.put(fullName, set);
            
            showInfoDialog("Measurement completed", fullName);
            
        } catch (Exception ex) {
            handleError("Measurement failed", ex);
        }
    }
    
    /**
     * Checks if spectrometer is connected.
     */
    private boolean checkConnection() {
        if (spectrometer == null) {
            showErrorDialog("Error", "Not connected to spectrometer.");
            return false;
        }
        return true;
    }

    // ==================== VISUALIZATION ====================

    /**
     * Shows spectrum plot for selected measurement.
     */
    private void showSpectrumPlot() {
        String name = getSelectedMeasurement();
        if (name == null) {
            showErrorDialog("Error", "No measurement selected.");
            return;
        }

        MeasurementSet set = measurementSets.get(name);
        if (set == null) {
            showErrorDialog("Error", "No data available for the selected measurement.");
            return;
        }

        SpectrumOptionsDialog dialog = new SpectrumOptionsDialog(this);
        dialog.setVisible(true);
        
        if (!dialog.isConfirmed()) {
            return;
        }

        Visualizer vis = new Visualizer(set);
        vis.setPlotType(dialog.getPlotType());
        vis.setNormalize(dialog.isNormalize());
        vis.setShowErrorBars(dialog.isShowErrorBars());
        vis.useWavelengthAxis(dialog.isUseWavelength());

        updatePlot(vis, dialog.getPlotType());
    }

    /**
     * Shows absorption plot comparing reference and sample.
     */
    private void showAbsorptionPlot() {
        List<String> names = getMeasurementNames();
        if (names.size() < 2) {
            showErrorDialog("Error", "Need at least two measurements (reference and sample).");
            return;
        }

        AbsorptionOptionsDialog dialog = new AbsorptionOptionsDialog(this, names);
        dialog.setVisible(true);
        
        if (!dialog.isConfirmed()) {
            return;
        }

        String refName = dialog.getReferenceName();
        String sampleName = dialog.getSampleName();
        
        if (!validateAbsorptionSelection(refName, sampleName)) {
            return;
        }

        MeasurementSet refSet = measurementSets.get(refName);
        MeasurementSet sampleSet = measurementSets.get(sampleName);

        Visualizer vis = new Visualizer(refSet);
        vis.useWavelengthAxis(dialog.isUseWavelength());

        XYChart chart = vis.createAbsorptionChart(refSet, sampleSet);
        displayChart(chart);
    }
    
    /**
     * Validates absorption plot selections.
     */
    private boolean validateAbsorptionSelection(String refName, String sampleName) {
        if (refName == null || sampleName == null || refName.equals(sampleName)) {
            showErrorDialog("Error", "Please choose two different measurements.");
            return false;
        }
        
        if (!measurementSets.containsKey(refName) || !measurementSets.containsKey(sampleName)) {
            showErrorDialog("Error", "Selected measurements are not available.");
            return false;
        }
        
        return true;
    }

    /**
     * Updates the center panel with a new plot.
     */
    private void updatePlot(Visualizer vis, String plotType) {
        centerPanel.removeAll();
        
        if ("bar".equals(plotType)) {
            CategoryChart chart = vis.createBarChart();
            centerPanel.add(new XChartPanel<>(chart), BorderLayout.CENTER);
        } else {
            XYChart chart = vis.createCurveChart();
            centerPanel.add(new XChartPanel<>(chart), BorderLayout.CENTER);
        }
        
        centerPanel.revalidate();
        centerPanel.repaint();
    }
    
    /**
     * Displays a chart in the center panel.
     */
    private void displayChart(XYChart chart) {
        centerPanel.removeAll();
        centerPanel.add(new XChartPanel<>(chart), BorderLayout.CENTER);
        centerPanel.revalidate();
        centerPanel.repaint();
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Shows measurement details in a dialog.
     */
    private void showMeasurementDetails() {
        String name = measurementList.getSelectedValue();
        if (name == null) return;

        MeasurementSet set = measurementSets.get(name);
        if (set == null) {
            showErrorDialog("Error", "No data stored for this measurement.");
            return;
        }

        // Create text area with monospaced font for better formatting
        JTextArea area = new JTextArea(set.toString(), 25, 50);
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(600, 400));

        JOptionPane.showMessageDialog(
            this, scroll, "Measurement Details: " + name, 
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     * Deletes the selected measurement.
     */
    private void deleteSelectedMeasurement() {
        String name = getSelectedMeasurement();
        if (name == null) return;

        int index = measurementList.getSelectedIndex();
        if (index >= 0) {
            measurementListModel.remove(index);
        }
        
        measurementSets.remove(name);
    }

    /**
     * Adds a measurement to the list.
     */
    public void addMeasurement(String name) {
        measurementListModel.addElement(name);
    }

    /**
     * Gets the name of the selected measurement.
     */
    public String getSelectedMeasurement() {
        return measurementList.getSelectedValue();
    }
    
    /**
     * Gets list of all measurement names.
     */
    private List<String> getMeasurementNames() {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < measurementListModel.size(); i++) {
            names.add(measurementListModel.getElementAt(i));
        }
        return names;
    }

    // ==================== DIALOG HELPERS ====================

    /**
     * Shows an error dialog.
     */
    private void showErrorDialog(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Shows an information dialog.
     */
    private void showInfoDialog(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Handles an exception with error dialog and stack trace.
     */
    private void handleError(String message, Exception ex) {
        ex.printStackTrace();
        showErrorDialog("Error", message + ":\n" + ex.getMessage());
    }
}