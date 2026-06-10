package ui;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.HashMap;

import core.*;
import ui.menu.*;
import ui.panel.*;

/**
 * Main application window for the Spectrometer Control Software.
 * Serves as the primary container for all UI components including
 * the menu bar, left panel (measurement list), and center panel (visualization).
 * 
 * @author Spectrometer Control Software
 * @version 1.0
 */
public class MainWindow extends JFrame {
    
    // ==================== CONSTANTS ====================
    
    /** Default window width in pixels. */
    private static final int WINDOW_WIDTH = 1000;
    
    /** Default window height in pixels. */
    private static final int WINDOW_HEIGHT = 600;
    
    /** Width of the left panel (measurement list) in pixels. */
    public static final int LEFT_PANEL_WIDTH = 250;
    
    // ==================== UI COMPONENTS ====================
    
    /** List model for the measurement list. */
    public DefaultListModel<String> measurementListModel;
    
    /** JList component displaying measurement names. */
    public JList<String> measurementList;

    /** Application menu bar. */
    public CreateMenuBar menuBar;
    
    /** Left panel containing the measurement list. */
    public CreateLeftPanel leftPanel;
    
    /** Center panel containing visualizations. */
    public CreateCenterPanel centerPanel;
    
    // ==================== DATA ====================
    
    /** Spectrometer hardware controller instance. */
    public Spectrometer spectrometer;
    
    /** Map of measurement names to their corresponding MeasurementSet objects. */
    public Map<String, MeasurementSet> measurementSets = new HashMap<>();
    
    // ==================== CONSTRUCTOR ====================

    /**
     * Constructs the main window and initializes all UI components.
     */
    public MainWindow() {
        initializeWindow();

        this.menuBar = new CreateMenuBar(this);
        setJMenuBar(menuBar);

        this.leftPanel = new CreateLeftPanel(this);
        add(leftPanel, BorderLayout.WEST);

        this.centerPanel = new CreateCenterPanel(this);
        add(centerPanel, BorderLayout.CENTER);
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
}