package ui.menu;

import javax.swing.*;
import java.awt.*;
import ui.MainWindow;

/**
 * Main menu bar for the application.
 * Contains the File, Measurement, and View menus.
 * 
 * @author Spectrometer Control Software
 * @version 1.0
 */
public class CreateMenuBar extends JMenuBar {
    
    /** Reference to the main window. */
    public MainWindow mainWindow;

    /**
     * Constructs the menu bar with all sub-menus.
     * 
     * @param mainWindow the parent MainWindow instance
     */
    public CreateMenuBar(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        CreateFileMenu fileMenu = new CreateFileMenu(this);
        CreateMeasurementMenu measurementMenu = new CreateMeasurementMenu(this);
        CreateViewMenu viewMenu = new CreateViewMenu(this);
        
        add(fileMenu);
        add(measurementMenu);
        add(viewMenu);
    }
}