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

	public CreateMeasurementMenu(CreateMenuBar createMenuBar) {
		super("Measurement");
		this.createMenuBar = createMenuBar;
		mainWindow = createMenuBar.mainWindow;
		spectrometer = mainWindow.spectrometer;

		JMenuItem connectItem = new JMenuItem("Connect");
        JMenuItem configureItem = new JMenuItem("Configure");
        JMenuItem measureItem = new JMenuItem("Measure");
        
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
        try {
            spectrometer = new Spectrometer();
            DialogUtils.showInfoDialog(mainWindow, "Connection successful", 
                "Connected to " + spectrometer.getPortName());
            configureItem.setEnabled(true);
            measureItem.setEnabled(true);
        } catch (Exception ex) {
            spectrometer = null;
            DialogUtils.handleError(mainWindow, "Connection error", ex);
        }
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

        if (baseName == null || baseName.trim().isEmpty()) {
            return;
        }

        try {
            spectrometer.measure(baseName.trim());
            MeasurementSet set = spectrometer.getMeasurementSet();
            String fullName = set.getName();
            
            mainWindow.measurementListModel.addElement(fullName);
            mainWindow.measurementSets.put(fullName, set);
            
            DialogUtils.showInfoDialog(mainWindow,"Measurement completed", fullName);
            
        } catch (Exception ex) {
            DialogUtils.handleError(mainWindow,"Measurement failed", ex);
        }
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
