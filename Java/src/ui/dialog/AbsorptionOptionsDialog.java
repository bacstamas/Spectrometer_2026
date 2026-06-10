package ui.dialog;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Dialog for configuring absorption plot options.
 * Allows selection of reference and sample measurements and axis type.
 * 
 * @author Spectrometer Control Software
 * @version 1.0
 */
public class AbsorptionOptionsDialog extends JDialog {

    // ==================== UI COMPONENTS ====================
    
    /** Combo box for selecting reference measurement. */
    private JComboBox<String> referenceBox;
    
    /** Combo box for selecting sample measurement. */
    private JComboBox<String> sampleBox;
    
    /** Radio button for wavelength axis (nm). */
    private JRadioButton wavelengthButton;
    
    /** Radio button for frequency axis (THz). */
    private JRadioButton frequencyButton;
    
    // ==================== STATE ====================
    
    /** Whether the user confirmed the dialog (clicked OK). */
    private boolean confirmed = false;
    
    // ==================== CONSTANTS ====================
    
    private static final int DIALOG_WIDTH = 350;
    private static final int DIALOG_HEIGHT = 350;
    private static final int PADDING = 8;
    private static final int GRID_ROWS = 3;
    private static final int GRID_COLS = 1;
    private static final int GRID_GAP = 4;

    /**
     * Constructs the absorption options dialog.
     * 
     * @param parent Parent frame
     * @param measurementNames List of available measurement names
     */
    public AbsorptionOptionsDialog(JFrame parent, List<String> measurementNames) {
        super(parent, "Absorption Options", true);
        initializeDialog();
        
        String[] names = measurementNames.toArray(new String[0]);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(createOptionsPanel(names), BorderLayout.CENTER);
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
     * Creates the main options panel.
     * 
     * @param measurementNames array of measurement names for the combo boxes
     * @return configured JPanel
     */
    private JPanel createOptionsPanel(String[] measurementNames) {
        JPanel panel = new JPanel(new GridLayout(GRID_ROWS, GRID_COLS, GRID_GAP, GRID_GAP));
        panel.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));

        panel.add(createReferencePanel(measurementNames));
        panel.add(createSamplePanel(measurementNames));
        panel.add(createAxisPanel());

        return panel;
    }

    /**
     * Creates panel for reference selection.
     * 
     * @param measurementNames array of measurement names
     * @return configured JPanel
     */
    private JPanel createReferencePanel(String[] measurementNames) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Reference"));
        
        referenceBox = new JComboBox<>(measurementNames);
        panel.add(new JLabel("Reference:"));
        panel.add(referenceBox);
        
        return panel;
    }

    /**
     * Creates panel for sample selection.
     * 
     * @param measurementNames array of measurement names
     * @return configured JPanel
     */
    private JPanel createSamplePanel(String[] measurementNames) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Sample"));
        
        sampleBox = new JComboBox<>(measurementNames);
        panel.add(new JLabel("Sample:"));
        panel.add(sampleBox);
        
        return panel;
    }

    /**
     * Creates panel for axis type selection (wavelength vs frequency).
     * 
     * @return configured JPanel
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
     * 
     * @return configured JPanel with buttons
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
     * Returns whether the user confirmed the dialog (clicked OK).
     * 
     * @return true if OK was clicked, false otherwise
     */
    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * Returns the name of the selected reference measurement.
     * 
     * @return the reference measurement name, or null if none selected
     */
    public String getReferenceName() {
        return (String) referenceBox.getSelectedItem();
    }

    /**
     * Returns the name of the selected sample measurement.
     * 
     * @return the sample measurement name, or null if none selected
     */
    public String getSampleName() {
        return (String) sampleBox.getSelectedItem();
    }

    /**
     * Returns whether the wavelength axis should be used.
     * 
     * @return true for wavelength axis, false for frequency axis
     */
    public boolean isUseWavelength() {
        return wavelengthButton.isSelected();
    }
}