package core;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.*;
import java.io.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Main controller class for spectrometer hardware communication.
 * Handles serial port connection, configuration, and data acquisition
 * for the AS726x spectrometer sensor connected via Arduino.
 * 
 * @author Spectrometer Control Software
 * @version 1.0
 */
public class Spectrometer {

    // Serial communication
    private SerialPort port;
    private String portName;
    
    // Measurement parameters with defaults
    private int integrationTime = 50;      // ms
    private int gain = 16;                  // 1, 4, 16, 64
    private int avg = 1;                     // number of samples to average
    private String mode = "cal";             // "raw" or "cal"
    private int numberOfMeasurements = 1;    // number of spectra to record
    private int lightInt = 50;                // LED intensity (0-100)
    
    // Data storage
    private MeasurementSet measurementSet = new MeasurementSet();
    private Map<String, Object> params = new HashMap<>();

    private ConnectionListener connectionListener;
    private volatile boolean disconnected = false;

    /**
     * Listener interface for connection events.
     */
    public interface ConnectionListener {
        /**
         * Called when the serial port is disconnected.
         */
        void onDisconnected();
    }

    /**
     * Constructor - establishes connection with Arduino/spectrometer.
     * 
     * @param listener listener for disconnection events
     * @throws Exception if connection fails (port not found or cannot open)
     */
    public Spectrometer(ConnectionListener listener) throws Exception {
        this.connectionListener = listener;
        try {
            System.out.println("Initializing spectrometer connection...");
            
            // Find and configure Arduino port
            port = findArduinoPort();
            if (port == null) {
                throw new Exception("Arduino not found - check USB connection");
            }
            
            configureSerialPort();
            initializeConnection();
            
            // Store port name for display
            portName = port.getSystemPortName();
            
            // Initialize parameter map with defaults
            initializeParameters();
            setupDisconnectListener();

            System.out.println("Successfully connected to " + portName);
            
        } catch (Exception e) {
            //System.err.println("Connection failed: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Sets up a listener for serial port disconnection events.
     */
    private void setupDisconnectListener() {
        port.addDataListener(new SerialPortDataListener() {

            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_PORT_DISCONNECTED;
            }

            @Override
            public void serialEvent(SerialPortEvent event) {
                if (event.getEventType() == SerialPort.LISTENING_EVENT_PORT_DISCONNECTED
                    && !disconnected) {

                    disconnected = true;

                    System.out.println("Serial port disconnected!");
                    port.closePort();

                    if (connectionListener != null) {
                        connectionListener.onDisconnected();
                    }
                }
            }
        });
    }
    
    /**
     * Configure serial port parameters (baud rate, data bits, stop bits, parity, timeouts).
     */
    private void configureSerialPort() {
        port.setBaudRate(115200);
        port.setNumDataBits(8);
        port.setNumStopBits(SerialPort.ONE_STOP_BIT);
        port.setParity(SerialPort.NO_PARITY);
        
        // Set timeouts: non-blocking read with 5s timeout
        port.setComPortTimeouts(
            SerialPort.TIMEOUT_READ_SEMI_BLOCKING | SerialPort.TIMEOUT_WRITE_BLOCKING,
            5000,  // read timeout
            5000   // write timeout
        );
    }
    
    /**
     * Open port and flush startup messages from Arduino.
     * 
     * @throws Exception if port cannot be opened
     */
    private void initializeConnection() throws Exception {
        if (!port.openPort()) {
            throw new Exception("Failed to open serial port");
        }
        
        // Wait for Arduino auto-reset
        Thread.sleep(2000);
        
        // Flush any startup garbage
        InputStream in = port.getInputStream();
        byte[] flushBuffer = new byte[256];
        while (port.bytesAvailable() > 0) {
            in.read(flushBuffer, 0, 
                Math.min(flushBuffer.length, port.bytesAvailable()));
            Thread.sleep(10);
        }
    }
    
    /**
     * Configure measurement parameters.
     * 
     * @param integrationTime integration time in milliseconds
     * @param gain gain value (1, 4, 16, or 64)
     * @param avg number of samples to average
     * @param mode measurement mode ("raw" or "cal")
     * @param numberOfMeasurements number of spectra to record
     * @param lightInt LED intensity (0-100)
     */
    public void configure(int integrationTime, int gain, int avg, 
                         String mode, int numberOfMeasurements, int lightInt) {
        this.integrationTime = integrationTime;
        this.gain = gain;
        this.avg = avg;
        this.mode = mode;
        this.numberOfMeasurements = numberOfMeasurements;
        this.lightInt = lightInt;
        
        // Update parameter map with consistent keys
        params.put("integrationTime", integrationTime);
        params.put("gain", gain);
        params.put("avg", avg);
        params.put("mode", mode);
        params.put("numberOfMeasurements", numberOfMeasurements);
        params.put("lightInt", lightInt);
    }

    /**
     * Initialize parameter map with current default values.
     */
    private void initializeParameters() {
        params.put("integrationTime", integrationTime);
        params.put("gain", gain);
        params.put("avg", avg);
        params.put("mode", mode);
        params.put("numberOfMeasurements", numberOfMeasurements);
        params.put("lightInt", lightInt);
    }

    /**
     * Perform measurement with current configuration.
     * 
     * @param baseName base name for the measurement set (timestamp will be appended)
     * @throws Exception if measurement fails or times out
     */
    public void measure(String baseName) throws Exception {
        // Create timestamped name
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String timestamp = LocalDateTime.now().format(fmt);
        String fullName = baseName + "_" + timestamp;
        
        // Prepare measurement set
        measurementSet = new MeasurementSet();
        measurementSet.setParameters(params);
        measurementSet.setName(fullName);
        
        OutputStream out = port.getOutputStream();
        InputStream in = port.getInputStream();
        
        byte[] buffer = new byte[1024];
        StringBuilder lineBuffer = new StringBuilder();
        
        for (int i = 0; i < numberOfMeasurements; i++) {
            // Send command to Arduino
            String command = String.format(
                "read int=%d gain=%d avg=%d mode=%s light=%d\n",
                integrationTime, gain, avg, mode, lightInt
            );
            out.write(command.getBytes());
            out.flush();
            
            // Calculate appropriate timeout based on parameters
            // Base time: integrationTime * avg (total measurement time)
            // Add 2 seconds for communication and processing overhead
            int totalMeasurementTimeMs = integrationTime * avg;
            int timeoutMs = totalMeasurementTimeMs*12 + 2000; // Add 2 seconds buffer
            timeoutMs = Math.max(5000, timeoutMs); // At least 5 seconds minimum
            
            System.out.println("Measurement " + (i+1) + "/" + numberOfMeasurements + 
                              " - Timeout set to: " + timeoutMs + "ms");
            
            long startTime = System.currentTimeMillis();
            boolean received = false;
            
            while (System.currentTimeMillis() - startTime < timeoutMs) {
                int available = port.bytesAvailable();
                if (available <= 0) {
                    // Print progress for long measurements
                    if (totalMeasurementTimeMs > 5000 && (System.currentTimeMillis() - startTime) % 1000 == 0) {
                        System.out.print(".");
                    }
                    Thread.sleep(10);
                    continue;
                }
                
                int n = in.read(buffer, 0, Math.min(buffer.length, available));
                if (n <= 0) continue;
                
                // Parse incoming data
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
                            System.out.println(" Received measurement " + (i+1));
                            break;
                        } catch (Exception ignored) {}
                    } else if (c != '\r') {
                        lineBuffer.append(c);
                    }
                }
                
                if (received) break;
            }
            
