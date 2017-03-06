/* Mechatronics : Smart Gym Equipments
*  Hall effect sensor */
/*Analog output with no applied field, calibrate this*/

#define NOFIELD 505L  /*#define TOMILLIGAUSS 3756L  /*For A1302: 1.3mV = 1Gauss, and 1024 analog steps = 5V, so 1 step = 3756mG*/
#define VTHIGH 650
#define VTLOW 530
#define T_THRESHOLD 4000

/*Pick one up today in the adafruit shop!

Adafruit invests time and resources providing this open source code,
please support Adafruit and open-source hardware by purchasing
products from Adafruit!

MIT license, check LICENSE for more information
All text above, and the splash screen below must be included in
any redistribution
*********************************************************************/

#include <string.h>
#include <Arduino.h>
#include <SPI.h>
#if not defined (_VARIANT_ARDUINO_DUE_X_) && not defined (_VARIANT_ARDUINO_ZERO_)
#include <SoftwareSerial.h>
#endif

#include "Adafruit_BLE.h"
#include "Adafruit_BluefruitLE_SPI.h"
#include "Adafruit_BluefruitLE_UART.h"

#include "BluefruitConfig.h"

unsigned long time0;
unsigned long t_pressed_ms;
int button_pin = 2;
int Count = 0;
int Count_Speed = 0;
int hall_pin = A0;
int sensor_value = 0;
unsigned long time1;
String count_string="";
String time_pressed="";
boolean begin_count=true;
String temp;
/*=========================================================================
APPLICATION SETTINGS
/*=========================================================================
APPLICATION SETTINGS

    FACTORYRESET_ENABLE       Perform a factory reset when running this sketch
     
                                   Enabling this will put your Bluefruit LE module
                   in a 'known good' state and clear any config
                   data set in previous sketches or projects, so
                                                 running this at least once is a good idea.
                                      
                                                                  When deploying your project, however, you will
                                                  want to disable factory reset by setting this
                                                  value to 0.  If you are making changes to your
                                                                                Bluefruit LE device via AT commands, and those
                                                                  changes aren't persisting across resets, this
                                                                  is the reason why.  Factory reset will erase
                                                                  the non-volatile memory where config data is
                                                                  stored, setting it back to factory default
                                                                  values.
                                                                         
                                                                                                   Some sketches that require you to bond to a
                                                                                     central device (HID mouse, keyboard, etc.)
                                                                                     won't work at all with this feature enabled
                                                                                     since the factory reset will clear all of the
                                                                                     bonding data stored on the chip, meaning the
                                                                                     central device won't be able to reconnect.
                                                                                     MINIMUM_FIRMWARE_VERSION  Minimum firmware version to have some new features
                                                                                     MODE_LED_BEHAVIOUR        LED activity, valid options are
                                                                                     "DISABLE" or "MODE" or "BLEUART" or
                                                                                     "HWUART"  or "SPI"  or "MANUAL"
                                                                                     -----------------------------------------------------------------------*/
#define FACTORYRESET_ENABLE         1
#define MINIMUM_FIRMWARE_VERSION    "0.6.6"
#define MODE_LED_BEHAVIOUR          "MODE"
                                                                                     /*=========================================================================*/
int go = 1;
// Create the bluefruit object, either software serial...uncomment these lines
/*
SoftwareSerial bluefruitSS = SoftwareSerial(BLUEFRUIT_SWUART_TXD_PIN, BLUEFRUIT_SWUART_RXD_PIN);

Adafruit_BluefruitLE_UART ble(bluefruitSS, BLUEFRUIT_UART_MODE_PIN,
BLUEFRUIT_UART_CTS_PIN, BLUEFRUIT_UART_RTS_PIN);
*/
//////////////////Button//////////////
//////////////////Button//////////////
//pinMode(button_pin , INPUT) ;
//digitalWrite(button_pin , HIGH) ; /* turn on the pullup resistor */

//////////////////Sensor_Count////////////
/* ...hardware SPI, using SCK/MOSI/MISO hardware SPI pins and then user selected CS/IRQ/RST */
Adafruit_BluefruitLE_SPI ble(BLUEFRUIT_SPI_CS, BLUEFRUIT_SPI_IRQ, BLUEFRUIT_SPI_RST);

//Serial.begin(115200);      /*set the baurd rate 9600*/

bool getUserInput(char buffer[], uint8_t maxSize)
{
  // timeout in 100 milliseconds
  TimeoutTimer timeout(100);

  memset(buffer, 0, maxSize);
  while ((!Serial.available()) && !timeout.expired()) { delay(1); }

  if (timeout.expired()) return false;

  delay(2);
  uint8_t count = 0;
  do
  {
    count += Serial.readBytes(buffer + count, maxSize);
    delay(2);
  } while ((count < maxSize) && (Serial.available()));

  return true;
}
void error(const __FlashStringHelper*err) {
  Serial.println(err);
  while (1);
}


