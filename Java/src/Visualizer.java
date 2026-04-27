import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;
import java.util.*;

/**
 * Handles visualization of spectrometer data.
 * Supports bar and curve plots with optional error bars and normalization.
 */
public class Visualizer {

    private MeasurementSet measurementSet;
    
    // Plot configuration
    private PlotType plotType = PlotType.CURVE;
    private boolean normalize = false;
    private boolean showErrorBars = true;
    private AxisType axisType = AxisType.WAVELENGTH;
    
    // Physical constants and data
    private static final double SPEED_OF_LIGHT = 299792458.0; // m/s
    
    // AS7262 visible channel wavelengths (nm)
    private double[] wavelengthsNm = {450, 500, 550, 570, 600, 650};
    
    // Pre-calculated frequencies (THz)
    private double[] frequenciesTHz;
    
    // Channel labels
    private static final String[] CHANNEL_LABELS = {"V", "B", "G", "Y", "O", "R"};
    
    public enum PlotType { BAR, CURVE }
    public enum AxisType { WAVELENGTH, FREQUENCY }
    
    public Visualizer(MeasurementSet measurementSet) {
        this.measurementSet = measurementSet;
        calculateFrequencies();
    }
    
    /**
     * Calculate frequencies from wavelengths
     */
    private void calculateFrequencies() {
        frequenciesTHz = new double[wavelengthsNm.length];
        for (int i = 0; i < wavelengthsNm.length; i++) {
            // Convert nm to m, calculate frequency in THz
            double wavelengthM = wavelengthsNm[i] * 1e-9;
            frequenciesTHz[i] = SPEED_OF_LIGHT / wavelengthM / 1e12;
        }
    }

    // ==================== CONFIGURATION ====================

    public void setPlotType(PlotType plotType) { this.plotType = plotType; }
    public void setPlotType(String plotType) {
        this.plotType = plotType.equalsIgnoreCase("bar") ? PlotType.BAR : PlotType.CURVE;
    }
    
    public void setNormalize(boolean normalize) { this.normalize = normalize; }
    public void setShowErrorBars(boolean showErrorBars) { this.showErrorBars = showErrorBars; }
    public void setAxisType(AxisType axisType) { this.axisType = axisType; }
    public void useWavelengthAxis(boolean useWavelength) {
        this.axisType = useWavelength ? AxisType.WAVELENGTH : AxisType.FREQUENCY;
    }

    // ==================== CHART CREATION ====================

    /**
     * Create bar chart for current measurement set
     */
    public CategoryChart createBarChart() {
        MeasurementSet.StatisticsResult stats = measurementSet.getAverageAndStd();
        
        // Prepare data
        PlotData data = preparePlotData(stats.mean, stats.std);
        
        // Create chart
        CategoryChart chart = new CategoryChartBuilder()
            .width(800).height(600)
            .title("Spectrum - " + measurementSet.getName())
            .xAxisTitle(getAxisTitle())
            .yAxisTitle(getYAxisTitle())
            .build();
        
        // Style
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setAvailableSpaceFill(0.5);
        
        // Add series
        if (showErrorBars) {
            chart.addSeries("Intensity", data.labels, data.yValues, data.errorValues);
        } else {
            chart.addSeries("Intensity", data.labels, data.yValues);
        }
        
        return chart;
    }

    /**
     * Create curve chart for current measurement set
     */
    public XYChart createCurveChart() {
        MeasurementSet.StatisticsResult stats = measurementSet.getAverageAndStd();
        
        // Prepare data
        PlotData data = preparePlotData(stats.mean, stats.std);
        
        // Create chart
        XYChart chart = new XYChartBuilder()
            .width(800).height(600)
            .title("Spectrum - " + measurementSet.getName())
            .xAxisTitle(getAxisTitle())
            .yAxisTitle(getYAxisTitle())
            .build();
        
        // Style
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setMarkerSize(6);
        
        // Add series
        if (showErrorBars) {
            chart.addSeries("Intensity", data.xValues, data.yValues, data.errorValues);
        } else {
            chart.addSeries("Intensity", data.xValues, data.yValues);
        }
        
        return chart;
    }

