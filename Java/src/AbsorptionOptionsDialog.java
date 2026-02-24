import javax.swing.*;
import java.awt.*;
import java.util.List;

class AbsorptionOptionsDialog extends JDialog {

    private JComboBox<String> refBox;
    private JComboBox<String> sampleBox;
    private JRadioButton wavelengthButton;
    private JRadioButton frequencyButton;
    private boolean confirmed = false;

    public AbsorptionOptionsDialog(JFrame parent, List<String> measurementNames) {
        super(parent, "Absorption options", true);
        setSize(350, 250);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(3, 1, 4, 4));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Reference & sample selectors
        JPanel refPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        refPanel.setBorder(BorderFactory.createTitledBorder("Reference"));
        refBox = new JComboBox<>(measurementNames.toArray(new String[0]));
        refPanel.add(new JLabel("Reference:"));
        refPanel.add(refBox);

        JPanel samplePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        samplePanel.setBorder(BorderFactory.createTitledBorder("Sample"));
        sampleBox = new JComboBox<>(measurementNames.toArray(new String[0]));
        samplePanel.add(new JLabel("Sample:"));
        samplePanel.add(sampleBox);

        // Axis
        JPanel axisPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        axisPanel.setBorder(BorderFactory.createTitledBorder("X axis"));
        wavelengthButton = new JRadioButton("Wavelength", true);
        frequencyButton = new JRadioButton("Frequency");
        ButtonGroup axisGroup = new ButtonGroup();
        axisGroup.add(wavelengthButton);
        axisGroup.add(frequencyButton);
        axisPanel.add(wavelengthButton);
        axisPanel.add(frequencyButton);

        panel.add(refPanel);
        panel.add(samplePanel);
        panel.add(axisPanel);
        add(panel, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
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
        buttons.add(okButton);
        buttons.add(cancelButton);
        add(buttons, BorderLayout.SOUTH);
    }

    public boolean isConfirmed() { return confirmed; }

    public String getReferenceName() {
        return (String) refBox.getSelectedItem();
    }

    public String getSampleName() {
        return (String) sampleBox.getSelectedItem();
    }

    public boolean isUseWavelength() {
        return wavelengthButton.isSelected();
    }
}
