#include <Wire.h>
#include "AS726X.h"

AS726X sensor;
String command = "";
const int BULB_PIN = 3;

// -------- DEFAULTS --------
int defaultIntegration = 50;   // ms
int defaultGain = 16;          // 1, 3, 16, 64
int defaultAvg = 1;            // number of measurements
bool defaultCalibrated = true;
int defaultLightInt = 50; // 0..100


void setup() {
  Serial.begin(115200);
  Wire.begin();

  if (!sensor.begin()) {
    while (1); // Sensor not found
  }

  pinMode(BULB_PIN, OUTPUT);
  int pwm = map(defaultLightInt, 0, 100, 0, 255);
  analogWrite(BULB_PIN, pwm);

  sensor.disableIndicator();
  sensor.disableBulb();

  Serial.println("READY");
}

void loop() {
  while (Serial.available()) {
    char c = Serial.read();
    if (c == '\n' || c == '\r') {
      handleCommand(command);
      command = "";
    } else {
      command += c;
    }
  }
}

// ---------------- COMMAND HANDLING ----------------

void handleCommand(String cmd) {
  cmd.trim();
  if (!cmd.startsWith("read")) return;

  // Start with defaults
  int integration = defaultIntegration;
  int gain = defaultGain;
  int avg = defaultAvg;
  bool calibrated = defaultCalibrated;
  int lightInt = defaultLightInt;

  // Remove "read"
  cmd = cmd.substring(4);
  cmd.trim();

  // Parse key=value pairs
  while (cmd.length() > 0) {
    int spaceIndex = cmd.indexOf(' ');
    String token;

    if (spaceIndex == -1) {
      token = cmd;
      cmd = "";
    } else {
      token = cmd.substring(0, spaceIndex);
      cmd = cmd.substring(spaceIndex + 1);
      cmd.trim();
    }

    int eqIndex = token.indexOf('=');
    if (eqIndex == -1) continue;

    String key = token.substring(0, eqIndex);
    String value = token.substring(eqIndex + 1);

    key.trim();
    value.trim();

    if (key == "int") {
      integration = value.toInt();
    } else if (key == "gain") {
      gain = value.toInt();
    } else if (key == "avg") {
      avg = value.toInt();
      if (avg < 1) avg = 1;
    } else if (key == "mode") {
      calibrated = (value == "cal");
    } else if (key == "light") {
      lightInt = value.toInt();
      if (lightInt < 0) lightInt = 0;
      if (lightInt > 100) lightInt = 100;
    }
  }

  // Apply settings
  sensor.setIntegrationTime(integration);
  uint8_t gainCode;
  if (gain <= 1) {
      gainCode = 0;            // 1x
  } else if (gain <= 4) {
      gainCode = 1;            // ~3.7x
  } else if (gain <= 16) {
      gainCode = 2;            // 16x
  } else {
      gainCode = 3;            // 64x
  }
  sensor.setGain(gainCode);

  int pwm = map(lightInt, 0, 100, 0, 255);
  analogWrite(BULB_PIN, pwm);

  // Take measurements
  takeAveragedMeasurement(avg, calibrated);
}

// ---------------- MEASUREMENT ----------------

void takeAveragedMeasurement(int avg, bool calibrated) {

  float sum[6] = {0, 0, 0, 0, 0, 0};

  for (int i = 0; i < avg; i++) {
    sensor.takeMeasurements();

    if (calibrated) {
      sum[0] += sensor.getCalibratedViolet();
      sum[1] += sensor.getCalibratedBlue();
      sum[2] += sensor.getCalibratedGreen();
      sum[3] += sensor.getCalibratedYellow();
      sum[4] += sensor.getCalibratedOrange();
      sum[5] += sensor.getCalibratedRed();
    } else {
      sum[0] += sensor.getViolet();
      sum[1] += sensor.getBlue();
      sum[2] += sensor.getGreen();
      sum[3] += sensor.getYellow();
      sum[4] += sensor.getOrange();
      sum[5] += sensor.getRed();
    }
  }

  // Output CSV
  for (int i = 0; i < 6; i++) {
    float value = sum[i] / avg;
    Serial.print(value, 3);
    if (i < 5) Serial.print(",");
  }
  Serial.println();
}
