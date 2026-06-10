package ui.dialog;

import javax.swing.*;
import java.awt.*;

/**
 * Utility class for displaying common dialog boxes.
 * Provides convenience methods for error, info, and exception dialogs.
 * 
 * @author Spectrometer Control Software
 * @version 1.0
 */
public class DialogUtils {
    
    /**
     * Displays an error dialog with the specified title and message.
     * 
     * @param frame the parent frame (can be null)
     * @param title the dialog title
     * @param message the error message to display
     */
    public static void showErrorDialog(JFrame frame, String title, String message) {
        JOptionPane.showMessageDialog(frame, message, title, JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Displays an information dialog with the specified title and message.
     * 
     * @param frame the parent frame (can be null)
     * @param title the dialog title
     * @param message the information message to display
     */
    public static void showInfoDialog(JFrame frame, String title, String message) {
        JOptionPane.showMessageDialog(frame, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Handles an exception by printing the stack trace and showing an error dialog.
     * 
     * @param frame the parent frame (can be null)
     * @param message the error description message
     * @param ex the exception that occurred
     */
    public static void handleError(JFrame frame, String message, Exception ex) {
        ex.printStackTrace();
        showErrorDialog(frame, "Error", message + ":\n" + ex.getMessage());
    }
}