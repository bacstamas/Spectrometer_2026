package ui.menu;

import javax.swing.*;
import java.awt.*;
import core.*;
import ui.dialog.*;
import ui.MainWindow;

public class CreateFileMenu extends JMenu {
	private CreateMenuBar createMenuBar;
	private MainWindow mainWindow;

	public CreateFileMenu(CreateMenuBar createMenuBar) {
		super("File");
		this.createMenuBar = createMenuBar;
		this.mainWindow = createMenuBar.mainWindow;

		JMenuItem loadItem = new JMenuItem("Load Measurement");
        JMenuItem saveItem = new JMenuItem("Save Measurement");
        JMenuItem exitItem = new JMenuItem("Exit");

        loadItem.addActionListener(e -> loadMeasurementFromFile());
        saveItem.addActionListener(e -> saveMeasurementToFile());
        exitItem.addActionListener(e -> System.exit(0));

        add(loadItem);
        add(saveItem);
        addSeparator();
        add(exitItem);
	}

	    /**
     * Saves the selected measurement to a file.
     */
    private void saveMeasurementToFile() {
        String selectedName = mainWindow.measurementList.getSelectedValue();
        if (selectedName == null) {
            DialogUtils.showErrorDialog(mainWindow,"Error", "No measurement selected.");
            return;
        }

        MeasurementSet set = mainWindow.measurementSets.get(selectedName);
        if (set == null) {
            DialogUtils.showErrorDialog(mainWindow, "Error", "No data available for the selected measurement.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Measurement");
        chooser.setSelectedFile(new java.io.File(selectedName + ".json"));

        int result = chooser.showSaveDialog(mainWindow);
        if (result == JFileChooser.APPROVE_OPTION) {
            java.io.File file = chooser.getSelectedFile();
            try {
                set.saveToFile(file.getAbsolutePath());
                DialogUtils.showInfoDialog(mainWindow, "Save successful", "Saved to:\n" + file.getAbsolutePath());
            } catch (Exception ex) {
                DialogUtils.handleError(mainWindow, "Failed to save file", ex);
            }
        }
    }

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

            mainWindow.measurementSets.put(name, set);
            mainWindow.measurementListModel.addElement(name);
            
            DialogUtils.showInfoDialog(mainWindow,"Load successful", "Loaded measurement:\n" + name);
            
        } catch (Exception ex) {
            DialogUtils.handleError(mainWindow, "Failed to load file", ex);
        }
    }
    
    /**
     * Validates a loaded measurement set.
     */
    
    private boolean validateMeasurementSet(MeasurementSet set) {
        if (set.getMeasurements().isEmpty()) {
            DialogUtils.showErrorDialog(mainWindow, "Invalid file", "Selected file does not contain any measurement data.");
            return false;
        }
        
        // Validate data format (expect 6 channels for AS726x)
        for (double[] measurement : set.getMeasurements()) {
            if (measurement.length != 6) {
                DialogUtils.showErrorDialog(mainWindow, "Invalid file", 
                    "File has wrong data format (expected 6 channels).");
                return false;
            }
        }
        
        return true;
    }
    
}