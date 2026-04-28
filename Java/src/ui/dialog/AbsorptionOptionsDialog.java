package ui.dialog;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Dialog for configuring absorption plot options.
 * Allows selection of reference and sample measurements and axis type.
 */
public class AbsorptionOptionsDialog extends JDialog {

    // ==================== UI COMPONENTS ====================
    
    private JComboBox<String> referenceBox;
    private JComboBox<String> sampleBox;
    private JRadioButton wavelengthButton;
    private JRadioButton frequencyButton;
    
    // ==================== STATE ====================
    
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
     * Creates panel for axis type selection.
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
     * @return Name of selected reference measurement
     */
    public String getReferenceName() {
        return (String) referenceBox.getSelectedItem();
    }

    /**
     * @return Name of selected sample measurement
     */
    public String getSampleName() {
        return (String) sampleBox.getSelectedItem();
    }

    /**
     * @return true if wavelength axis should be used (false for frequency)
     */
    public boolean isUseWavelength() {
        return wavelengthButton.isSelected();
    }
}