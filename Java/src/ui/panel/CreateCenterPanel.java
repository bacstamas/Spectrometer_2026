package ui.panel;

import javax.swing.*;
import java.awt.*;
import core.*;
import ui.dialog.*;
import ui.MainWindow;

/**
 * Center panel of the main window, responsible for displaying
 * visualizations such as spectrum plots and absorption charts.
 * Initially shows a placeholder label until a plot is generated.
 * 
 * @author Spectrometer Control Software
 * @version 1.0
 */
public class CreateCenterPanel extends JPanel {
    
    /** Reference to the main window for accessing shared data. */
    public MainWindow mainWindow;

    /**
     * Constructs the center panel with a placeholder.
     * 
     * @param mainWindow the parent MainWindow instance
     */
    public CreateCenterPanel(MainWindow mainWindow) {
        super(new BorderLayout());
        this.mainWindow = mainWindow;

        setBorder(BorderFactory.createTitledBorder("Visualization"));
        
        JLabel placeholder = new JLabel("No plot yet", SwingConstants.CENTER);
        add(placeholder, BorderLayout.CENTER);
    }
}