package ui.menu;

import java.util.Map;
import java.util.HashMap;

import javax.swing.*;
import java.awt.*;
import core.*;
import ui.dialog.*;
import ui.MainWindow;

/**
 * Measurement menu providing spectrometer connection, configuration,
 * and data acquisition functionality.
 * Uses SwingWorker for background tasks to keep the UI responsive.
 * 
 * @author Spectrometer Control Software
 * @version 1.0
 */
public class CreateMeasurementMenu extends JMenu {
    
    /** Reference to the parent menu bar. */
    private CreateMenuBar createMenuBar;
    
    /** Reference to the main window. */
    private MainWindow mainWindow;
    
    /** Spectrometer controller instance. */
    private Spectrometer spectrometer;

    /** Menu item for configuration (enabled only when connected). */
    private JMenuItem configureItem;
    
    /** Menu item for measurement (enabled only when connected). */
    private JMenuItem measureItem;

    /**
     * Constructs the Measurement menu with its menu items.
     * 
     * @param createMenuBar the parent menu bar
     */
    public CreateMeasurementMenu(CreateMenuBar createMenuBar) {
        super("Measurement");
        this.createMenuBar = createMenuBar;
        mainWindow = createMenuBar.mainWindow;
        spectrometer = mainWindow.spectrometer;

        JMenuItem connectItem = new JMenuItem("Connect");
        configureItem = new JMenuItem("Configure");
        measureItem = new JMenuItem("Measure");
        
        // Initially disable configure and measure until connected
        configureItem.setEnabled(false);
        measureItem.setEnabled(false);
        
        connectItem.addActionListener(e -> connectToSpectrometer(configureItem, measureItem));
        configureItem.addActionListener(e -> configureSpectrometer());
        measureItem.addActionListener(e -> performMeasurement());
        
        add(connectItem);
        addSeparator();
        add(configureItem);
        add(measureItem);
    }

    /**
     * Connects to the spectrometer in a background thread.
     * 
     * @param configureItem menu item to enable on successful connection
     * @param measureItem menu item to enable on successful connection
     */
    private void connectToSpectrometer(JMenuItem configureItem, JMenuItem measureItem) {
        // 1. Set wait cursor
        mainWindow.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<Spectrometer, Void> worker = new SwingWorker<>() {
            @Override
            protected Spectrometer doInBackground() throws Exception {
                return new Spectrometer(() -> {
                    // This runs when disconnected
                    SwingUtilities.invokeLater(() -> handleDisconnect());
                });
            }

            @Override
            protected void done() {
                try {
                    // 2. Retrieve the created spectrometer
                    spectrometer = get(); 
                    mainWindow.setCursor(Cursor.getDefaultCursor());
                    DialogUtils.showInfoDialog(mainWindow, "Connection successful", 
                        "Connected to " + spectrometer.getPortName());
                    
                    configureItem.setEnabled(true);
                    measureItem.setEnabled(true);
                } catch (Exception ex) {
                    spectrometer = null;
                    mainWindow.setCursor(Cursor.getDefaultCursor());
                    DialogUtils.handleError(mainWindow, "Connection error", ex);
                }
            }
        };

        worker.execute();
    }

    /**
     * Handles disconnection events from the spectrometer.
     * Disables configuration and measurement menu items.
     */
    private void handleDisconnect() {
        spectrometer = null;

        DialogUtils.showErrorDialog(
            mainWindow,
            "Connection lost",
            "The spectrometer was disconnected."
        );

        configureItem.setEnabled(false);
        measureItem.setEnabled(false);
    }

    /**
     * Opens the configuration dialog for spectrometer settings.
     */
    private void configureSpectrometer() {
        if (!checkConnection()) return;

        Map<String, Object> currentParams = new HashMap<>();
        ConfigureDialog dialog = new ConfigureDialog(mainWindow, currentParams);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            Map<String, Object> params = dialog.getParameters();
            applyConfiguration(params);
        }
    }

    /**
     * Applies configuration parameters to the spectrometer.
     * 
     * @param params map containing int, gain, avg, count, mode, light values
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
     * Prompts for a measurement name and runs the acquisition in a background thread.
     */
    private void performMeasurement() {
        if (!checkConnection()) return;

        String baseName = JOptionPane.showInputDialog(
            mainWindow, "Enter measurement name:", "New Measurement", JOptionPane.PLAIN_MESSAGE
        );

        if (baseName == null || baseName.trim().isEmpty()) return;

        // 1. Set the wait cursor on the MainWindow
        mainWindow.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        // 2. Perform the task in a background thread
        SwingWorker<MeasurementSet, Void> worker = new SwingWorker<>() {
            @Override
            protected MeasurementSet doInBackground() throws Exception {
                // This runs on a separate thread, so the UI stays responsive
                spectrometer.measure(baseName.trim());
                return spectrometer.getMeasurementSet();
            }

            @Override
            protected void done() {
                // This runs back on the Event Dispatch Thread when finished
                try {
                    MeasurementSet set = get(); // Retrieves the result from doInBackground
                    String fullName = set.getName();
                    
                    mainWindow.measurementListModel.addElement(fullName);
                    mainWindow.measurementSets.put(fullName, set);
                    mainWindow.setCursor(Cursor.getDefaultCursor());

                    DialogUtils.showInfoDialog(mainWindow, "Measurement completed", fullName);
                } catch (Exception ex) {
                    mainWindow.setCursor(Cursor.getDefaultCursor());
                    DialogUtils.handleError(mainWindow, "Measurement failed", ex);
                }
            }
        };

        worker.execute(); // Start the background thread
    }
    
    /**
     * Checks if spectrometer is connected.
     * 
     * @return true if connected, false otherwise (displays error dialog)
     */
    private boolean checkConnection() {
        if (spectrometer == null) {
            DialogUtils.showErrorDialog(mainWindow, "Error", "Not connected to spectrometer.");
            return false;
        }
        return true;
    }
}