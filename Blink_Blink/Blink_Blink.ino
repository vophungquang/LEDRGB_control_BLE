#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

hw_timer_t * timer = NULL;

const int LED = GPIO_NUM_2; // on board blue led. (pin24) 
const char ledR = A4;
const char ledG = A5;
const char ledB = A18;

uint8_t ledArray[3] = {1, 2, 3};
unsigned char hue[][3] = {
  {255, 0, 0}, 
  {255, 64, 0},
  {255, 128, 0},
  {255, 191, 0},
  {255, 255, 0},
  {191, 255, 0},
  {128, 255, 0},
  {64, 255, 0},
  {0, 255, 0},
  {0, 255, 64},
  {0, 255, 128},
  {0, 255, 191},
  {0, 255, 255},
  {0, 191, 255},
  {0, 128, 255},
  {0, 64, 255},
  {0, 0, 255},
  {64, 0, 255},
  {128, 0, 255},
  {191, 0, 255},
  {255, 0, 255},
  {255, 0, 191},
  {255, 0, 128},
  {255, 0, 64},
  };

bool deviceConnected = false; // connection-status flag

String receivedCommand = ""; // commands via bluetooth stored here

//#define SERVICE_UUID           "6E400001-B5A3-F393-E0A9-E50E24DCCA9E" // UART service UUID
//#define CHARACTERISTIC_UUID_RX "4529357d-6659-4495-8082-f8b55cedfadb"
//#define CHARACTERISTIC_UUID_TX "6E400003-B5A3-F393-E0A9-E50E24DCCA9E"

#define SERVICE_UUID              "0000ffe0-0000-1000-8000-00805f9b34fb"
#define CHARACTERISTIC_UUID_RX    "0000ffe1-0000-1000-8000-00805f9b34fb"

void IRAM_ATTR onTimer(){
    //static byte state = LOW;
    //state =! state;
    //digitalWrite(LED, state);

    static char i = 0;
    if(++i > 23) i = 0;
    apply_color(hue[i][0],hue[i][1],hue[i][2]);
    
}

class MyServerCallbacks: public BLEServerCallbacks {
  void onConnect(BLEServer* pServer) {
      deviceConnected = true;
      blink_1();
      Serial.println("connnected to a device!");
    }
  void onDisconnect(BLEServer* pServer) {
      deviceConnected = false;
      Serial.println("disconnected!");
    }
};

class MyCallbacks: public BLECharacteristicCallbacks {    
  void onWrite(BLECharacteristic *pCharacteristic) {
      std::string rxValue = pCharacteristic->getValue();
      if (rxValue.length() > 0) {
        Serial.println("Received a command");
        receivedCommand = rxValue.c_str();
      }
    }
};

void setup(){
  // Create the BLE Device
  BLEDevice::init("NHOM 8 JAVA");
  
  // Create the BLE Server
  BLEServer *pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());

  // Create the BLE Service
  BLEService *pService = pServer->createService(SERVICE_UUID);

  // Create a BLE 
  /*BLECharacteristic *pCharacteristic = pService->createCharacteristic(
                      CHARACTERISTIC_UUID_TX,
                      BLECharacteristic::PROPERTY_NOTIFY
                    );
  pCharacteristic->addDescriptor(new BLE2902());*/
  BLECharacteristic *pCharacteristic = pService->createCharacteristic(
                                         CHARACTERISTIC_UUID_RX,
                                         BLECharacteristic::PROPERTY_WRITE
                                       );
  pCharacteristic->setCallbacks(new MyCallbacks());

  // Start the service
  pService->start();
  
  // Start advertising
  pServer->getAdvertising()->start();

  timer = timerBegin(0, 80, true);
  timerAttachInterrupt(timer, &onTimer, true);
  timerAlarmWrite(timer, 100000, true);
  
  Serial.begin(115200);
  Serial.println("Waiting a client connection to notify...");
  pinMode(LED,OUTPUT);

  ledcAttachPin(ledR, 1); // assign RGB led pins to channels
  ledcAttachPin(ledG, 2);
  ledcAttachPin(ledB, 3);

  ledcSetup(1, 12000, 8); // 12 kHz PWM, 8-bit resolution
  ledcSetup(2, 12000, 8);
  ledcSetup(3, 12000, 8);
  
}

void loop() {
  if(receivedCommand.length() > 0)
  {
    if(receivedCommand == "BL1")
    {
      blink_1();
    }
    
    else if(receivedCommand == "ONLED" || receivedCommand == "1")
    {
      digitalWrite(LED, HIGH);
    }
    
    else if(receivedCommand == "OFFLED" || receivedCommand == "0")
    {
      digitalWrite(LED, LOW);
    }
    
    else if(receivedCommand == "BLN")
    {
      timerAlarmEnable(timer);
    }
    
    else if(receivedCommand == "SBL")
    {
      timerAlarmDisable(timer);
    }

    else if (receivedCommand.substring(0,2) == "ND")
    {
      int time = receivedCommand.substring(2,receivedCommand.length()).toInt();
      timerAlarmWrite(timer, time, true);
    }
    
    else
    {
      change_color(receivedCommand);
    }
    
    receivedCommand = "";
   }
  delay(10);
}

void change_color(String cmd)
{
  int R = 0, G = 0, B = 0;
  if(cmd.substring(0,3) == "RGB")
  {
    R = cmd.substring(3,6).toInt();
    G = cmd.substring(6,9).toInt();
    B = cmd.substring(9,12).toInt();
    apply_color(R,G,B);
  }
  else
    Serial.println("Invalid command!");
    Serial.println(cmd);
}

void apply_color(unsigned char R, unsigned char G, unsigned char B)
{
  ledcWrite(1,R);
  ledcWrite(2,G);  
  ledcWrite(3,B);
  
  Serial.println("LED's color changed!");
}

void blink_1()
{
  Serial.println("Received a blinking require command!");
  digitalWrite(LED,HIGH);
  delay(300);
  digitalWrite(LED,LOW);
  delay(300);
  digitalWrite(LED,HIGH);
  delay(300);
  digitalWrite(LED,LOW);
  delay(300);
}
