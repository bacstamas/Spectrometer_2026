# Design and Implementation of the Spectrophotometer
## Hardware

### Idea

The starting point for the hardware design was the desire to build a functional absorption spectrophotometer that was as simple and inexpensive as possible, while remaining open to future refinement and improvement. A key decision in this direction was the choice of the SparkFun [AS7262](https://cdn.sparkfun.com/assets/f/b/c/c/f/AS7262.pdf) Visible Spectral Sensor as the core detection element. This sensor measures light intensity in six fixed channels across the visible spectrum, centered at 450, 500, 550, 570, 600, and 650 nm, and communicates digitally with a microcontroller via I2C. Its main advantage for this application is that it eliminates the need for a diffraction grating and a rotating wavelength selector entirely, the spectral discrimination is handled internally by the sensor itself. This greatly simplifies the optical layout and reduces the number of precision components required, at the cost of spectral resolution, since the instrument can only sample six discrete wavelengths rather than a continuous spectrum.

As the microcontroller, an [Arduino Uno](https://docs.arduino.cc/resources/datasheets/A000066-datasheet.pdf) was chosen. This is a natural pairing with the AS7262 sensor, as the Arduino provides the I2C interface needed to communicate with it, can supply the sensor with power, and can transfer the acquired data to a PC via its USB connection. The Arduino Uno is also widely available, inexpensive, and well documented, making it a suitable platform for an educational instrument.

For the light source, an incandescent bulb was preferred over an LED. While LEDs are cheaper and more energy efficient, their emission spectrum is strongly peaked at specific wavelengths, which can result in uneven illumination across the six detection channels of the sensor. An incandescent bulb, by contrast, emits a broad and relatively smooth blackbody-like spectrum across the entire visible range, providing more uniform illumination of all channels. The chosen bulb is a Goobay product with an E10 socket, rated at 3.5 V and 0.2 A, giving a power consumption of 0.7 W. This low current draw is important, as the 5V power pin of the Arduino Uno can supply a limited current, and exceeding it risks damaging the board.

The optical layout follows a straightforward single-beam transmission configuration. Light from the bulb passes through a vertical slit, which collimates it into a narrow beam. This beam then passes through the cuvette, which holds the sample solution. A second slit placed after the cuvette further restricts the beam before it reaches the AS7262 sensor. Standard commercial cuvettes with a 10×10 mm inner cross section and 1 cm path length were used, consistent with the standard path length assumed in Beer-Lambert law calculations. The structural frame holding all of these components together was designed in 3D modeling software and printed using a high-precision 3D printer with PLA filament, resulting in a compact enclosure slightly smaller than a 6×10×25 cm block.

### Obstacles

he development of the hardware went through two main iterations, driven by a series of practical problems encountered during testing.

The first version of the frame was modular in design, meaning that the positions of the light source, the slits, and the sensor could be adjusted along the optical axis. This was useful in the early stages of development, as it allowed the optimal spacing between components to be found experimentally. However, this first version had no cover or enclosure around the optical path, meaning that ambient light from the room could reach the sensor and contaminate the measurements. As a result, all measurements with this version had to be performed in a darkened room, which was clearly impractical for regular use.

A further issue that emerged during testing was the need to control the intensity of the light source. The AS7262 sensor has a fixed integration time and gain setting for each measurement, and if the light reaching it is too intense, the sensor saturates and the readings become unreliable. A way to reduce the light intensity without modifying the optical layout was therefore needed.

Finally, the PLA filament used for 3D printing presented an unexpected problem. The filament available was blue in color, and it turned out to be partially translucent to ambient light, allowing a non-negligible amount of stray light to penetrate the walls of the enclosure even in the second, closed version of the frame.

### Implementation

The second version of the frame addressed the problems identified during testing with the first. All components (the light source, the two slits, the cuvette holder, and the sensor) were given fixed, precisely dimensioned positions inside a closed enclosure, based on the optimal spacing determined during the first phase. The enclosure included a removable cover to allow access to the cuvette holder for changing samples, while keeping the rest of the optical path shielded from ambient light. A dedicated bay was also added to securely mount the Arduino Uno board, positioned with the USB Type-B connector accessible from the outside of the enclosure, so that the connection to the PC could be made without opening the instrument. Crucially, the Arduino was physically separated from the optical components inside the box, so that the indicator LEDs on the board would not interfere with the sensor readings. The internal structure of the assembled spectrometer is shown in **Figure 5**. 

![Spectrometer structure](/home/tomi/Desktop/Spectrometer_2026/Docs/Figures/box_inside.jpeg)

***Figure 5**: Photo of the inside of the spectrometer.*

To solve the problem of stray light penetrating through the blue PLA walls, the exterior and interior surfaces of the enclosure were painted with a matte black paint of the type used industrially to produce surfaces with near-unity emissivity for thermal imaging applications. This paint effectively blocked the residual light transmission through the plastic walls and also minimized internal reflections within the enclosure.

The light intensity control problem was solved by adding a [BD139](https://share.google/5DOaTHuip5NTmbAFO) NPN bipolar transistor to the circuit. The transistor was wired with its collector connected to the Arduino's 5V supply pin, its emitter connected to the positive terminal of the light bulb, and the negative terminal of the bulb connected to the Arduino's ground. The base of the transistor was driven by pin 3 of the Arduino, which is a PWM-capable output. By varying the duty cycle of the PWM signal, the effective voltage across the bulb can be adjusted continuously, allowing the light intensity to be tuned to a level that keeps the sensor within its linear operating range without saturating it. This simple addition significantly improved the reliability and reproducibility of the measurements. See **Figure 6** for the circuit diagram.

![Spectrometer structure](/home/tomi/Desktop/Spectrometer_2026/Docs/Figures/circuit_design_1.png)

***Figure 6**: Photo of the inside of the spectrometer.*

Several sub-components, including the sensor holder, the cuvette holder, and the box cover, went through additional minor refinements beyond the two main frame iterations, as small geometric adjustments were needed to improve the fit and reduce stray light leakage around the edges. The final assembled instrument is a compact, self-contained device that can be connected to any computer via a standard USB cable and operated without any need for a darkened environment.

## Software

The software component of the spectrophotometer system consists of two separate programs: the firmware running on the Arduino microcontroller, which is responsible for directly operating the sensor and acquiring data, and a Java desktop application running on the PC, which serves as the interface between the user and the instrument. The two communicate through a serial connection over USB.

The **Arduino firmware** is intentionally kept simple and lightweight. On startup, it initializes the AS7262 sensor and sets a default configuration for the measurement parameters. It then enters a waiting loop, listening for commands arriving through the serial port.

When a measurement is requested, the command arrives as a formatted string specifying all relevant parameters: integration time, gain, number of averages, measurement mode, and light intensity. The firmware parses this string, applies the specified settings to the sensor, and adjusts the PWM signal on pin 3 to set the light source intensity accordingly. It then performs the measurement the requested number of times and computes the average across repetitions for each of the six spectral channels. The result is sent back to the PC as a single comma-separated line of six floating point values, one per channel.

The AS7262 sensor library supports two measurement modes: a raw mode, which returns the unprocessed digital counts from each photodetector channel, and a calibrated mode, which applies internal corrections to return values in units of calibrated irradiance. Both modes are accessible through the firmware and can be selected by the user from the PC software.

The **PC-side software** is written in Java, a choice motivated by several practical considerations. Java is inherently cross-platform, the same application can run on Linux, Windows, and macOS without significant modification, and it lends itself naturally to well-structured, object-oriented design that can be extended and maintained over time. The application was developed and tested on Linux but is intended to be portable across operating systems. It uses the *jSerialComm* library for serial port communication, the *XChart* library for data visualization and the *Jackson* library for handling JSON files.

The architecture of the application is organized around a small set of well-defined classes, each with a clear responsibility. This separation of concerns makes the codebase easy to navigate and extend without requiring a complete redesign.

### Structure

The *Spectrometer* class is the core of the application. It is responsible for all communication with the Arduino hardware. On instantiation, it automatically scans the available serial ports on the system (using the *jSerialComm* library) and identifies the connected Arduino by checking port descriptors and system names. Once connected, it configures the serial port parameters (baud rate, data bits, stop bits, parity) opens the connection, and flushes any startup messages sent by the Arduino. It exposes methods to configure the measurement parameters and to trigger measurements, handling the sending of the formatted command string and the reception and parsing of the response. Each call to the measurement method can request multiple spectra in sequence, each acquired with on-board averaging by the Arduino.

The *MeasurementSet* class acts as a container for all data associated with a single acquisition session. It stores the list of individual spectra recorded during that session, the full set of measurement parameters used, and a unique name composed of a user-defined label and the date and time of acquisition. It provides methods for computing the mean and standard deviation across the recorded spectra for each channel, for formatting this information as a readable string, and for saving the entire measurement set to a JSON file or loading it back from one. JSON format was chosen because it is human-readable and easy to handle.

The *Visualizer* class handles all data visualization. It takes a *MeasurementSet* as input and can produce two types of charts: a spectrum plot showing the intensity values across the six channels of the sensor, and an absorption spectrum computed from two separate measurement sets representing the reference and the sample. The absorbance at each channel is calculated according to the Beer-Lambert law as:

$$A=-\log_{10} \left(\frac{I_{sample}}{I_{reference}}\right)$$

where $I_{sample}$ and $I_{reference}$ are the mean intensities of the sample and reference measurements at each channel respectively. The *Visualizer* class uses the *XChart* library to produce the actual chart objects, which are then embedded in the GUI.

The remaining classes handle the graphical user interface. The *Main* class serves as the application entry point, launching the GUI on the appropriate thread. The *MainWindow* class implements the main application window, while the remaining classes manage the panels, menus, and dialogs, and handle specific user interactions.

### Features

The main window of the application is organized around a central list that displays all measurement sets currently loaded in memory, each identified by its unique name. From the main window, the user can connect to the spectrometer and configure the measurement parameters through a dedicated dialog, which provides input fields for integration time, gain, number of averages, number of measurements, measurement mode, and light intensity. Once configured, a measurement can be triggered with a single button press. The acquired measurement set is automatically added to the list and displayed in a text area showing its parameters and statistics.

Measurement sets can be saved to and loaded from plain text files, allowing results to be stored for later analysis or shared between sessions. The user can also load previously saved measurements without connecting to the hardware, making it possible to perform data analysis and visualization independently of the instrument.

For visualization, two separate plot types are available. The spectrum plot displays the raw or calibrated intensity values across the six sensor channels for a selected measurement set, while the absorption plot computes and displays the absorbance spectrum from a user-selected reference and sample pair. Both plot types offer configurable options accessible through dialog windows: the x-axis can be displayed in wavelength (nm) or in frequency (THz), the y-axis values can be shown as raw values or normalized to the maximum, the plot style can be switched between a line curve and a bar chart, and error bars representing the standard deviation across repeated measurements can be toggled on or off.

### Data Processing and Calibration

The data processing performed by the software is deliberately straightforward, reflecting the educational purpose of the instrument. The primary computation is the averaging of repeated spectra, which is handled partly by the Arduino firmware, which averages multiple sensor readings within a single acquisition, and partly by the *MeasurementSet* class, which computes the mean and standard deviation across multiple sequential acquisitions requested by the user. This two-level averaging scheme gives the user flexibility in balancing measurement speed against statistical reliability.

The absorbance calculation follows directly from the Beer-Lambert law, as described above. No additional baseline correction or spectral smoothing is applied, keeping the processing chain transparent and easy to follow.

## Experimental Results and Validation

With the hardware and software components completed, a series of experiments was carried out to characterize the performance of the instrument and to validate its suitability for quantitative absorbance measurements.

### Stray Light Test

The first test performed was aimed at verifying that the closed enclosure of the spectrophotometer effectively excludes ambient light. An empty cuvette was placed in the cuvette holder, the cover of the box was closed, and the light source intensity was set to 0, meaning the bulb was switched off via the PWM control. Under these conditions, repeated measurements returned a value of exactly zero on each of the six channels of the sensor. This result confirms that no stray or ambient light reaches the detector when the light source is off, validating the effectiveness of the matte black coating applied to the interior of the enclosure and ruling out the presence of any light leaks through the housing.

### Reproducibility Test

To assess the reproducibility of the measurements, a solution of yellow food coloring at a concentration of $2.5\%$ was prepared and measured repeatedly. Ten independent measurement sets were acquired, each consisting of 100 individual measurements per channel (integration time: 100 ms, gain: 16, averaging: 1, mode: raw, light intensity: 100). All ten sets were plotted together, as shown in **Figure 7**, where each of the six channels is represented by ten overlapping traces of 100 points each.

![Spectrometer structure](/home/tomi/Desktop/Spectrometer_2026/Docs/Figures/Reproductibility_plot.png)

***Figure 7**: Raw measurement values recorded across the six channels of the AS7262 sensor (450, 500, 550, 570, 600, and 650 nm) for ten independently acquired measurement sets of a $2.5\%$ yellow food coloring solution, each consisting of 100 individual measurements.*

The plot shows that the measured values remain clustered around a stable baseline for each channel, with some random scatter present throughout the dataset. Averaging across all 1000 individual measurements per channel (10 sets × 100 measurements), the mean raw values obtained were $\{3.99, 14.251, 33.428, 50.629, 11.336, 24.334\}$ for the $450,\ 500,\ 550,\ 570,\ 600,\ \text{and}\ 650\ \text{nm}$ channels respectively, with corresponding standard deviations of $\{0.099,\ 0.522,\ 1.101,\ 2.004,\ $ $0.501,\ 0.696\}$. The relative standard deviation remains below approximately $4\%$ for every channel, which indicates a satisfactory level of reproducibility for an instrument of this cost and complexity. This noise may be attributable to small imperfections such as scratches on the cuvette surface and slight variations in its exact positioning within the holder between insertions.

### Long-Term Stability Test

A further test was conducted to check whether the solution itself remained stable over time, an important consideration since sedimentation or settling of the food coloring could introduce a systematic drift in absorbance unrelated to the instrument's performance. The same yellow food coloring solution and the same measurement parameters used in the reproducibility test were employed. A cuvette containing the solution was measured once, left undisturbed inside the instrument for 24 hours, and then measured again without being touched or repositioned in between. The difference between the two measurements was found to be smaller than the standard deviations already observed in the reproducibility test, indicating that no significant settling or degradation of the solution occurred over this timescale, and that any short-term drift in the instrument's readings is dominated by the handling-related noise identified above rather than by sample instability.

### Calibration Curves

Finally, the linearity of the instrument's response was tested by preparing a series of yellow food coloring solutions at concentrations ranging from $0.8\%$ to $10\%$, and measuring the absorbance of each at all six sensor channels. The resulting calibration curves are shown in **Figure 8** (one subplot for each channel) together with a linear best-fit line.

![Spectrometer structure](/home/tomi/Desktop/Spectrometer_2026/Docs/Figures/Calibration_curves.png)

***Figure 7**: Six calibration curves showing absorbance against concentration ($\%$), along with their corresponding best-fit lines, at each of the sensor's wavelengths.*

Across all six channels, the measured absorbance values follow the linear trend predicted by the Beer-Lambert law reasonably well, particularly in the central concentration range. Some scatter around the fitted line is visible, which is consistent with the absolute measurement noise identified in the reproducibility test. The channels at $550$, $570$, and $600\ \text{nm}$, which are closer to the absorption maximum of the yellow dye, display the clearest linear behavior and the largest overall absorbance range, while the $450$ nm channel, which lies further from the absorption peak, shows comparatively more scatter relative to its smaller dynamic range. Overall, these results confirm that the constructed instrument is capable of producing calibration curves with a clearly recognizable linear character across the entire visible range covered by the sensor, supporting its intended use for quantitative concentration measurements in an educational context.

