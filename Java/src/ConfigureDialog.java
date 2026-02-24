import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class ConfigureDialog extends JDialog {

    private JTextField intField;
    private JTextField avgField;
    private JTextField countField;
    private JSpinner lightSpinner;

    private JComboBox<Integer> gainBox;
    private JComboBox<String> modeBox;

    private boolean confirmed = false;

    public ConfigureDialog(JFrame parent,
                           Map<String, Object> currentParams) {

        super(parent, "Configure Measurement", true);
        setSize(360, 300);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        add(createFormPanel(currentParams), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    /* ================= FORM ================= */

    private JPanel createFormPanel(Map<String, Object> params) {

        JPanel panel = new JPanel(new GridLayout(6, 2, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        intField = new JTextField(value(params, "int", "50"));
        avgField = new JTextField(value(params, "avg", "1"));
        countField = new JTextField(value(params, "count", "10"));
        gainBox = new JComboBox<>(new Integer[]{1, 4, 16, 64});
        gainBox.setSelectedItem(Integer.parseInt(value(params, "gain", "16")));
        modeBox = new JComboBox<>(new String[]{"raw", "cal"});
        modeBox.setSelectedItem(value(params, "mode", "raw"));

        // Light intensity spinner (0..100)
        SpinnerNumberModel lightModel = new SpinnerNumberModel(
            Integer.parseInt(value(params, "light", "50")),  // initial value
            0,                                               // min
            100,                                             // max
            1                                                // step
        );
        lightSpinner = new JSpinner(lightModel);
        lightSpinner.setPreferredSize(new Dimension(80, 25));  // nice width

        panel.add(new JLabel("Integration time (ms):"));
        panel.add(intField);

        panel.add(new JLabel("Gain:"));
        panel.add(gainBox);

        panel.add(new JLabel("Averaging:"));
        panel.add(avgField);

        panel.add(new JLabel("Measurements:"));
        panel.add(countField);

        panel.add(new JLabel("Mode:"));
        panel.add(modeBox);

        panel.add(new JLabel("Light intensity (0-100):"));
        panel.add(lightSpinner);

        return panel;
    }

    /* ================= BUTTONS ================= */

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

    /* ================= RESULT ================= */

    public boolean isConfirmed() {
        return confirmed;
    }

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

    /* ================= HELPER ================= */

    private String value(Map<String, Object> params,
                         String key,
                         String def) {
        Object v = params.get(key);
        return v == null ? def : v.toString();
    }
}
