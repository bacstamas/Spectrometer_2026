import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.HashMap;

class SpectrumOptionsDialog extends JDialog {

    private JRadioButton barButton;
    private JRadioButton curveButton;
    private JCheckBox normalizeBox;
    private JCheckBox errorBarsBox;
    private JRadioButton wavelengthButton;
    private JRadioButton frequencyButton;

    private boolean confirmed = false;

    public SpectrumOptionsDialog(JFrame parent) {
        super(parent, "Spectrum options", true);
        setSize(300, 300);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(4, 1, 4, 4));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Plot type
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        typePanel.setBorder(BorderFactory.createTitledBorder("Plot type"));
        barButton = new JRadioButton("Bar");
        curveButton = new JRadioButton("Curve", true);
        ButtonGroup typeGroup = new ButtonGroup();
        typeGroup.add(barButton);
        typeGroup.add(curveButton);
        typePanel.add(barButton);
        typePanel.add(curveButton);

        // Normalize
        JPanel normPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        normalizeBox = new JCheckBox("Normalize", false);
        normPanel.add(normalizeBox);

        // Error bars
        JPanel errPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        errorBarsBox = new JCheckBox("Show error bars", true);
        errPanel.add(errorBarsBox);

        // Axis
        JPanel axisPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        axisPanel.setBorder(
                BorderFactory.createTitledBorder("X axis")
        );
        wavelengthButton = new JRadioButton("Wavelength", true);
        frequencyButton = new JRadioButton("Frequency");
        ButtonGroup axisGroup = new ButtonGroup();
        axisGroup.add(wavelengthButton);
        axisGroup.add(frequencyButton);
        axisPanel.add(wavelengthButton);
        axisPanel.add(frequencyButton);

        panel.add(typePanel);
        panel.add(normPanel);
        panel.add(errPanel);
        panel.add(axisPanel);

        add(panel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
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
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public boolean isConfirmed() { return confirmed; }

    public String getPlotType() {
        return barButton.isSelected() ? "bar" : "curve";
    }

    public boolean isNormalize() {
        return normalizeBox.isSelected();
    }

    public boolean isShowErrorBars() {
        return errorBarsBox.isSelected();
    }

    public boolean isUseWavelength() {
        return wavelengthButton.isSelected();
    }
}
