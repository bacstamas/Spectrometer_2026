import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {

        try {
            /*
            Spectrometer spec = new Spectrometer();

            spec.configure(
                    50,     // integration time
                    16,     // gain
                    5,      // averaging
                    "cal",  // mode
                    10      // number of measurements
            );

            spec.measure("DarkReference");

            MeasurementSet set = spec.getMeasurementSet();

            set.saveToFile("DarkReference.txt");

            System.out.println(set);

            MeasurementSet.StatisticsResult stats =
                    set.getAverageAndStd();

            System.out.println("Averages:");
            for (double v : stats.mean) {
                System.out.printf("%8.3f ", v);
            }

            System.out.println("\nStandard deviations:");
            for (double v : stats.std) {
                System.out.printf("%8.3f ", v);
            }
            System.out.println();


            Visualizer vis = new Visualizer(set);
            vis.setPlotType("curve");      // "curve" or "bar"
            //vis.setInterpolation("linear");
            vis.setNormalize(false);
            vis.setShowErrorBars(true);
            vis.useWavelengthAxis(false);
            vis.savePlot("spectrum1.png");
            vis.useWavelengthAxis(true);
            vis.savePlot("spectrum2.png");

            System.out.println("\nDelay\n");
            Thread.sleep(10_000); // 10 seconds


            spec.measure("Sample");

            MeasurementSet set1 = spec.getMeasurementSet();
            System.out.println(set);
            System.out.println(set1);
            vis.saveAbsorptionPlot(set, set1, "absorption.png");

            spec.close();

            MeasurementSet loaded = MeasurementSet.loadFromFile("DarkReference.txt");

            System.out.println(loaded);
            */
            SwingUtilities.invokeLater(() -> {
                MainWindow window = new MainWindow();
                window.setVisible(true);
                //window.addMeasurement("DarkReference_2026-01-04_14-32-18");
                //window.addMeasurement("Sample_2026-01-04_14-45-02");
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
