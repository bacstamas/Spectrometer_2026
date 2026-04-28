package ui.dialog;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog for configuring spectrum plot options.
 * Allows selection of plot type, normalization, error bars, and axis type.
 */
public class SpectrumOptionsDialog extends JDialog {

    // ==================== UI COMPONENTS ====================
    
    private JRadioButton barButton;
    private JRadioButton curveButton;
    private JCheckBox normalizeBox;
    private JCheckBox errorBarsBox;
    private JRadioButton wavelengthButton;
    private JRadioButton frequencyButton;
    
    // ==================== STATE ====================
    
    private boolean confirmed = false;
    
    // ==================== CONSTANTS ====================
    
    private static final int DIALOG_WIDTH = 300;
    private static final int DIALOG_HEIGHT = 300;
    private static final int PADDING = 8;
    private static final int GRID_ROWS = 4;
    private static final int GRID_COLS = 1;

    /**
     * Constructs the spectrum options dialog.
     * @param parent Parent frame
     */
    public SpectrumOptionsDialog(JFrame parent) {
        super(parent, "Spectrum Options", true);
        initializeDialog();
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(createOptionsPanel(), BorderLayout.CENTER);
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    /**
     * Sets up dialog properties.
     */
    private void initializeDialog() {
        setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        setResizable(false);
    }

    // ==================== PANEL CREATION ====================

    /**
     * Creates the panel with all plot options.
     */
    private JPanel createOptionsPanel() {
        JPanel panel = new JPanel(new GridLayout(GRID_ROWS, GRID_COLS, 4, 4));
        panel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));

        panel.add(createPlotTypePanel());
        panel.add(createNormalizePanel());
        panel.add(createErrorBarsPanel());
        panel.add(createAxisPanel());

        return panel;
    }

    /**
     * Creates panel for plot type selection (bar/curve).
     */
    private JPanel createPlotTypePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Plot Type"));
        
        barButton = new JRadioButton("Bar");
        curveButton = new JRadioButton("Curve", true);
        
        ButtonGroup group = new ButtonGroup();
        group.add(barButton);
        group.add(curveButton);
        
        panel.add(barButton);
        panel.add(curveButton);
        
        return panel;
    }

    /**
     * Creates panel for normalization option.
     */
    private JPanel createNormalizePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        normalizeBox = new JCheckBox("Normalize to Maximum", false);
        panel.add(normalizeBox);
        return panel;
    }

    /**
     * Creates panel for error bars option.
     */
    private JPanel createErrorBarsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        errorBarsBox = new JCheckBox("Show Error Bars", true);
        panel.add(errorBarsBox);
        return panel;
    }

    /**
     * Creates panel for axis type selection (wavelength/frequency).
     */
    private JPanel createAxisPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("X-Axis"));
        
        wavelengthButton = new JRadioButton("Wavelength (nm)", true);
        frequencyButton = new JRadioButton("Frequency (THz)");
        
        ButtonGroup group = new ButtonGroup();
        group.add(wavelengthButton);
        group.add(frequencyButton);
        
        panel.add(wavelengthButton);
        panel.add(frequencyButton);
        
        return panel;
    }

    // ==================== BUTTON PANEL ====================

    /**
     * Creates the button panel with OK and Cancel.
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        
        okButton.addActionListener(e -> {
            confirmed = true;
            setVisible(false);
        });
        
        cancelButton.addActionListener(e -> {
            confirmed = false;
            setVisible(false);
        });
        
        panel.add(okButton);
        panel.add(cancelButton);
        
        return panel;
    }

    // ==================== GETTERS ====================

    /**
     * @return true if user confirmed (clicked OK)
     */
    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * @return Selected plot type ("bar" or "curve")
     */
    public String getPlotType() {
        return barButton.isSelected() ? "bar" : "curve";
    }

    /**
     * @return true if normalization should be applied
     */
    public boolean isNormalize() {
        return normalizeBox.isSelected();
    }

    /**
     * @return true if error bars should be shown
     */
    public boolean isShowErrorBars() {
        return errorBarsBox.isSelected();
    }

    /**
     * @return true if wavelength axis should be used (false for frequency)
     */
    public boolean isUseWavelength() {
        return wavelengthButton.isSelected();
    }
}