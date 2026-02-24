import org.knowm.xchart.*;

import java.io.IOException;
import java.util.*;

public class Visualizer {

    private MeasurementSet measurementSet;

    private String plotType = "curve";      // "curve" or "bar"
    private boolean normalize = false;
    private boolean showErrorBars = true;

    /* ===== X-AXIS MODE ===== */
    private boolean useWavelength = true;

    // Wavelengths for AS7262 visible channels
    private double[] wavelengthsNm = {450, 500, 550, 570, 600, 650};

    // Optional: compute corresponding frequencies (THz)
    private double[] frequenciesTHz = {
            299792458.0 / (450e-9) / 1e12, // ≈ 666.2 THz
            299792458.0 / (500e-9) / 1e12, // ≈ 599.6 THz
            299792458.0 / (550e-9) / 1e12, // ≈ 545.1 THz
            299792458.0 / (570e-9) / 1e12, // ≈ 526.0 THz
            299792458.0 / (600e-9) / 1e12, // ≈ 499.7 THz
            299792458.0 / (650e-9) / 1e12  // ≈ 461.2 THz
    };


    public Visualizer(MeasurementSet measurementSet) {
        this.measurementSet = measurementSet;
    }

    /* ===================== CONFIG ===================== */

    public void setPlotType(String plotType) {
        this.plotType = plotType.toLowerCase();
    }

    public void setNormalize(boolean normalize) {
        this.normalize = normalize;
    }

    public void setShowErrorBars(boolean showErrorBars) {
        this.showErrorBars = showErrorBars;
    }

    public void useWavelengthAxis(boolean useWavelength) {
        this.useWavelength = useWavelength;
    }

    public void setWavelengths(double[] wavelengthsNm) {
        this.wavelengthsNm = wavelengthsNm;
    }

    public void setFrequencies(double[] frequenciesTHz) {
        this.frequenciesTHz = frequenciesTHz;
    }

    /* ===================== PUBLIC ===================== */

    public void savePlot(String filename) throws IOException {
        if (plotType.equals("bar")) {
            saveBarPlot(filename);
        } else {
            saveCurvePlot(filename);
        }
    }

    /* ===================== BAR PLOT ===================== */

    private void saveBarPlot(String filename) throws IOException {

        MeasurementSet.StatisticsResult stats =
                measurementSet.getAverageAndStd();

        double[] mean = stats.mean;
        double[] std = stats.std;

        List<String> xLabels = new ArrayList<>();
        double[] axisValues = useWavelength ? wavelengthsNm : frequenciesTHz;
        String xLabel = useWavelength ? "Wavelength (nm)" : "Frequency (THz)";

        for (double v : axisValues) {
            xLabels.add(String.valueOf(v));
        }

        List<Double> y;
        List<Double> e;

        if (normalize) {
            NormalizedData data = normalize(mean, std);
            y = data.y;
            e = data.e;
        } else {
            y = toList(mean);
            e = toList(std);
        }

        CategoryChart chart = new CategoryChartBuilder()
                .width(800)
                .height(600)
                .title("Spectrum")
                .xAxisTitle(xLabel)
                .yAxisTitle(getYLabel())
                .build();

        if (showErrorBars) {
            chart.addSeries("Intensity", xLabels, y, e);
        } else {
            chart.addSeries("Intensity", xLabels, y);
        }

        BitmapEncoder.saveBitmap(chart, filename,
                BitmapEncoder.BitmapFormat.PNG);
    }

    /* ===================== CURVE PLOT ===================== */

    private void saveCurvePlot(String filename) throws IOException {

        MeasurementSet.StatisticsResult stats =
                measurementSet.getAverageAndStd();

        double[] mean = stats.mean;
        double[] std = stats.std;

        double[] axisValues = useWavelength ? wavelengthsNm : frequenciesTHz;
        String xLabel = useWavelength ? "Wavelength (nm)" : "Frequency (THz)";

        List<Double> x = toList(axisValues);

        List<Double> y;
        List<Double> e;

        if (normalize) {
            NormalizedData data = normalize(mean, std);
            y = data.y;
            e = data.e;
        } else {
            y = toList(mean);
            e = toList(std);
        }

        XYChart chart = new XYChartBuilder()
                .width(800)
                .height(600)
                .title("Spectrum")
                .xAxisTitle(xLabel)
                .yAxisTitle(getYLabel())
                .build();

        if (showErrorBars) {
            chart.addSeries("Intensity", x, y, e);
        } else {
            chart.addSeries("Intensity", x, y);
        }

        BitmapEncoder.saveBitmap(chart, filename,
                BitmapEncoder.BitmapFormat.PNG);
    }

    /* ===================== CREATE BAR CHART ===================== */

    public CategoryChart createBarChart() {
        MeasurementSet.StatisticsResult stats =
                measurementSet.getAverageAndStd();
        double[] mean = stats.mean;
        double[] std = stats.std;

        // X axis labels
        List<String> xLabels = new ArrayList<>();
        double[] axisValues = useWavelength ? wavelengthsNm : frequenciesTHz;
        String xLabel = useWavelength ? "Wavelength (nm)" : "Frequency (THz)";

        if (useWavelength) {
            for (double v : axisValues) {
                xLabels.add(String.valueOf((int) v));       // 450, 500, ...
            }
        } else {
            for (double v : axisValues) {
                xLabels.add(String.format("%.2f", v));      // 2 decimals
            }
        }

        // Y values
        List<Double> y;
        List<Double> e;
        if (normalize) {
            NormalizedData data = normalize(mean, std);
            y = data.y;
            e = data.e;
        } else {
            y = toList(mean);
            e = toList(std);
        }

        // If plotting frequencies, reverse axis and data so lowest freq is left
        if (!useWavelength) {
            java.util.Collections.reverse(xLabels);
            java.util.Collections.reverse(y);
            java.util.Collections.reverse(e);
        }

        CategoryChart chart = new CategoryChartBuilder()
            .width(800)
            .height(600)
            .title("Spectrum")
            .xAxisTitle(xLabel)
            .yAxisTitle(getYLabel())
            .build();

        if (showErrorBars) {
            chart.addSeries("Intensity", xLabels, y, e);
        } else {
            chart.addSeries("Intensity", xLabels, y);
        }

        return chart;
    }

