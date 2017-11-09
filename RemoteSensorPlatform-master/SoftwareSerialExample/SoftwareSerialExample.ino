
/*
  Software serial multple serial test

 Receives from the hardware serial, sends to software serial.
 Receives from software serial, sends to hardware serial.

 The circuit:
 * RX is digital pin 10 (connect to TX of other device)
 * TX is digital pin 11 (connect to RX of other device)

 Note:
 Not all pins on the Mega and Mega 2560 support change interrupts,
 so only the following can be used for RX:
 10, 11, 12, 13, 50, 51, 52, 53, 62, 63, 64, 65, 66, 67, 68, 69

 Not all pins on the Leonardo support change interrupts,
 so only the following can be used for RX:
 8, 9, 10, 11, 14 (MISO), 15 (SCK), 16 (MOSI).

 created back in the mists of time
 modified 25 May 2012
 by Tom Igoe
 based on Mikal Hart's example

 This example code is in the public domain.

 */
#include <SoftwareSerial.h>

// rx and tx for communicating with rs-485
SoftwareSerial mySerial(10, 11); // RX, TX

void setup() {
  // Open serial communications and wait for port to open:
  Serial.begin(9600);
  while (!Serial) {
    ; // wait for serial port to connect. Needed for native USB port only
  }


  //Serial.println("Test Serial line!");

  // set the data rate for the SoftwareSerial port
  mySerial.begin(9600);

  delay(500);
}

void loop() { // run over and over
  static int numBytes = 0;
  static char received[30];
  static char functionCode;
  static char myAddress = 3;
  static int expectedBytes;
  
  if (mySerial.available()) {
    char charVal[2];
    char newByte = mySerial.read();
    
    //split into hex values
    sprintf(charVal, "%02X", newByte);
    
    //print the received byte
    Serial.write(charVal[0]);
    Serial.write(charVal[1]);
    //mySerial.write(newByte);
    
    //place received byte into buffer
    received[numBytes] = newByte;

    //do something depending on which byte this is
    switch (numBytes) {
      case 0: {
        //slave address
        /*if (newByte == myAddress) {
          //Serial.println("found address");
          numBytes++;
        }*/
        //TODO switch back to modbus above
        numBytes++;
        break;
      }

      case 1: {
        //function code
        numBytes++;
        functionCode = newByte;

        //switch based on the function code
        switch (functionCode) {
          case 3: {
              //Serial.println("read register function");
              expectedBytes = 8;
              break;
          }
          case 16: {
              //Serial.println("write register function");
              // write register
              // TODO this will be dynamic
              expectedBytes = 11;
              break;
          }

          default: {
              //unknown function code
              numBytes = 0;
              break;
          }
        }
        
        break;
      }

      default: {
        numBytes++;
        
        if (numBytes >= expectedBytes) {
          char message[30];
          
          switch (functionCode) {
            case 3: {
                //read register
                message[0] = myAddress;
                message[1] = functionCode;

                //number of bytes to send
                message[2] = 0x02;
                int randNum = random(0, 50);
                //the return value
                message[3] = 0x00;
                message[4] = (unsigned char)randNum & 0xFF;

                mySerial.write(message[3]);
                mySerial.write(message[4]);
                //SendMessage(message, 5);
                break;
            }
            case 16: {
                //Serial.println("responding to write register");

                message[0] = myAddress;
                message[1] = functionCode;

                // the starting register address
                message[2] = received[2];
                message[3] = received[3];

                // the number of registers written to
                message[4] = received[4];
                message[5] = received[5];

                SendMessage(message, 6);
                break;
            }
  
            default: {
                //unknown function code
                break;
            }
          }

          numBytes = 0;
        } 
        break;
      }
    }
    
  }
  if (Serial.available()) {
    Serial.read();
    Serial.write('d');
  }
}

// function send the given message (buf) of byte length len
// with a crc
void SendMessage(char * buf, int len)
{
  unsigned short checkSum = ModRTU_CRC(buf, len);
  
  for (int i = 0; i < len; i++) {
      mySerial.write(buf[i]);
  }

  mySerial.write((char)(checkSum & 0x00FF));
  mySerial.write((char)((checkSum & 0xFF00) >> 8));
}

// function to generate the Modbus CRC
unsigned short ModRTU_CRC(char * buf, int len)
{
  unsigned short crc = 0xFFFF;
 
  for (int pos = 0; pos < len; pos++) {
    crc ^= (uint16_t)buf[pos];          // XOR byte into least sig. byte of crc
 
    for (int i = 8; i != 0; i--) {    // Loop over each bit
      if ((crc & 0x0001) != 0) {      // If the LSB is set
        crc >>= 1;                    // Shift right and XOR 0xA001
        crc ^= 0xA001;
      }
      else                            // Else LSB is not set
        crc >>= 1;                    // Just shift right
    }
  }
  // Note, this number has low and high bytes swapped, so use it accordingly (or swap bytes)
  return crc;  
}


