package ui.menu;

import java.util.List;
import java.util.ArrayList;

import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XChartPanel;
import javax.swing.*;
import java.awt.*;
import core.*;
import ui.dialog.*;
import ui.MainWindow;

/**
 * View menu providing spectrum and absorption plot visualization.
 * Allows customization of plot appearance including bar/curve type,
 * normalization, error bars, and axis selection.
 * 
 * @author Spectrometer Control Software
 * @version 1.0
 */
public class CreateViewMenu extends JMenu {
    
    /** Reference to the parent menu bar. */
    private CreateMenuBar createMenuBar;
    
    /** Reference to the main window. */
    private MainWindow mainWindow;

    /**
     * Constructs the View menu with its menu items.
     * 
     * @param createMenuBar the parent menu bar
     */
    public CreateViewMenu(CreateMenuBar createMenuBar) {
        super("View");
        this.createMenuBar = createMenuBar;
        this.mainWindow = createMenuBar.mainWindow;

        JMenuItem spectrumItem = new JMenuItem("Spectrum");
        JMenuItem absorptionItem = new JMenuItem("Absorption");
        
        spectrumItem.addActionListener(e -> showSpectrumPlot());
        absorptionItem.addActionListener(e -> showAbsorptionPlot());
        
        add(spectrumItem);
        add(absorptionItem);
    }

    /**
     * Shows spectrum plot for selected measurement.
     * Displays a configuration dialog before generating the plot.
     */
    private void showSpectrumPlot() {
        String name = mainWindow.measurementList.getSelectedValue();
        if (name == null) {
            DialogUtils.showErrorDialog(mainWindow, "Error", "No measurement selected.");
            return;
        }

        MeasurementSet set = mainWindow.measurementSets.get(name);
        if (set == null) {
            DialogUtils.showErrorDialog(mainWindow, "Error", "No data available for the selected measurement.");
            return;
        }

        SpectrumOptionsDialog dialog = new SpectrumOptionsDialog(mainWindow);
        dialog.setVisible(true);
        
        if (!dialog.isConfirmed()) {
            return;
        }

        Visualizer vis = new Visualizer(set);
        vis.setPlotType(dialog.getPlotType());
        vis.setNormalize(dialog.isNormalize());
        vis.setShowErrorBars(dialog.isShowErrorBars());
        vis.useWavelengthAxis(dialog.isUseWavelength());

        if ("bar".equals(dialog.getPlotType())) {
            CategoryChart chart = vis.createBarChart();
            displayChart(chart);
        } else {
            XYChart chart = vis.createCurveChart();
            displayChart(chart);
        }
    }

    /**
     * Shows absorption plot comparing reference and sample measurements.
     * Requires at least two measurements to be available.
     */
    private void showAbsorptionPlot() {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < mainWindow.measurementListModel.size(); i++) {
            names.add(mainWindow.measurementListModel.getElementAt(i));
        }

        if (names.size() < 2) {
            DialogUtils.showErrorDialog(mainWindow, "Error", "Need at least two measurements (reference and sample).");
            return;
        }

        AbsorptionOptionsDialog dialog = new AbsorptionOptionsDialog(mainWindow, names);
        dialog.setVisible(true);
        
        if (!dialog.isConfirmed()) {
            return;
        }

        String refName = dialog.getReferenceName();
        String sampleName = dialog.getSampleName();
        
        if (!validateAbsorptionSelection(refName, sampleName)) {
            return;
        }

        MeasurementSet refSet = mainWindow.measurementSets.get(refName);
        MeasurementSet sampleSet = mainWindow.measurementSets.get(sampleName);

        Visualizer vis = new Visualizer(refSet);
        vis.useWavelengthAxis(dialog.isUseWavelength());

        XYChart chart = vis.createAbsorptionChart(refSet, sampleSet);
        displayChart(chart);
    }
    
    /**
     * Validates absorption plot selections.
     * 
     * @param refName the selected reference measurement name
     * @param sampleName the selected sample measurement name
     * @return true if selections are valid, false otherwise
     */
    private boolean validateAbsorptionSelection(String refName, String sampleName) {
        if (refName == null || sampleName == null || refName.equals(sampleName)) {
            DialogUtils.showErrorDialog(mainWindow, "Error", "Please choose two different measurements.");
            return false;
        }
        
        if (!mainWindow.measurementSets.containsKey(refName) || !mainWindow.measurementSets.containsKey(sampleName)) {
            DialogUtils.showErrorDialog(mainWindow, "Error", "Selected measurements are not available.");
            return false;
        }
        
        return true;
    }
    
    /**
     * Displays an XY chart in the center panel.
     * 
     * @param chart the XYChart to display
     */
    private void displayChart(XYChart chart) {
        mainWindow.centerPanel.removeAll();
        mainWindow.centerPanel.add(new XChartPanel<>(chart), BorderLayout.CENTER);
        mainWindow.centerPanel.revalidate();
        mainWindow.centerPanel.repaint();
    }

    /**
     * Displays a category (bar) chart in the center panel.
     * 
     * @param chart the CategoryChart to display
     */
    private void displayChart(CategoryChart chart) {
        mainWindow.centerPanel.removeAll();
        mainWindow.centerPanel.add(new XChartPanel<>(chart), BorderLayout.CENTER);
        mainWindow.centerPanel.revalidate();
        mainWindow.centerPanel.repaint();
    }
}