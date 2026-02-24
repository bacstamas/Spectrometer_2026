import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.HashMap;

import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XChartPanel;


public class MainWindow extends JFrame {

    private DefaultListModel<String> measurementListModel;
    private JList<String> measurementList;
    private Spectrometer spectrometer;
    private Map<String, MeasurementSet> measurementSets = new HashMap<>();

    private JPanel centerPanel;
    private JLabel plotLabel;

    public MainWindow() {

        setTitle("Spectrometer GUI");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        createMenuBar();
        createLeftPanel();
        createCenterPanel();
    }

    /* ================= MENU ================= */

    private void createMenuBar() {

        JMenuBar menuBar = new JMenuBar();

        /* ========== FILE ========== */

        JMenu fileMenu = new JMenu("File");

        JMenuItem loadItem = new JMenuItem("Load Measurement");
        JMenuItem saveItem = new JMenuItem("Save Measurement");
        JMenuItem exitItem = new JMenuItem("Exit");

        loadItem.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Load Measurement");

            int result = chooser.showOpenDialog(this);
            if (result != JFileChooser.APPROVE_OPTION) {
                return; // user cancelled
            }

            java.io.File file = chooser.getSelectedFile();
            try {
                MeasurementSet set =
                        MeasurementSet.loadFromFile(file.getAbsolutePath());

                // ---- VALIDATION ----
                java.util.List measurements = set.getMeasurements();
                if (measurements.isEmpty()) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Selected file does not contain any measurement data.",
                            "Invalid file",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                // expect 6 values per spectrum for AS726x
                int expectedChannels = 6;
                for (Object o : measurements) {
                    if (!(o instanceof double[])) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Selected file is not a valid measurement file.",
                                "Invalid file",
                                JOptionPane.ERROR_MESSAGE
                        );
                        return;
                    }
                    double[] m = (double[]) o;
                    if (m.length != expectedChannels) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Selected file has wrong data format (expected "
                                        + expectedChannels + " channels).",
                                "Invalid file",
                                JOptionPane.ERROR_MESSAGE
                        );
                        return;
                    }
                }
                // ---- END VALIDATION ----

                String name = set.getName();
                if (name == null || name.trim().isEmpty()) {
                    name = file.getName();
                }

                measurementSets.put(name, set);
                addMeasurement(name);

                JOptionPane.showMessageDialog(
                        this,
                        "Loaded measurement:\n" + name,
                        "Load successful",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                        this,
                        "Failed to load:\n" + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        saveItem.addActionListener(e -> {
            String selectedName = getSelectedMeasurement();
            if (selectedName == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "No measurement selected.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            MeasurementSet set = measurementSets.get(selectedName);
            if (set == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "No data available for the selected measurement.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Save Measurement");

            // Suggest default file name based on measurement name
            chooser.setSelectedFile(new java.io.File(selectedName + ".txt"));

            int result = chooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                java.io.File file = chooser.getSelectedFile();
                try {
                    set.saveToFile(file.getAbsolutePath());
                    JOptionPane.showMessageDialog(
                            this,
                            "Saved to:\n" + file.getAbsolutePath(),
                            "Save successful",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(
                            this,
                            "Failed to save:\n" + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });

        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(loadItem);
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        /* ========== MEASUREMENT ========== */

        JMenu measurementMenu = new JMenu("Measurement");

        JMenuItem connectItem = new JMenuItem("Connect");
        JMenuItem configureItem = new JMenuItem("Configure");
        JMenuItem measureItem = new JMenuItem("Measure");

        configureItem.setEnabled(false);
        measureItem.setEnabled(false);

        connectItem.addActionListener(e -> {

            try {
                spectrometer = new Spectrometer();

                JOptionPane.showMessageDialog(
                        this,
                        "Connected to " + spectrometer.getPortName(),
                        "Connection successful",
                        JOptionPane.INFORMATION_MESSAGE
                );

                configureItem.setEnabled(true);
                measureItem.setEnabled(true);

            } catch (Exception ex) {

                spectrometer = null;

                JOptionPane.showMessageDialog(
                        this,
                        "Unable to connect.",
                        "Connection error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        configureItem.addActionListener(e -> {

            if (spectrometer == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "Not connected to spectrometer.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            // Current parameters could later be read back from the spectrometer.
            Map<String, Object> currentParams = new HashMap<>();

            ConfigureDialog dialog = new ConfigureDialog(this, currentParams);
            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                Map<String, Object> params = dialog.getParameters();
                System.out.println("New parameters: " + params);

                // Map dialog params -> Spectrometer.configure(...)
                int integrationTime      = (int) params.get("int");
                int gain                 = (int) params.get("gain");
                int avg                  = (int) params.get("avg");
                int numberOfMeasurements = (int) params.get("count");
                String mode              = params.get("mode").toString();
                int lightInt             = (int) params.get("light");

                // Apply configuration to the spectrometer
                spectrometer.configure(
                        integrationTime,
                        gain,
                        avg,
                        mode,
                        numberOfMeasurements,
                        lightInt
                );
            }
        });

        measureItem.addActionListener(e -> {
            if (spectrometer == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "Not connected to spectrometer.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            String baseName = JOptionPane.showInputDialog(
                    this,
                    "Enter measurement name:",
                    "New Measurement",
                    JOptionPane.PLAIN_MESSAGE
            );

            if (baseName == null || baseName.trim().isEmpty()) {
                // user cancelled or empty input
                return;
            }

            try {
                spectrometer.measure(baseName.trim());
                MeasurementSet set = spectrometer.getMeasurementSet();
                String fullName = set.getName(); // baseName + timestamp
                addMeasurement(fullName);
                measurementSets.put(fullName, set);
                JOptionPane.showMessageDialog(
                        this,
                        "Measurement completed:\n" + fullName,
                        "Measurement",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                        this,
                        "Measurement failed:\n" + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        measurementMenu.add(connectItem);
        measurementMenu.addSeparator();
        measurementMenu.add(configureItem);
        measurementMenu.add(measureItem);

        /* ========== VIEW ========== */

        JMenu viewMenu = new JMenu("View");

        JMenuItem spectrumItem = new JMenuItem("Spectrum");
        JMenuItem absorptionItem = new JMenuItem("Absorption");

        spectrumItem.addActionListener(e -> {
            String name = getSelectedMeasurement();
            if (name == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "No measurement selected.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            MeasurementSet set = measurementSets.get(name);
            if (set == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "No data available for the selected measurement.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            // Ask user for plot options (your SpectrumOptionsDialog)
            SpectrumOptionsDialog dialog = new SpectrumOptionsDialog(this);
            dialog.setVisible(true);
            if (!dialog.isConfirmed()) {
                return;
            }

            // Configure visualizer
            Visualizer vis = new Visualizer(set);
            vis.setPlotType(dialog.getPlotType());
            vis.setNormalize(dialog.isNormalize());
            vis.setShowErrorBars(dialog.isShowErrorBars());
            vis.useWavelengthAxis(dialog.isUseWavelength());

            // Use XChartPanel for interactive plot
            centerPanel.removeAll();

            if ("bar".equals(dialog.getPlotType())) {
                CategoryChart chart = vis.createBarChart();
                XChartPanel<CategoryChart> panel = new XChartPanel<>(chart);
                centerPanel.add(panel, BorderLayout.CENTER);
            } else {
                XYChart chart = vis.createCurveChart();
                XChartPanel<XYChart> panel = new XChartPanel<>(chart);
                centerPanel.add(panel, BorderLayout.CENTER);
            }

            centerPanel.revalidate();
            centerPanel.repaint();
        });

        absorptionItem.addActionListener(e -> {
            // Build list of available measurement names
            java.util.List<String> names = new java.util.ArrayList<>();
            for (int i = 0; i < measurementListModel.size(); i++) {
                names.add(measurementListModel.getElementAt(i));
            }
            if (names.size() < 2) {
                JOptionPane.showMessageDialog(
                        this,
                        "Need at least two measurements (reference and sample).",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            AbsorptionOptionsDialog dialog =
                    new AbsorptionOptionsDialog(this, names);
            dialog.setVisible(true);
            if (!dialog.isConfirmed()) {
                return;
            }

            String refName = dialog.getReferenceName();
            String sampleName = dialog.getSampleName();
            if (refName == null || sampleName == null ||
                refName.equals(sampleName)) {
                JOptionPane.showMessageDialog(
                        this,
                        "Please choose two different measurements.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            MeasurementSet refSet = measurementSets.get(refName);
            MeasurementSet sampleSet = measurementSets.get(sampleName);
            if (refSet == null || sampleSet == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "Selected measurements are not available.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            // Configure visualizer with axis mode
            Visualizer vis = new Visualizer(refSet); // measurementSet field is not used for absorption data themselves
            vis.useWavelengthAxis(dialog.isUseWavelength());

            XYChart chart = vis.createAbsorptionChart(refSet, sampleSet);

            centerPanel.removeAll();
            XChartPanel<XYChart> panel = new XChartPanel<>(chart);
            centerPanel.add(panel, BorderLayout.CENTER);
            centerPanel.revalidate();
            centerPanel.repaint();
        });


        viewMenu.add(spectrumItem);
        viewMenu.add(absorptionItem);

        /* ========== ADD TO BAR ========== */

        menuBar.add(fileMenu);
        menuBar.add(measurementMenu);
        menuBar.add(viewMenu);

        setJMenuBar(menuBar);
    }

    /* ================= LEFT PANEL ================= */

    private void createLeftPanel() {

        measurementListModel = new DefaultListModel<>();
        measurementList = new JList<>(measurementListModel);
        measurementList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // delet key functionality
        InputMap im = measurementList.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = measurementList.getActionMap();

        im.put(KeyStroke.getKeyStroke("DELETE"), "deleteMeasurement");
        am.put("deleteMeasurement", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                deleteSelectedMeasurement();  // your existing delete method
            }
        });

        measurementList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String name = measurementList.getSelectedValue();
                    if (name == null) return;

                    MeasurementSet set = measurementSets.get(name);
                    if (set == null) {
                        JOptionPane.showMessageDialog(
                                MainWindow.this,
                                "No data stored for this measurement.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                        return;
                    }
                    showMeasurementDetails(set);
                }
            }
        });

        JScrollPane scrollPane =
                new JScrollPane(measurementList);

        scrollPane.setPreferredSize(new Dimension(250, 0));

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(
                BorderFactory.createTitledBorder("Measurements"));

        leftPanel.add(scrollPane, BorderLayout.CENTER);

        add(leftPanel, BorderLayout.WEST);
    }

    /* ================= CENTER PANEL ================= */

    private void createCenterPanel() {
        centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(
                BorderFactory.createTitledBorder("Visualization")
        );

        plotLabel = new JLabel("No plot yet", SwingConstants.CENTER);
        centerPanel.add(plotLabel, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
    }

    /* ================= PUBLIC API ================= */

    public void addMeasurement(String name) {
        measurementListModel.addElement(name);
    }

    public String getSelectedMeasurement() {
        return measurementList.getSelectedValue();
    }

    /* ================= HELPER ================= */

    private void showMeasurementDetails(MeasurementSet set) {
        StringBuilder sb = new StringBuilder();

        // Name
        sb.append("Name: ").append(set.getName()).append("\n\n");

        sb.append("Parameters:\n");
        Map params = set.getParameters();

        Object intTime = params.get("integrationTime");
        Object gain = params.get("gain");
        Object avg = params.get("avg");
        Object count = params.get("numberOfMeasurements");
        Object light   = params.get("lightInt");

        sb.append("  Integration time: ")
          .append(intTime != null ? intTime : "?")
          .append(" ms\n");

        sb.append("  Gain: ")
          .append(gain != null ? gain : "?")
          .append("\n");

        sb.append("  Averaging: ")
          .append(avg != null ? avg : "?")
          .append("\n");

        sb.append("  Number of measurements: ")
          .append(count != null ? count : "?")
          .append("\n");
        sb.append("  Light intensity: ")
          .append(light != null ? light : "?")
          .append("\n");


        // Statistics
        MeasurementSet.StatisticsResult stats = set.getAverageAndStd();
        double[] mean = stats.mean;
        double[] std  = stats.std;

        // Wavelength labels for AS726x visible channels
        String[] wavelengths = {
                "450 nm", "500 nm", "550 nm",
                "570 nm", "600 nm", "650 nm"
        };

        sb.append("\nWavelength\tAverage\tStdDev\n");
        int n = Math.min(mean.length, wavelengths.length);
        for (int i = 0; i < n; i++) {
            sb.append(wavelengths[i])
              .append("\t")
              .append(String.format("%.3f", mean[i]))
              .append("\t")
              .append(String.format("%.3f", std[i]))
              .append("\n");
        }

        JTextArea area = new JTextArea(sb.toString(), 20, 30);
        area.setEditable(false);
        JScrollPane scroll = new JScrollPane(area);

        JOptionPane.showMessageDialog(
                this,
                scroll,
                "Measurement details",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void deleteSelectedMeasurement() {
        String name = getSelectedMeasurement();
        if (name == null) {
            return;
        }

        int index = measurementList.getSelectedIndex();
        if (index >= 0) {
            measurementListModel.remove(index);
        }

        if (measurementSets != null) {
            measurementSets.remove(name);
        }
    }
}
