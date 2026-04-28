import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import ui.MainWindow;

/**
 * Main application entry point.
 * Launches the spectrometer GUI.
 */
public class Main {

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