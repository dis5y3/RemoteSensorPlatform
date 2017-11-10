import os
import time
## import ModbusCom
from bluetooth import *
from threading import Thread
class bcolors:
    HEADER = '\033[95m'
    BLUE = '\033[94m'
    GREEN = '\033[92m'
    YELLOW = '\033[93m'
    RED = '\033[91m'
    ENDC = '\033[0m'
    BOLD = '\033[1m'
    UNDERLINE = '\033[4m'

base_dir = '/home/pi/Desktop/RemoteSensorPlatform-master'
device_file = base_dir + '/Config/Device_Config.txt'

#comment the next two lines out if you do not have USB to R485 dongle
modbusThread = Thread(target = modbusCom.modbusComTask)
modbusThread.start()

while True:
    try:
        server_sock=BluetoothSocket( RFCOMM )
        server_sock.bind(("",PORT_ANY))
        server_sock.listen(1)
        
        port = server_sock.getsockname()[1]
        
        uuid = "2ffc3a4f-b5d1-4ed2-bead-adba7328232d"
        
        advertise_service( server_sock, "AmpliServer",
                           service_id = uuid,
                           service_classes = [ uuid, SERIAL_PORT_CLASS ],
                           profiles = [ SERIAL_PORT_PROFILE ], 
        #                   protocols = [ OBEX_UUID ] 
                            )         
        
        print bcolors.BOLD + "Waiting for connection on RFCOMM channel %d" % port + bcolors.ENDC
        
        client_sock, client_info = server_sock.accept()
        print bcolors.BOLD + "Accepted connection from " + bcolors.BLUE + "{}".format(client_info) + bcolors.ENDC
        
        #Work on code to send existing data to app here
        
        old_file_object = open(device_file,"r")
        existing_data = old_file_object.readlines()
        data_str = " ,".join(str(item) for item in existing_data)
        old_file_object.close
        
        client_sock.send("[" + data_str + "]")
        print bcolors.BOLD + "sending " + bcolors.YELLOW + "[%s]" % data_str + bcolors.ENDC
        
        while True:
        
            try:
                    
                data = client_sock.recv(1024)
                if len(data) == 0: 
                    data = "NO DATA SET IN APP"
                    break
		
                print bcolors.BOLD + "recieved " + bcolors.GREEN + "[%s]" % data + bcolors.ENDC       
##                modbusCom.gotNewConfig = True
##                new_file_object = open(device_file,"w")
##                new_file_object.writelines(data)
##                new_file_object.closed
            
	    except IOError:
                new_file_object = open(device_file,"w")
                new_file_object.writelines(data)
                print "closing file"
                new_file_object.close()
                modbusCom.gotNewConfig = True
                
                print bcolors.BOLD + bcolors.RED + "Disconnected" + bcolors.ENDC
        
                client_sock.close()
                server_sock.close()
                print bcolors.BOLD + "All done - " + bcolors.RED + "I0" + bcolors.ENDC

                
                
                break
        
            except KeyboardInterrupt:
        
                print bcolors.BOLD + bcolors.RED + "Disconnected" + bcolors.ENDC
        
                client_sock.close()
                server_sock.close()
                print bcolors.BOLD + "Bluetooth connection terminated - " + bcolors.RED + "Keyboard" + bcolors.ENDC
                #modbusCom.stopThread = True
                #modbusThread.join()
                break

    except KeyboardInterrupt:
        print bcolors.BOLD + bcolors.RED + "Disconnected" + bcolors.ENDC

        server_sock.close()
        print bcolors.BOLD + "Pi server offline - " + bcolors.RED + "Keyboard" + bcolors.ENDC
	#comment out two lines below if you do not have the USB to RS485 dongle 
        modbusCom.stopThread = True
        modbusThread.join()
        break
