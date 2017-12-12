import requests
import json

#this should be updated to Amplisine's API
url = "https://remotesensingplatform.000webhostapp.com/php/createLog.php"

#fucntion to send data to the server
def LogDataToServer(site_id, id, type_id, time, value):
	global url
	data = {"id": id,
			"site_id": site_id, 
			"type_id": type_id, 
			"time": time, 
			"value": value}
    logFile = open("dataLog.txt","a")
    logFile.write(json.dumps(data)+"\n")
    logFile.close()
	#make http post request
	requests.post(url, data)
