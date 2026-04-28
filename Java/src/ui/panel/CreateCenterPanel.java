package ui.panel;

import javax.swing.*;
import java.awt.*;
import core.*;
import ui.dialog.*;
import ui.MainWindow;

public class CreateCenterPanel extends JPanel {
    public MainWindow mainWindow;

	public CreateCenterPanel(MainWindow mainWindow){
		super(new BorderLayout());
		this.mainWindow = mainWindow;

		setBorder(BorderFactory.createTitledBorder("Visualization"));
        
        JLabel placeholder = new JLabel("No plot yet", SwingConstants.CENTER);
        add(placeholder, BorderLayout.CENTER);
        
    }
}
