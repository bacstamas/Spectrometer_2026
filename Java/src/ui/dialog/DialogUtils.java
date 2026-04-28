package ui.dialog;

import javax.swing.*;
import java.awt.*;

public class DialogUtils {
    public static void showErrorDialog(JFrame frame, String title, String message) {
        JOptionPane.showMessageDialog(frame, message, title, JOptionPane.ERROR_MESSAGE);
    }
    
    public static void showInfoDialog(JFrame frame, String title, String message) {
        JOptionPane.showMessageDialog(frame, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void handleError(JFrame frame, String message, Exception ex) {
		ex.printStackTrace();
        showErrorDialog(frame, "Error", message + ":\n" + ex.getMessage());
	}
}
