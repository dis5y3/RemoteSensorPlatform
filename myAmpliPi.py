import os
import time
from bluetooth import *

base_dir = '/home/pi/Desktop/Python/Output/'
device_file = base_dir + 'params.txt'

def read_string_raw():
    f = open(device_file, 'r')
    lines = f.readlines()
    f.close()
    return lines


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
print "Waiting for connection on RFCOMM channel %d" % port

client_sock, client_info = server_sock.accept()
print "Accepted connection from ", client_info

while True:

	try:
	        data = client_sock.recv(1024)
        	if len(data) == 0: break
	        print "received [%s]" % data

#		if data != 0:
		data = str(read_string_raw())
		print data
#		else:
#			data = 'WTF!' 

#	        client_sock.send(data)
#		print "sending [%s]" % data

	except IOError:
		pass

	except KeyboardInterrupt:

		print "disconnected"

		client_sock.close()
		server_sock.close()
		print "all done"

		break
