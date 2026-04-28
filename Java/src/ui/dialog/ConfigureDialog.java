package ui.dialog;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Dialog for configuring spectrometer measurement parameters.
 * Provides input fields for integration time, gain, averaging, etc.
 */
public class ConfigureDialog extends JDialog {

    // ==================== UI COMPONENTS ====================
    
    private JTextField intField;
    private JTextField avgField;
    private JTextField countField;
    private JSpinner lightSpinner;
    private JComboBox<Integer> gainBox;
    private JComboBox<String> modeBox;
    
    // ==================== STATE ====================
    
    private boolean confirmed = false;
    
    // ==================== CONSTANTS ====================
    
    private static final Integer[] GAIN_OPTIONS = {1, 4, 16, 64};
    private static final String[] MODE_OPTIONS = {"raw", "cal"};
    private static final int LIGHT_MIN = 0;
    private static final int LIGHT_MAX = 100;
    private static final int LIGHT_STEP = 1;
    private static final int DIALOG_WIDTH = 360;
    private static final int DIALOG_HEIGHT = 300;
    private static final int SPINNER_WIDTH = 80;
    private static final int SPINNER_HEIGHT = 25;

    /**
     * Constructs the configuration dialog.
     * @param parent Parent frame
     * @param currentParams Current parameter values (may be empty)
     */
    public ConfigureDialog(JFrame parent, Map<String, Object> currentParams) {
        super(parent, "Configure Measurement", true);
        initializeDialog();
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(createFormPanel(currentParams), BorderLayout.CENTER);
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

    // ==================== FORM CREATION ====================

    /**
     * Creates the input form panel.
     */
    private JPanel createFormPanel(Map<String, Object> params) {
        JPanel panel = new JPanel(new GridLayout(6, 2, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        initializeComponents(params);
        
        addFormRow(panel, "Integration time (ms):", intField);
        addFormRow(panel, "Gain:", gainBox);
        addFormRow(panel, "Averaging:", avgField);
        addFormRow(panel, "Measurements:", countField);
        addFormRow(panel, "Mode:", modeBox);
        addFormRow(panel, "Light intensity (0-100):", lightSpinner);

        return panel;
    }
    
    /**
     * Helper to add a labeled row to the form.
     */
    private void addFormRow(JPanel panel, String label, JComponent component) {
        panel.add(new JLabel(label));
        panel.add(component);
    }
    
    /**
     * Initializes all input components with current values.
     */
    private void initializeComponents(Map<String, Object> params) {
        intField = new JTextField(getParamValue(params, "int", "50"));
        avgField = new JTextField(getParamValue(params, "avg", "1"));
        countField = new JTextField(getParamValue(params, "count", "10"));
        
        gainBox = new JComboBox<>(GAIN_OPTIONS);
        gainBox.setSelectedItem(Integer.parseInt(getParamValue(params, "gain", "16")));
        
        modeBox = new JComboBox<>(MODE_OPTIONS);
        modeBox.setSelectedItem(getParamValue(params, "mode", "raw"));
        
        lightSpinner = createLightSpinner(params);
    }
    
    /**
     * Creates the light intensity spinner.
     */
    private JSpinner createLightSpinner(Map<String, Object> params) {
        int initialValue = Integer.parseInt(getParamValue(params, "light", "50"));
        SpinnerNumberModel model = new SpinnerNumberModel(
            initialValue, LIGHT_MIN, LIGHT_MAX, LIGHT_STEP
        );
        
        JSpinner spinner = new JSpinner(model);
        spinner.setPreferredSize(new Dimension(SPINNER_WIDTH, SPINNER_HEIGHT));
        return spinner;
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

    // ==================== RESULT HANDLING ====================

    /**
     * @return true if user confirmed (clicked OK)
     */
    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * @return Map of parameter names to values entered by user
     */
    public Map<String, Object> getParameters() {
        Map<String, Object> params = new HashMap<>();

        params.put("int", Integer.parseInt(intField.getText()));
        params.put("gain", (Integer) gainBox.getSelectedItem());
        params.put("avg", Integer.parseInt(avgField.getText()));
        params.put("count", Integer.parseInt(countField.getText()));
        params.put("mode", modeBox.getSelectedItem().toString());
        params.put("light", lightSpinner.getValue());

        return params;
    }

    // ==================== HELPER METHODS ====================

    /**
     * Safely gets a parameter value or returns default.
     */
    private String getParamValue(Map<String, Object> params, String key, String defaultValue) {
        Object value = params.get(key);
        return value == null ? defaultValue : value.toString();
    }
}