            if (!received) {
                throw new Exception("Timeout waiting for measurement " + (i+1) + 
                                  " after " + timeoutMs + "ms");
            }
        }
    }

    /**
     * Parse CSV line into double array.
     * Expects 6 values for AS726x sensor (450, 500, 550, 570, 600, 650 nm).
     * 
     * @param line CSV string containing 6 comma-separated values
     * @return double array of parsed values
     * @throws Exception if format is invalid
     */
    private double[] parseCSV(String line) throws Exception {
        String[] tokens = line.split(",");
        if (tokens.length != 6) {
            throw new Exception("Invalid data format");
        }
        
        double[] values = new double[6];
        for (int i = 0; i < 6; i++) {
            values[i] = Double.parseDouble(tokens[i]);
        }
        return values;
    }

    /**
     * Find Arduino port by checking common identifiers.
     * 
     * @return the first matching SerialPort, or null if none found
     */
    private SerialPort findArduinoPort() {
        for (SerialPort p : SerialPort.getCommPorts()) {
            String desc = p.getDescriptivePortName().toLowerCase();
            String sysName = p.getSystemPortName().toLowerCase();
            
            if (desc.contains("arduino") || 
                sysName.contains("ttyacm") || 
                sysName.contains("usb") ||
                sysName.contains("com") && desc.contains("serial")) {
                return p;
            }
        }
        return null;
    }

    /**
     * Gets the current measurement set.
     * 
     * @return the MeasurementSet containing all recorded data
     */
    public MeasurementSet getMeasurementSet() { return measurementSet; }
    
    /**
     * Gets the serial port name.
     * 
     * @return the system port name (e.g., "COM3" or "/dev/ttyACM0")
     */
    public String getPortName() { return portName; }
    
    /**
     * Close serial port connection.
     */
    public void close() {
        if (port != null && port.isOpen()) {
            port.closePort();
            System.out.println("Serial port closed");
        }
    }
}