    /**
     * Create absorption chart comparing reference and sample
     */
    public XYChart createAbsorptionChart(MeasurementSet reference, MeasurementSet sample) {
        MeasurementSet.StatisticsResult refStats = reference.getAverageAndStd();
        MeasurementSet.StatisticsResult sampleStats = sample.getAverageAndStd();
        
        // Calculate absorbance
        double[] absorbance = calculateAbsorbance(refStats.mean, sampleStats.mean);
        
        // Get axis values
        double[] axisValues = getAxisValues();
        List<Double> xValues = toList(axisValues);
        List<Double> yValues = toList(absorbance);
        
        // Create chart
        XYChart chart = new XYChartBuilder()
            .width(800).height(600)
            .title("Absorption Spectrum")
            .xAxisTitle(getAxisTitle())
            .yAxisTitle("Absorbance")
            .build();
        
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.addSeries("Absorbance", xValues, yValues);
        
        return chart;
    }

    // ==================== DATA PREPARATION ====================

    /**
     * Container for prepared plot data
     */
    private class PlotData {
        List<String> labels;      // for bar charts
        List<Double> xValues;      // for curve charts
        List<Double> yValues;
        List<Double> errorValues;
    }
    
    /**
     * Prepare data for plotting based on current settings
     */
    private PlotData preparePlotData(double[] mean, double[] std) {
        PlotData data = new PlotData();
        
        // Get axis values
        double[] axisValues = getAxisValues();
        String[] axisLabels = getAxisLabels();
        
        // Apply normalization if requested
        double[] yData = mean;
        double[] errorData = std;
        if (normalize) {
            double max = Arrays.stream(mean).max().orElse(1.0);
            yData = Arrays.stream(mean).map(v -> v / max).toArray();
            errorData = Arrays.stream(std).map(v -> v / max).toArray();
        }
        
        // Prepare data structures
        data.xValues = toList(axisValues);
        data.labels = Arrays.asList(axisLabels);
        data.yValues = toList(yData);
        data.errorValues = toList(errorData);
        
        // Reverse for frequency axis (so lower freq on left)
        if (axisType == AxisType.FREQUENCY) {
            Collections.reverse(data.xValues);
            Collections.reverse(data.labels);
            Collections.reverse(data.yValues);
            Collections.reverse(data.errorValues);
        }
        
        return data;
    }
    
    /**
     * Calculate absorbance A = -log10(I/I0)
     */
    private double[] calculateAbsorbance(double[] reference, double[] sample) {
        int n = Math.min(reference.length, sample.length);
        double[] absorbance = new double[n];
        
        for (int i = 0; i < n; i++) {
            if (reference[i] > 0 && sample[i] > 0) {
                absorbance[i] = -Math.log10(sample[i] / reference[i]);
            } else {
                absorbance[i] = Double.NaN;
            }
        }
        
        return absorbance;
    }

    // ==================== UTILITY METHODS ====================

    private double[] getAxisValues() {
        return axisType == AxisType.WAVELENGTH ? wavelengthsNm : frequenciesTHz;
    }
    
    private String[] getAxisLabels() {
        String[] labels = new String[wavelengthsNm.length];
        if (axisType == AxisType.WAVELENGTH) {
            for (int i = 0; i < wavelengthsNm.length; i++) {
                labels[i] = String.valueOf((int) wavelengthsNm[i]);
            }
        } else {
            for (int i = 0; i < frequenciesTHz.length; i++) {
                labels[i] = String.format("%.1f", frequenciesTHz[i]);
            }
        }
        return labels;
    }
    
    private String getAxisTitle() {
        return axisType == AxisType.WAVELENGTH ? "Wavelength (nm)" : "Frequency (THz)";
    }
    
    private String getYAxisTitle() {
        Map<String, Object> params = measurementSet.getParameters();
        Object mode = params.get("mode");
        String base = (mode != null && mode.toString().equalsIgnoreCase("cal")) 
            ? "Calibrated Intensity" : "Raw Counts";
        return normalize ? base + " (normalized)" : base;
    }
    
    private List<Double> toList(double[] arr) {
        List<Double> list = new ArrayList<>();
        for (double v : arr) list.add(v);
        return list;
    }
}