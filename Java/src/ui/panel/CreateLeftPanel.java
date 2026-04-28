package ui.panel;

import javax.swing.*;
import java.awt.*;
import core.*;
import ui.dialog.*;
import ui.MainWindow;

public class CreateLeftPanel extends JPanel {

    public MainWindow mainWindow;

	public CreateLeftPanel(MainWindow mainWindow){
		super(new BorderLayout());
		this.mainWindow = mainWindow;

		mainWindow.measurementListModel = new DefaultListModel<>();
    	mainWindow.measurementList = new JList<>(mainWindow.measurementListModel);

		mainWindow.measurementList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        setupKeyboardShortcuts();
        setupMouseListeners();
        
        JScrollPane scrollPane = new JScrollPane(mainWindow.measurementList);
        scrollPane.setPreferredSize(new Dimension(mainWindow.LEFT_PANEL_WIDTH, 0));
        
        setBorder(BorderFactory.createTitledBorder("Measurements"));
        add(scrollPane, BorderLayout.CENTER);
        
    }

    /**
     * Sets up keyboard shortcuts for the measurement list.
     */
    private void setupKeyboardShortcuts() {
        InputMap im = mainWindow.measurementList.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = mainWindow.measurementList.getActionMap();
        
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
        mainWindow.measurementList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showMeasurementDetails();
                }
            }
        });
    }

    /**
     * Shows measurement details in a dialog.
     */
    private void showMeasurementDetails() {
        String name = mainWindow.measurementList.getSelectedValue();
        if (name == null) return;

        MeasurementSet set = mainWindow.measurementSets.get(name);
        if (set == null) {
            DialogUtils.showErrorDialog(mainWindow, "Error", "No data stored for this measurement.");
            return;
        }

        // Create text area with monospaced font for better formatting
        JTextArea area = new JTextArea(set.toString(), 25, 50);
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(600, 400));

        JOptionPane.showMessageDialog(
            mainWindow, scroll, "Measurement Details: " + name, 
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     * Deletes the selected measurement.
     */
	private void deleteSelectedMeasurement() {
		String name = mainWindow.measurementList.getSelectedValue();
		if (name == null) return;

		int index = mainWindow.measurementList.getSelectedIndex();
		if (index >= 0) {
			mainWindow.measurementListModel.remove(index);
		}

		mainWindow.measurementSets.remove(name);
	}
}
