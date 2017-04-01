#include "simpletools.h"
#define uint8 unsigned char
#define AD7993BRUZ_0    (0x21)  // Don't shift addresses, it's done by simpletools
#define AD7993BRUZ_1    (0x23)  //
#define VTHIGH 3800
#define VTLOW 3700
#define T_THRESHOLD 4000
#define ADC_PIN 0
#define PIN_OUT 7

///////////////////////////////////////////////////////////////////
// ADC routines for BOE
///////////////////////////////////////////////////////////////////
i2c *adcBus;                    // I2C bus ID
uint8 adcAddr;                  // Address for this module
uint8 addList[5]= { 0x20, 0x21, 0x22, 0x23, 0x24 }; //I2C bus addresses for ADC
int sensor_value;
int Count;
unsigned long time0, t_pressed_ms;

int AD7993start(unsigned int) ;
int AD7993inV(int) ;
int AD7993in(int);

int main(void) 
{
  uint8 i;
  pause(200);
  AD7993start(0);   //Init i2c bus and address
  Count =0;
  set_direction(PIN_OUT, 1);

  while(1)
  {
    Count =0;

    sensor_value = AD7993inV(ADC_PIN);
    printf("Count = %d \n", Count);
    printf("Sensor Voltage Output1 = %d \n", sensor_value);

    while (sensor_value < VTHIGH)
    {
      sensor_value = AD7993inV(ADC_PIN);
          printf("Count = %d \n", Count);
          printf("Sensor Voltage Output = %d \n", sensor_value);
          pause(100);
    };
    //  time0 = millis();
    set_output(PIN_OUT,1);
    while (sensor_value > VTLOW)
    {
      sensor_value = AD7993inV(ADC_PIN);
      printf("Sensor Voltage Output = %d \n", sensor_value);
      printf("Gripper Pressed \n");
      pause(100);
    };

    //t_pressed_ms = t_pressed_ms + (millis()-time0);

    Count = Count + 1;
    pause(102);
    set_output(PIN_OUT,0);
  }

  return 0;
}



//-------------------------------------------------------------------------
// Get ADC value in mV (0-5000)
//-------------------------------------------------------------------------
int AD7993inV(int channel) 
{
  return (5000 - (AD7993in(channel) * 5000 / 1023));
}


//-------------------------------------------------------------------------
// Get ADC value for channel in binary form (0-1023)
//-------------------------------------------------------------------------
int AD7993in(int channel) 
{
  uint8 adcVal[2];

  
  i2c_out(adcBus, adcAddr, 1<<((channel&3)+4), 1,NULL, 0); //Set channel, mem ptr and start convert
  i2c_in( adcBus, adcAddr, 0, 0, adcVal, 2);           //Get 2 bytes from adc
  
  return ((adcVal[0] & 0xf) << 6) | (adcVal[1] >> 2);
}

//-------------------------------------------------------------------------
// Init I2C bus and find/define the AD7993 address
// Returns: 1=ok
//          0=chip not found, address defaulted
//-------------------------------------------------------------------------
int AD7993start(unsigned int address) 
{
  uint8 i, t;


  adcBus = i2c_newbus(28,  29,   0);           // Set up I2C bus, get bus ID
  if (address == 0) { //Scan bus for AD7993 chips
    for (i=0; i<sizeof(addList); i++) {
      if (i2c_in(adcBus, addList[i], 0, 0, &t, 1) == 2) {
        adcAddr = addList[i];
        //printf("AD7993 found on 0x%X\n", adcAddr);
        return 1;
      }
    }
  } else {
    if (i2c_in(adcBus, address, 0, 0, &t, 1) == 2) {
      adcAddr = address;
      //printf("AD7993 found on 0x%X\n", adcAddr);
      return 1;
    }
  }
  adcAddr = AD7993BRUZ_0;   //force address if everything else failed
  //printf("AD7993 defaulted to 0x%X\n",adcAddr);
  return 0; //return in error
}