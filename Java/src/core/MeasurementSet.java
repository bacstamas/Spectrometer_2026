package core;

import java.util.*;
import java.io.*;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * Container for measurement data with metadata.
 * Handles storage, statistics, and file I/O for spectrometer measurements.
 */
public class MeasurementSet {

    private List<double[]> measurements;
    private Map<String, Object> parameters;
    private String name;
    
    // Constants
    private static final int EXPECTED_CHANNELS = 6;
    private static final String[] WAVELENGTHS = {
        "450 nm", "500 nm", "550 nm", "570 nm", "600 nm", "650 nm"
    };

    public MeasurementSet() {
        measurements = new ArrayList<>();
        parameters = new HashMap<>();
    }

    // ==================== METADATA ====================

    public void setParameters(Map<String, Object> params) {
        parameters.clear();
        parameters.putAll(params);
    }

    public void setName(String name) { this.name = name; }
    public String getName() { return name; }
    public Map<String, Object> getParameters() { return new HashMap<>(parameters); }

    // ==================== DATA MANAGEMENT ====================

    public void addMeasurement(double[] measurement) {
        if (measurement.length != EXPECTED_CHANNELS) {
            throw new IllegalArgumentException(
                "Expected " + EXPECTED_CHANNELS + " channels, got " + measurement.length);
        }
        measurements.add(measurement.clone());
    }

    public List<double[]> getMeasurements() { 
        return Collections.unmodifiableList(measurements);
    }

    public void clear() {
        measurements.clear();
        parameters.clear();
        name = null;
    }

    public boolean isEmpty() { return measurements.isEmpty(); }
    public int size() { return measurements.size(); }

    // ==================== STATISTICS ====================

    /**
     * Calculate mean and standard deviation across all measurements
     */
    public StatisticsResult getAverageAndStd() {
        if (measurements.isEmpty()) {
            return new StatisticsResult(new double[0], new double[0]);
        }

        int n = measurements.size();
        int channels = measurements.get(0).length;
        double[] mean = new double[channels];
        double[] std = new double[channels];

        // Calculate mean
        for (double[] m : measurements) {
            for (int i = 0; i < channels; i++) {
                mean[i] += m[i];
            }
        }
        for (int i = 0; i < channels; i++) {
            mean[i] /= n;
        }

        // Calculate standard deviation
        for (double[] m : measurements) {
            for (int i = 0; i < channels; i++) {
                double diff = m[i] - mean[i];
                std[i] += diff * diff;
            }
        }
        for (int i = 0; i < channels; i++) {
            std[i] = Math.sqrt(std[i] / n);
        }

        return new StatisticsResult(mean, std);
    }

    /**
     * Get formatted statistics as string
     */
    public String getStatisticsString() {
        StatisticsResult stats = getAverageAndStd();
        StringBuilder sb = new StringBuilder();
        
        sb.append("\nWavelength\tAverage\tStdDev\n");
        int n = Math.min(stats.mean.length, WAVELENGTHS.length);
        for (int i = 0; i < n; i++) {
            sb.append(WAVELENGTHS[i])
              .append("\t")
              .append(String.format("%.3f", stats.mean[i]))
              .append("\t")
              .append(String.format("%.3f", stats.std[i]))
              .append("\n");
        }
        return sb.toString();
    }

    // ==================== FILE I/O ====================

    /**
     * Save measurement set to file
     */
    public void saveToFile(String filename) throws IOException {
        com.fasterxml.jackson.databind.ObjectMapper mapper =
                new com.fasterxml.jackson.databind.ObjectMapper();

        Map<String, Object> jsonMap = new HashMap<>();

        jsonMap.put("name", name);
        jsonMap.put("parameters", parameters);
        jsonMap.put("measurements", measurements);

        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(filename), jsonMap);
    }

    /**
     * Load measurement set from file
     */
    public static MeasurementSet loadFromFile(String filename) throws IOException {
        com.fasterxml.jackson.databind.ObjectMapper mapper =
                new com.fasterxml.jackson.databind.ObjectMapper();

        Map<String, Object> jsonMap =
                mapper.readValue(new File(filename), Map.class);

        MeasurementSet set = new MeasurementSet();

        // Name
        Object nameObj = jsonMap.get("name");
        if (nameObj != null) {
            set.setName(nameObj.toString());
        }

        // Parameters
        Object paramsObj = jsonMap.get("parameters");
        if (paramsObj instanceof Map) {
            Map<?, ?> rawParams = (Map<?, ?>) paramsObj;
            for (Map.Entry<?, ?> e : rawParams.entrySet()) {
                set.parameters.put(e.getKey().toString(), e.getValue());
            }
        }

        // Measurements
        Object measObj = jsonMap.get("measurements");
        if (measObj instanceof List) {
            List<?> list = (List<?>) measObj;

            for (Object row : list) {
                List<?> values = (List<?>) row;
                double[] arr = new double[values.size()];

                for (int i = 0; i < values.size(); i++) {
                    arr[i] = ((Number) values.get(i)).doubleValue();
                }

                set.addMeasurement(arr);
            }
        }

        return set;
    }

    // ==================== TO STRING ====================

    /**
     * Returns a formatted string representation of the measurement set.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        if (name != null) {
            sb.append("=== ").append(name).append(" ===\n\n");
        }
        
        sb.append("PARAMETERS:\n");
        appendParameter(sb, "integrationTime", "Integration time", " ms");
        appendParameter(sb, "gain", "Gain", "");
        appendParameter(sb, "avg", "Averaging", "");
        appendParameter(sb, "numberOfMeasurements", "Measurements", "");
        appendParameter(sb, "mode", "Mode", "");
        appendParameter(sb, "lightInt", "Light intensity", "");
        
        if (!measurements.isEmpty()) {
            sb.append("\nSTATISTICS:\n");
            StatisticsResult stats = getAverageAndStd();
            
            // Wavelength labels for AS726x visible channels
            String[] wavelengths = {"450 nm", "500 nm", "550 nm", "570 nm", "600 nm", "650 nm"};
            
            sb.append(String.format("%-12s %12s %12s\n", "Wavelength", "Average", "StdDev"));
            sb.append("------------------------------------------------\n");
            
            int n = Math.min(stats.mean.length, wavelengths.length);
            for (int i = 0; i < n; i++) {
                sb.append(String.format("%-12s %12.3f %12.3f\n", 
                    wavelengths[i], stats.mean[i], stats.std[i]));
            }
            
            sb.append("\nRAW DATA (").append(measurements.size()).append(" spectra):\n");
            for (double[] m : measurements) {
                for (double v : m) {
                    sb.append(String.format("%8.3f ", v));
                }
                sb.append("\n");
            }
        }
        
        return sb.toString();
    }

    /**
     * Helper method to safely append a parameter to string builder.
     */
    private void appendParameter(StringBuilder sb, String key, String label, String unit) {
        Object value = parameters.get(key);
        if (value != null) {
            sb.append(String.format("  %-20s: %s%s\n", label, value.toString(), unit));
        } else {
            sb.append(String.format("  %-20s: %s\n", label, "Not set"));
        }
    }
    // ==================== INNER CLASS ====================

    /**
     * Container for statistical results
     */
    public static class StatisticsResult {
        public final double[] mean;
        public final double[] std;
        
        public StatisticsResult(double[] mean, double[] std) {
            this.mean = mean;
            this.std = std;
        }
    }
}