package ui.menu;

import javax.swing.*;
import java.awt.*;
import ui.MainWindow;


public class CreateMenuBar extends JMenuBar{
	public MainWindow mainWindow;

	public CreateMenuBar(MainWindow mainWindow){
		this.mainWindow = mainWindow;
		CreateFileMenu fileMenu = new CreateFileMenu(this);
		CreateMeasurementMenu measurementMenu = new CreateMeasurementMenu(this);
		CreateViewMenu viewMenu = new CreateViewMenu(this);
		
		add(fileMenu);
        add(measurementMenu);
        add(viewMenu);
	}

}