    /* ===================== CREATE CURVE CHART ===================== */

    public XYChart createCurveChart() {
        MeasurementSet.StatisticsResult stats =
                measurementSet.getAverageAndStd();
        double[] mean = stats.mean;
        double[] std = stats.std;

        double[] axisValues = useWavelength ? wavelengthsNm : frequenciesTHz;
        String xLabel = useWavelength ? "Wavelength (nm)" : "Frequency (THz)";

        List<Double> x = toList(axisValues);
        List<Double> y;
        List<Double> e;
        if (normalize) {
            NormalizedData data = normalize(mean, std);
            y = data.y;
            e = data.e;
        } else {
            y = toList(mean);
            e = toList(std);
        }

        XYChart chart = new XYChartBuilder()
                .width(800)
                .height(600)
                .title("Spectrum")
                .xAxisTitle(xLabel)
                .yAxisTitle(getYLabel())
                .build();

        if (showErrorBars) {
            chart.addSeries("Intensity", x, y, e);
        } else {
            chart.addSeries("Intensity", x, y);
        }

        return chart;
    }

    /* ===================== ABSORPTION PLOT ===================== */
    public void saveAbsorptionPlot(MeasurementSet set0,
                               MeasurementSet set1,
                               String filename) throws IOException {

        MeasurementSet.StatisticsResult s0 = set0.getAverageAndStd();
        MeasurementSet.StatisticsResult s1 = set1.getAverageAndStd();

        double[] a0 = s0.mean;
        double[] a1 = s1.mean;

        int n = Math.min(a0.length, a1.length);

        double[] absorbance = new double[n];

        for (int i = 0; i < n; i++) {
            if (a0[i] > 0 && a1[i] > 0) {
                absorbance[i] = -Math.log10(a1[i] / a0[i]);
            } else {
                absorbance[i] = Double.NaN; // physically invalid
            }
        }

        double[] axisValues = useWavelength ? wavelengthsNm : frequenciesTHz;
        String xLabel = useWavelength ? "Wavelength (nm)" : "Frequency (THz)";

        List<Double> x = toList(axisValues);
        List<Double> y = toList(absorbance);

        XYChart chart = new XYChartBuilder()
                .width(800)
                .height(600)
                .title("Absorption Spectrum")
                .xAxisTitle(xLabel)
                .yAxisTitle("Absorbance")
                .build();

        chart.addSeries("Absorbance", x, y);

        BitmapEncoder.saveBitmap(chart, filename,
                BitmapEncoder.BitmapFormat.PNG);
    }

    /* ===================== CREATE ABSORPTION CHART ===================== */

    public XYChart createAbsorptionChart(MeasurementSet set0, MeasurementSet set1) {
        MeasurementSet.StatisticsResult s0 = set0.getAverageAndStd();
        MeasurementSet.StatisticsResult s1 = set1.getAverageAndStd();
        double[] a0 = s0.mean;
        double[] a1 = s1.mean;

        int n = Math.min(a0.length, a1.length);
        double[] absorbance = new double[n];
        for (int i = 0; i < n; i++) {
            if (a0[i] > 0 && a1[i] > 0) {
                absorbance[i] = -Math.log10(a1[i] / a0[i]);
            } else {
                absorbance[i] = Double.NaN;
            }
        }

        double[] axisValues = useWavelength ? wavelengthsNm : frequenciesTHz;
        String xLabel = useWavelength ? "Wavelength (nm)" : "Frequency (THz)";

        List<Double> x = toList(axisValues);
        List<Double> y = toList(absorbance);

        XYChart chart = new XYChartBuilder()
                .width(800)
                .height(600)
                .title("Absorption Spectrum")
                .xAxisTitle(xLabel)
                .yAxisTitle("Absorbance")
                .build();

        chart.addSeries("Absorbance", x, y);
        return chart;
    }

    /* ===================== HELPERS ===================== */

    private String getYLabel() {
        Map<String, Object> params = measurementSet.getParameters();
        Object mode = params.get("mode");

        if (mode != null && mode.toString().equalsIgnoreCase("cal")) {
            return "Calibrated Intensity";
        }
        return "Raw Counts";
    }

    private List<Double> toList(double[] arr) {
        List<Double> list = new ArrayList<>();
        for (double v : arr) list.add(v);
        return list;
    }

    /* ======== NORMALIZATION ======== */

    private static class NormalizedData {
        List<Double> y;
        List<Double> e;
    }

    private NormalizedData normalize(double[] mean, double[] std) {

        double max = Arrays.stream(mean).max().orElse(1.0);

        NormalizedData data = new NormalizedData();
        data.y = new ArrayList<>();
        data.e = new ArrayList<>();

        for (int i = 0; i < mean.length; i++) {
            data.y.add(mean[i] / max);
            data.e.add(std[i] / max);
        }

        return data;
    }
}
