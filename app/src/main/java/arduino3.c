/* Mechatronics : Smart Gym Equipments -2
*  Arduino Recieve module  */
/*Analog output with no applied field, calibrate this*/
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
#define PIN_IN 2
#define RES_FAC_MYO 0.00488
#define ARRAY_SIZE 10
unsigned int myo_sen=0;
float myo_sen_volt;
float myo_sen_squared;
float myo_wind[ARRAY_SIZE];
int Count;
int sensor_value = 0;
unsigned long time1;
String count_string="";
static char myo_wind_avg_char[15];
String myo_wind_avg_string="";
String samples_string="";
String temp;
void call_count();
unsigned long int samples;
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

/**************************************************************************/
/*!
@brief  Sets up the HW an the BLE module (this function is called
automatically on startup)
*/
/**************************************************************************/

void setup(void)
{
  pinMode(PIN_IN, INPUT_PULLUP);
  attachInterrupt(digitalPinToInterrupt(PIN_IN), call_count, RISING);
  Count =0;
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
{
  float myo_wind_avg = 0;
  myo_sen = analogRead(A0);
  samples++;
  myo_sen_volt = (myo_sen - 50)*RES_FAC_MYO ;
  myo_sen_squared = myo_sen_volt*myo_sen_volt;
  
  for(int i=ARRAY_SIZE-1;i>0;i--)
  {
    myo_wind[i] = myo_wind[i-1];
  }
  myo_wind[0] = myo_sen_squared;

  for(int i=ARRAY_SIZE-1;i>=0;i--)
  {
    myo_wind_avg = myo_wind_avg + myo_wind[i];
  }

  myo_wind_avg = myo_wind_avg/ARRAY_SIZE;

  Serial.println("Myo Average Squared Output = ");
  Serial.println(myo_wind_avg);

  
  Serial.print("\n");
  Serial.print("Count = ");
  Serial.print(Count);
  Serial.print("\n");
  Serial.print("Samples = ");
  Serial.print(samples);
  Serial.print("\n");
  count_string= String(abs(Count));
  
  dtostrf(myo_wind_avg,7, 3,  myo_wind_avg_char);
  strcat(myo_wind_avg_char,'\0');
  myo_wind_avg_string= String((myo_wind_avg));
  Serial.println(myo_wind_avg_string);
  samples_string= String(abs(samples));
  ble.println("count<"+count_string+">");
  delay(5);
  ble.flush();
  delay(5);
  ble.println("myo<"+myo_wind_avg_string+">");
  delay(5);
  ble.flush();
  delay(5);
  ble.println("samples<"+samples_string+">");
  delay(5);
  // ble.waitForOK();
  ble.flush();
  delay(200);
}

void call_count()
{
  Count++;
}
