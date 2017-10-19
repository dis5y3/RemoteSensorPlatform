import os
import time
from bluetooth import *

base_dir = '/home/pi/Desktop/Python/Output/'
device_file = base_dir + 'Device_Config.txt'

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
		
		print "Waiting for connection on RFCOMM channel %d" % port
		
		client_sock, client_info = server_sock.accept()
		print "Accepted connection from ", client_info
		
		#Work on code to send existing data to app here
		
		old_file_object = open(device_file,"r")
		existing_data = old_file_object.readlines()
		data_str = " ,".join(str(item) for item in existing_data)
		old_file_object.close
		
		client_sock.send("[" + data_str + "]")
		print "sending [%s]" % data_str
		
		while True:
		
			try:
			        
				data = client_sock.recv(1024)
		        	if len(data) == 0: 
					data = "NO DATA SET IN APP"
					break

				print "recieved [%s]" % data		
		
			except IOError:
				
				print "disconnected"
		
				client_sock.close()
				server_sock.close()
				print "all done - I0"
		
				new_file_object = open(device_file,"w")
				new_file_object.writelines(data)
				new_file_object.close

				break
		
			except KeyboardInterrupt:
		
				print "disconnected"
		
				client_sock.close()
				server_sock.close()
				print "all done - Keyboard"
	
				new_file_object = open(device_file,"w")
				new_file_object.writelines(data)
				new_file_object.close
		
				break

	except KeyboardInterrupt:
	
		print "disconnected"
		print "server offline - Keyboard"
	
		break
