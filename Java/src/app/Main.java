package app;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import ui.MainWindow;
import javax.swing.*;
import java.awt.*;

/**
 * Main application entry point.
 * Launches the spectrometer GUI.
 * 
 * @author Spectrometer Control Software
 * @version 1.0
 */
public class Main {

    /**
     * The main method that serves as the entry point for the application.
     * 
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        // Launch GUI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Set system look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Fall back to default look and feel
            }
            
            // Create and show main window
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}