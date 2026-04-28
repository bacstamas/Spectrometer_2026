package ui;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.HashMap;

import core.*;
import ui.menu.*;
import ui.panel.*;

public class MainWindow extends JFrame {
    
    // ==================== CONSTANTS ====================
    
    private static final int WINDOW_WIDTH = 1000;
    private static final int WINDOW_HEIGHT = 600;
    public static final int LEFT_PANEL_WIDTH = 250;
    
    // ==================== UI COMPONENTS ====================
    
    public DefaultListModel<String> measurementListModel;
    public JList<String> measurementList;

    public CreateMenuBar menuBar;
    public CreateLeftPanel leftPanel;
    public CreateCenterPanel centerPanel;
    
    // ==================== DATA ====================
    
    public Spectrometer spectrometer;
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
        add(leftPanel,BorderLayout.WEST);

        this.centerPanel = new CreateCenterPanel(this);
        add(centerPanel,BorderLayout.CENTER);
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