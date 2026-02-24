import com.fazecast.jSerialComm.SerialPort;
import java.io.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Spectrometer {

    private SerialPort port;
    private String portName;

    private int integrationTime = 50;
    private int gain = 16;
    private int avg = 1;
    private String mode = "cal";
    private int numberOfMeasurements = 1;
    private int lightInt = 50;

    private MeasurementSet measurementSet = new MeasurementSet();
    private Map<String, Object> params = new HashMap<>();

    // ---------- CONSTRUCTOR ----------
    public Spectrometer() throws Exception {

        try {
            System.out.println("=== SPECTROMETER DEBUG 1: START ===");
            
            System.out.println("Step 1: Finding ports...");
            SerialPort[] ports = SerialPort.getCommPorts();
            System.out.println("PORTS LENGTH: " + ports.length);
            
            if (ports.length == 0) {
                System.out.println("NO PORTS FOUND - jSerialComm failed");
                throw new Exception("No serial ports detected");
            }
            
            for (int i = 0; i < ports.length; i++) {
                System.out.println("Port " + i + ": " + 
                    ports[i].getSystemPortName() + 
                    " | " + ports[i].getDescriptivePortName());
            }
            
            System.out.println("Step 2: Calling findArduinoPort...");

            // 1. Find Arduino port
            port = findArduinoPort();
            if (port == null) {
                System.out.println("findArduinoPort returned NULL");
                throw new Exception("Arduino not found");
            }
            System.out.println("Arduino found: " + port.getSystemPortName());

            // 2. Configure serial port
            port.setBaudRate(115200);
            port.setNumDataBits(8);
            port.setNumStopBits(SerialPort.ONE_STOP_BIT);
            port.setParity(SerialPort.NO_PARITY);

            // IMPORTANT: non-blocking mode (no read timeouts)
            port.setComPortTimeouts(
                SerialPort.TIMEOUT_READ_SEMI_BLOCKING | SerialPort.TIMEOUT_WRITE_BLOCKING,
                5000,  // read timeout
                5000   // write timeout
            );

            // 3. Open port
            if (!port.openPort()) {
                throw new Exception("Failed to open serial port");
            }

            // 4. Wait for Arduino auto-reset to finish
            Thread.sleep(2000);

            // 5. Flush any startup garbage (e.g. READY, boot noise)
            InputStream in = port.getInputStream();
            byte[] flushBuffer = new byte[256];
            while (port.bytesAvailable() > 0) {
                in.read(flushBuffer, 0, Math.min(flushBuffer.length, port.bytesAvailable()));
                Thread.sleep(10);
            }

            System.out.println("Connected to " + port.getSystemPortName());
            portName=port.getSystemPortName();

            // Initialize parameter map with defaults
            params.put("integrationTime", integrationTime);
            params.put("gain", gain);
            params.put("avg", avg);
            params.put("mode", mode);
            params.put("numberOfMeasurements", numberOfMeasurements);
            params.put("lightInt", lightInt);
        } catch (Exception e) {
            System.out.println("=== SPECTROMETER EXCEPTION: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }


    // ---------- CONFIGURATION ----------
    public void configure(int integrationTime,
                          int gain,
                          int avg,
                          String mode,
                          int numberOfMeasurements,
                          int lightInt) {

        this.integrationTime = integrationTime;
        this.gain = gain;
        this.avg = avg;
        this.mode = mode;
        this.numberOfMeasurements = numberOfMeasurements;
        this.lightInt = lightInt;

        params.put("integrationTime", integrationTime);
        params.put("gain", gain);
        params.put("avg", avg);
        params.put("mode", mode);
        params.put("numberOfMeasurements", numberOfMeasurements);
        params.put("lightInt", lightInt);
    }


    // ---------- MEASUREMENT ----------
    public void measure(String baseName) throws Exception {

        DateTimeFormatter fmt =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

        String timestamp = LocalDateTime.now().format(fmt);
        String fullName = baseName + "_" + timestamp;

        measurementSet = new MeasurementSet();
        measurementSet.setParameters(params);
        measurementSet.setName(fullName);

        OutputStream out = port.getOutputStream();
        InputStream in = port.getInputStream();

        byte[] buffer = new byte[1024];
        StringBuilder lineBuffer = new StringBuilder();

        for (int i = 0; i < numberOfMeasurements; i++) {

            String command = String.format(
                "read int=%d gain=%d avg=%d mode=%s light=%d\n",
                integrationTime, gain, avg, mode, lightInt
            );

            out.write(command.getBytes());
            out.flush();

            long startTime = System.currentTimeMillis();
            boolean received = false;

            while (System.currentTimeMillis() - startTime < 5000) {

                int available = port.bytesAvailable();
                if (available <= 0) {
                    Thread.sleep(5);
                    continue;
                }

                int n = in.read(buffer, 0, Math.min(buffer.length, available));
                if (n <= 0) continue;

                for (int j = 0; j < n; j++) {
                    char c = (char) buffer[j];

                    if (c == '\n') {
                        String line = lineBuffer.toString().trim();
                        lineBuffer.setLength(0);

                        if (!line.contains(",")) continue;

                        try {
                            double[] spectrum = parseCSV(line);
                            measurementSet.addMeasurement(spectrum);
                            received = true;
                            break;
                        } catch (Exception ignored) {}
                    } else if (c != '\r') {
                        lineBuffer.append(c);
                    }
                }

                if (received) break;
            }

            if (!received) {
                throw new Exception("Timeout waiting for measurement");
            }
        }
    }


    private double[] parseCSV(String line) throws Exception {
        String[] tokens = line.split(",");
        if (tokens.length != 6) {
            throw new Exception("Invalid data");
        }

        double[] values = new double[6];
        for (int i = 0; i < 6; i++) {
            values[i] = Double.parseDouble(tokens[i]);
        }
        return values;
    }

    // ---------- ACCESS ----------
    public MeasurementSet getMeasurementSet() {
        return measurementSet;
    }

    public String getPortName() {
        return portName;
    }

    public void close() {
        if (port != null && port.isOpen()) {
            port.closePort();
        }
    }

    private SerialPort findArduinoPort() {
        for (SerialPort p : SerialPort.getCommPorts()) {
            String name = p.getDescriptivePortName().toLowerCase();
            if (name.contains("arduino") ||
                p.getSystemPortName().contains("ttyACM") ||
                p.getSystemPortName().contains("usb")) {
                return p;
            }
        }

        // DEBUG: print ALL available ports
        System.err.println("=== DEBUG: No Arduino found. Available ports ===");
        for (SerialPort p : SerialPort.getCommPorts()) {
            System.err.println("Port: " + p.getSystemPortName() + 
                              " | Desc: " + p.getDescriptivePortName());
        }
        System.err.println("=== END DEBUG ===");
        return null;
    }
}
