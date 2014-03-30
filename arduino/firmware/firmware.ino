#include <SoftwareSerial.h>
#include <Adafruit_MAX31855.h>

#define rxPin 3
#define txPin 4

SoftwareSerial serial(rxPin, txPin);
Adafruit_MAX31855 thermocouple(2, 1, 0);

void setup() {
  serial.begin(9600);
  delay(2000);
}

void loop() {
  int data = thermocouple.readCelsius();
  serial.println(data);
  delay(1000);
}