// Variables for reading transfer bits
// function prototypes over in packetparser.cpp
uint8_t readPacket(Adafruit_BLE *ble, uint16_t timeout);
float parsefloat(uint8_t *buffer);
void printHex(const uint8_t * data, const uint32_t numBytes);

// the packet buffer
extern uint8_t packetbuffer[];
//Servo servoL;
//Servo servoR;
//int weight;
//String weight_string;
//Hx711 scale(A2,A3);
/**************************************************************************/
/*!
@brief  Sets up the HW an the BLE module (this function is called
automatically on startup)
*/
/**************************************************************************/
void Hall_input()
{
  /* detect  magnetic field */
  sensor_value = analogRead(hall_pin);   // Range : 0..1024
                       //Serial.println(sensor_value);
}

void setup(void)
{


  while (!Serial);  // required for Flora & Micro
  delay(500);

  Serial.begin(9600);
  Serial.println(F("Adafruit Bluefruit Command Mode Example"));
  Serial.println(F("---------------------------------------"));

  /* Initialise the module */
  Serial.print(F("Initialising the Bluefruit LE module: "));

  if (!ble.begin(VERBOSE_MODE))
  {
    error(F("Couldn't find Bluefruit, make sure it's in CoMmanD mode & check wiring?"));
  }
  Serial.println(F("OK!"));

  if (FACTORYRESET_ENABLE)
  {
    /* Perform a factory reset to make sure everything is in a known state */
    Serial.println(F("Performing a factory reset: "));
    if (!ble.factoryReset()) {
      error(F("Couldn't factory reset"));
    }
  }

  /* Disable command echo from Bluefruit */
  ble.echo(false);

  Serial.println("Requesting Bluefruit info:");
  /* Print Bluefruit information */
  ble.info();

  //  Serial.println(F("Please use Adafruit Bluefruit LE app to connect in UART mode"));
  //  Serial.println(F("Then Enter characters to send to Bluefruit"));
  //  Serial.println();

  ble.verbose(false);  // debug info is a little annoying after this point!

  Serial.println(F("Setting maximum range!"));
  ble.sendCommandCheckOK("AT+BLEPOWERLEVEL=4");
             /* Wait for connection */
//  while (!ble.isConnected()) {
//    delay(200);
//  }

  // LED Activity command is only supported from 0.6.6
  if (ble.isVersionAtLeast(MINIMUM_FIRMWARE_VERSION))
  {
    // Change Mode LED Activity
    Serial.println(F("******************************"));
    Serial.println(F("Change LED activity to " MODE_LED_BEHAVIOUR));
    ble.sendCommandCheckOK("AT+HWModeLED=" MODE_LED_BEHAVIOUR);
  }

  Serial.println(F("Switching to DATA mode!"));
  ble.setMode(BLUEFRUIT_MODE_DATA);

  Serial.println(F("********************************************"));

}

void loop()
{ Count=0;
  Serial.println(begin_count);
  if(begin_count)
  {
    //Hall Effect Sensor input code
  Hall_input();
  Serial.println(sensor_value);
  //delay(200);
//  Count=0;
  while (sensor_value < VTHIGH)
  { Hall_input();
  };
  time0=millis();
    while (sensor_value > VTLOW)
    {
      Hall_input();
      Serial.print("Gripper Pressed");
    };
    t_pressed_ms = t_pressed_ms + (millis()-time0);

    Count = Count + 1;


//
  count_string= String(abs(Count));
  time_pressed= String(abs(t_pressed_ms));
  Serial.print("\n Count :");
  Serial.print(Count);
  Serial.print("\n");
  ble.println("count<"+count_string+">");
  ble.println("TP<"+time_pressed+">");
  ble.waitForOK();
 }


  delay(40);


//  char inputs[BUFSIZE + 1];
//  if (getUserInput(inputs, BUFSIZE))
//  {
//    // Send characters to Bluefruit
//    Serial.print("[Send] ");
//    Serial.println(inputs);
//
//    ble.print("AT+BLEUARTTX=");
//    ble.println(inputs);
//
//    // check response stastus
//    if (!ble.waitForOK()) {
//      Serial.println(F("Failed to send?"));
//    }
//  }
//
//
//// Check for incoming characters from Bluefruit
//
//Serial.println(ble.buffer);
//temp=ble.buffer;
//Serial.println("Temp variable value is <"+temp+">");
//  if (temp == "Timer started") {
////    begin_count=true;
//    Count=0;
//  }
//
//  if (temp == "Timer stopped") {
////    begin_count=false;
//    Count=0;
//    t_pressed_ms=0;
//  }
//  Serial.print(F("[Recv] ")); Serial.println(ble.buffer);

}


/**************************************************************************/
/*!
@brief  Checks for user input (via the Serial Monitor)
*/
/**************************************************************************/

