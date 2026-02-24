import java.util.*;
import java.io.*;

public class MeasurementSet {

    private List<double[]> measurements;
    private HashMap<String, Object> parameters;
    private String name;

    public MeasurementSet() {
        measurements = new ArrayList<>();
        parameters = new HashMap<>();
    }

    // ---------- PARAMETERS ----------

    public void setParameters(Map<String, Object> params) {
        parameters.clear();
        parameters.putAll(params);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Map<String, Object> getParameters() {
        return new HashMap<>(parameters);
    }

    // ---------- MEASUREMENTS ----------

    public void addMeasurement(double[] measurement) {
        measurements.add(measurement);
    }

    public List<double[]> getMeasurements() {
        return measurements;
    }

    public void clearMeasurements() {
        measurements.clear();
        name = null;
    }

    public void clearAll() {
        measurements.clear();
        parameters.clear();
        name = null;
    }


    // ---------- STATISTICS ----------

    public StatisticsResult getAverageAndStd() {

        if (measurements.isEmpty()) {
            return new StatisticsResult(new double[0], new double[0]);
        }

        int n = measurements.size();
        int channels = measurements.get(0).length;

        double[] mean = new double[channels];
        double[] std = new double[channels];

        // Mean
        for (double[] m : measurements) {
            for (int i = 0; i < channels; i++) {
                mean[i] += m[i];
            }
        }
        for (int i = 0; i < channels; i++) {
            mean[i] /= n;
        }

        // Standard deviation
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

    // ---------- STRING OUTPUT ----------

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (name != null) {
            sb.append("Measurement name: ").append(name).append("\n");
        }

        sb.append("Parameters:\n");

        Object intTime = parameters.get("integrationTime");
        Object gain = parameters.get("gain");
        Object avg = parameters.get("avg");
        Object count = parameters.get("numberOfMeasurements");

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


        sb.append("\nMeasurements:\n");
        for (double[] m : measurements) {
            for (double v : m) {
                sb.append(String.format("%8.3f ", v));
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    public void saveToFile(String filename) throws IOException {

        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {

            pw.println("# MeasurementSet");

            if (name != null) {
                pw.println("name=" + name);
            }
            pw.println();

            pw.println("[param]");
            for (Map.Entry<String, Object> e : parameters.entrySet()) {
                pw.println(e.getKey() + "=" + e.getValue());
            }
            pw.println();

            pw.println("[data]");
            for (double[] m : measurements) {
                for (int i = 0; i < m.length; i++) {
                    pw.print(m[i]);
                    if (i < m.length - 1) pw.print(",");
                }
                pw.println();
            }
        }
    }

    public static MeasurementSet loadFromFile(String filename)
            throws IOException {

        MeasurementSet set = new MeasurementSet();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {

            String line;
            String section = "";

            while ((line = br.readLine()) != null) {

                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                if (line.equals("[param]")) {
                    section = "param";
                    continue;
                }
                if (line.equals("[data]")) {
                    section = "data";
                    continue;
                }

                if (line.startsWith("name=")) {
                    set.setName(line.substring(5));
                    continue;
                }

                if (section.equals("param")) {
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        set.parameters.put(parts[0], parts[1]);
                    }
                }

                if (section.equals("data")) {
                    String[] parts = line.split(",");
                    double[] values = new double[parts.length];
                    for (int i = 0; i < parts.length; i++) {
                        values[i] = Double.parseDouble(parts[i]);
                    }
                    set.addMeasurement(values);
                }
            }
        }

        return set;
    }


    // ---------- HELPER CLASS ----------

    public static class StatisticsResult {
        public final double[] mean;
        public final double[] std;

        public StatisticsResult(double[] mean, double[] std) {
            this.mean = mean;
            this.std = std;
        }
    }
}
