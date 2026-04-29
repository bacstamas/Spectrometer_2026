package ui.menu;

import java.util.Map;
import java.util.HashMap;

import javax.swing.*;
import java.awt.*;
import core.*;
import ui.dialog.*;
import ui.MainWindow;

public class CreateMeasurementMenu extends JMenu {
	private CreateMenuBar createMenuBar;
	private MainWindow mainWindow;
	private Spectrometer spectrometer;

    private JMenuItem configureItem;
    private JMenuItem measureItem;

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
     * Connects to the spectrometer.
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
     */
    private boolean checkConnection() {
        if (spectrometer == null) {
            DialogUtils.showErrorDialog(mainWindow, "Error", "Not connected to spectrometer.");
            return false;
        }
        return true;
    }
}
