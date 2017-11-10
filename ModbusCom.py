__author__ = 'davidswed'

import json
import time
import serial
import struct


ser = serial.Serial('/dev/ttyUSB0', 9600, timeout = 0.5)
"""
{SiteID: 0, Registers: [{ID:0, TypeID:0, Interval:0, Message: [3,3,89,43,0,1,244,132], MinDiff:6}], MinimumAPIInterval:3, MaximumAPIInterval:600}
"""
siteInfo = []
registers = []
minInterval = 1
maxInterval = 2
siteID = 0
gotNewConfig = False
stopThread = False


#get data from serial line #TODO reformat for modbus and send an error if no value
def recieveData():
    try:
        rec = ser.read(2)
        x = int(ord(rec[0]) << 8) | int(ord(rec[1]))
        return x
    except serial.SerialException as e:
        #no data recieved
        return -1
    

#load the data from text file
def loadSiteInfo():
    #import global variables
    global siteInfo, registers, minInterval, maxInterval, siteID

    #open file and convert from json
    file = open('/home/pi/Desktop/RemoteSensorPlatform-master/Config/Device_Config.txt', "r")
    
    try:
        siteInfo = json.loads(file.read())
        file.close()
    except ValueError, e:
        print "no valid configuration"
        siteInfo = []
        file.close()
        return

    #load up global info
    registers = siteInfo['Registers']
    minInterval = siteInfo['MinimumAPIInterval']
    maxInterval = siteInfo['MaximumAPIInterval']
    siteID = siteInfo['SiteID']

    #for each register, fill in values
    for reg in registers:
        #add a spot to store values
        reg['lastValue'] = 0

        #add a spot for recording when the register was last read
        reg['lastReadTime'] = int(time.time())

        #convert the message from the configuration to byte array
        reg['convMessage'] = []
        for i in reg['Message']:
            reg['convMessage'].append(struct.pack('>B', i))

def modbusComTask():
    global siteInfo, registers, minInterval, maxInterval, siteID, gotNewConfig, stopThread
    #pause to allow serial to connect properly
    time.sleep(1)

    #load up the initial configuration
    loadSiteInfo()

    #this is the main loop
    while True:
        if stopThread == True:
            stopThread = False
            break;
        if gotNewConfig == True:
            loadSiteInfo()
            gotNewConfig = False
            print "got new config"

        if len(siteInfo) > 0:
            #get the current time
            currentTime = int(time.time())

            #for each register that needs to be read
            for reg in registers:
                #if the register hasn't been read in the minimum interval, get a value
                if currentTime - reg['lastReadTime'] > minInterval:
                    #send the message
                    for m in reg['convMessage']:
                        ser.write(m)

                    #get the data from the register TODO handle error
                    val = recieveData()
                    if val == -1:
                        #report an error
                        print "error reading value"
                    elif abs(reg['lastValue'] - val) > reg['MinDiff']:
                        #TODO report value
                        print "got " + str(val) + " at " + str(currentTime - reg['lastReadTime']) + ' seconds from register ID:' + str(reg['ID'])

                        #store the last value
                        reg['lastValue'] = val

                        #record when it was read
                        reg['lastReadTime'] = currentTime

                #check if the register is over the last interval and send an empty report
                if currentTime - reg['lastReadTime'] > maxInterval:
                    #TODO send an empty report

                    #update the last read time
                    reg['lastReadTime'] = currentTime

        #wait a second before going back throught the loop
        time.sleep(1)
