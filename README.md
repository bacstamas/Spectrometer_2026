
# DIY Spectrometer – Master’s Thesis Project

**Author:** Bács Tamás  
**Program:** Computational Physics MSc  
**University:** Babeș-Bolyai University (UBB), Faculty of Physics  

---

## 📌 Project Overview
This repository contains the complete hardware and software implementation of a low-cost DIY spectrometer. The project is developed as part of my **Master’s Thesis in Computational Physics at UBB**.

The spectrometer is built using an Arduino-based acquisition system, a multispectral sensor, custom electronics, and 3D-printed mechanical parts, with a Java-based desktop application for data visualization and analysis.

---

## 📂 Repository Structure
```
Arduino/        -> Arduino firmware for sensor acquisition and control
Java/           -> Java desktop application (GUI, data processing, visualization)
3DModels/        
 ├── FreeCAD/    -> Editable CAD source files (.FCStd)
 ├── STL/        -> 3D printable models
 └── UFP/        -> Additional fabrication/export formats
Docs/            -> Additional documents, such as images
README.md        -> Project documentation (this file)
.gitignore       -> Git ignore rules
```

---

## 🔧 Required Hardware Components
To build the spectrometer, you need the following components:

### 🧠 Electronics
- **Arduino Uno** – microcontroller for sensor control and data acquisition
- **SparkFun AS7265x Spectral Sensor** – multispectral sensor (visible)
- **BD139 NPN Transistor** – used for driving the light source
- **Light bulb with socket** – approx. **0.2 A at 5 V** (used as broadband illumination source)

### 🖨️ Mechanical Parts
- **3D printed enclosure**  
  (All CAD designs and STL files are included in the `3DModels/` directory.)
- **Spectrophotometer cuvettes**
---

## ⚡ Circuit Diagram
The following circuit is used to drive the illumination source and interface the AS7265x sensor with the Arduino:


![Spectrometer Circuit Diagram](/Docs/circuit_design_1.png)

### Arduino Uno to AS7265x (I2C)

| Arduino Uno Pin | AS7265x Pin | Description |
|-----------------|-------------|-------------|
| 5V              | VIN / 5V     | Sensor power supply |
| GND             | GND          | Common ground |
| A4              | SDA          | I2C data line |
| A5              | SCL          | I2C clock line |

### Arduino Uno to BD139 Transistor (Light Bulb Driver)

| Arduino Uno Pin | Component | Description |
|-----------------|-----------|-------------|
| D3 (PWM)         | BD139 Base | Controls light intensity using PWM |
| 5V               | BD139 Collector | Light source supply |
| Light bulb +  | BD139 Emitter | Current flows through bulb |
| Light bulb −    | GND         | Common system ground |

---

## 🚀 How to Use
1. **Hardware Assembly**
   - Print the 3D models from the `3DModels/STL` folder.
   - Assemble the optical and electronic components according to the circuit diagram.

2. **Arduino Firmware**
   - Open the Arduino project in the `Arduino/` folder.
   - Upload the firmware to the Arduino Uno.

3. **Java Application**
   - Compile and run the Java GUI from the `Java/` folder.
   - The application reads spectral data from the Arduino and visualizes it.
   - Detailed documentation for the Java application can be found here: [Java App Documentation](https://bacstamas.github.io/Spectrometer_2026/Java/docs/index.html).

---

## 🎓 Academic Context
This project is developed as part of a **Master’s Thesis in Computational Physics** at **Babeș-Bolyai University (UBB), Faculty of Physics**.  
The goal is to design a low-cost spectrometer system and analyze its performance, calibration, and computational data processing methods.

---

## 📧 Contact
Bács Tamás  
tamas.bacs@stud.ubbcluj.ro  
Computational Physics MSc Student  
Babeș-Bolyai University

