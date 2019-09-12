''' Installation
sudo apt-get install --no-install-recommends bluetooth
sudo service bluetooth status
sudo invoke-rc.d bluetooth restart
hcitool scan
sudo hciconfig hci0 name 'WirelessFencing'
sudo apt-get install bluez python-bluez
sudo nano /etc/bluetooth/main.conf <change to DisablePlugins=pnat>
sudo nano /etc/bluetooth/main.conf <change to Name=WirelessFencingRed>
sudo nano /etc/ntp.conf <change to 2.android.pool.ntp.org>
bluez-simple-agent hci0 23:E4:87:4C:B3:A1
'''

''' Run at startup (add to the end of rc.local)
# WirelessFencing
hciconfig hci0 name WirelessFencingRed
hciconfig hci0 piscan
sudo python /home/pi/Desktop/WirelessFencing.py
'''

import sys
import time
import platform
import subprocess
import datetime
import bluetooth
import RPi.GPIO as GPIO
import WirelessFencingConfig as wf

# initialize vars
gpio_input = 17
gpio_input_gnd = 4

def main():
	version = "1.0.2"

	print "*************************************************"
	print "Wireless Fencing v%s by Eric Stanfield" % version
	print "*************************************************"
	print platform.python_version()

	print "[ Setup ] setting %s fencer name ..." % wf.name
	p_name = subprocess.Popen(["sudo","hciconfig","hci0","name","WirelessFencingRed"])
	p_name.wait()

	if (p_name.returncode != 0):
		print "[ Error ] could not set fencer name"
		return

	print "[ Start ] making %s fencer discoverable ..." % wf.name
	p_discoverable = subprocess.Popen(["sudo","hciconfig","hci0","piscan"])
	p_discoverable.wait()

	if (p_discoverable.returncode != 0):
		print "[ Error ] could not make discoverable"
		return

	fencerServer()

def fencerServer():

	# open socket and listen for requests
	server_sock = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
	server_sock.bind(("", bluetooth.PORT_ANY))
	server_sock.listen(1)

	port = server_sock.getsockname()[1]

	# let other devices know we are here and ready
	bluetooth.advertise_service(	server_sock, wf.name,
					service_id = wf.uuid,
					service_classes = [ wf.uuid, bluetooth.SERIAL_PORT_CLASS ],
					profiles = [ bluetooth.SERIAL_PORT_PROFILE ]
					)

	# setup GPIO listener
	GPIO.setmode(GPIO.BCM)
	GPIO.setup(gpio_input, GPIO.IN, pull_up_down=GPIO.PUD_DOWN)
	GPIO.add_event_detect(gpio_input, GPIO.RISING, callback=hit, bouncetime=wf.reset_time)
	GPIO.setup(gpio_input_gnd, GPIO.IN, pull_up_down=GPIO.PUD_UP) #works!
	#GPIO.add_event_detect(gpio_input_gnd, GPIO.FALLING, callback=dummy, bouncetime=500)
	#GPIO.setup(gpio_input_gnd, GPIO.IN, pull_up_down=GPIO.PUD_UP) #works!
	#GPIO.add_event_detect(gpio_input_gnd, GPIO.FALLING, callback=gnd, bouncetime=20) #works!

	try:
		while True:

			# wait for connection
			print "Waiting for connection on RFCOMM channel %d" % port
			client_sock, client_info = server_sock.accept()
			local_address = client_info[0]
			port = client_info[1]
			print "Accepted connection from %s" % local_address

			# process requests as they come in
			try:
				while True:

					data = client_sock.recv(1)

					if (len(data) == 0):
						break
					else:			
						print "received [%s]" % data

						doResponse = True

						#checkpoint
						if (data == "c"):
							wf.checkpoint = unix_time_millis(datetime.datetime.now())
							response = "c:%.5f" % wf.checkpoint
						#lastTrigger
						elif (data == "h"):
							if (wf.point_trigger):
								#response = "h:%.5f" % wf.point_trigger_time
								if (wf.point_trigger_grounded):
									response = "g:%.5f" % wf.point_trigger_time
								else:
									response = "h:%.5f" % wf.point_trigger_time
								wf.point_trigger = False
							else:
								doResponse = False
						#timeSync
						elif (data == "t"):
					        	wf.time_sync = unix_time_millis(datetime.datetime.now())
							response = "t:%.5f" % wf.time_sync
						#shutdown
						elif (data == "s"):
							response = "s:Fencer {0} shutting down ..."
							client_sock.send(response)
							cleanupClientSocket(client_sock)
							cleanupServerSocket(server_socket)
							cleanupGPIO()
							p_shutdown = subprocess.Popen(["sudo","shutdown","-h","now"])
							return
						else:
							response = "invalid command      "

						wf.point_trigger = False

						if (doResponse):
							client_sock.send(response)
							print "sending response [%s]" % response

			except IOError:
				pass

			except KeyboardInterrupt:
				cleanupClientSocket(client_sock)

			cleanupClientSocket(client_sock)

	except IOError:
		pass

	except KeyboardInterrupt:
		cleanupServerSocket(server_sock)
		cleanupGPIO()
		stop()

	#cleanupServerSocket(server_sock)
	#cleanupGPIO()
	#stop()

def hit(channel):
	try:
		ts = datetime.datetime.now()
		time.sleep(0.002)
		input = GPIO.input(channel)
		ground = GPIO.input(gpio_input_gnd)
		print channel, input
		print gpio_input_gnd, ground
		if input:
			wf.point_trigger = True
			#wf.point_trigger_grounded = False
			wf.last_point_trigger_time = wf.point_trigger_time
			wf.point_trigger_time = unix_time_millis(ts)
			#print "Hit recorded at ", str(ts)

			if ground:
				wf.point_trigger_grounded = True
				print "Grounded hit recorded at ", str(ts)
				time.sleep(0.010) #extra cushion
			else:
				wf.point_trigger_grounded = False
				print "Hit recorded at ", str(ts)
				

	except Exception, e:
	        print str(e)

def gnd(channel):
	try:
		ts = datetime.datetime.now()
		ground = GPIO.input(channel)
		print channel, ground
		if ground:
			wf.point_trigger = True
			wf.point_trigger_grounded = True
			wf.last_point_trigger_time = wf.point_trigger_time
			wf.point_trigger_time = unix_time_millis(ts)
			print "Grounded hit recorded at ", str(ts)

	except Exception, e:
	        print str(e)

def dummy(channel):
	try:
		ts = datetime.datetime.now()
		d = GPIO.input(channel)
		print "Dummy did its job ", str(ts)

	except Exception, e:
	        print str(e)

def cleanupClientSocket(client_sock):
	try:
		print "Closing client socket ..."
		client_sock.close()

	except Exception, e:
		print str(e)

def cleanupServerSocket(server_sock):
	try:
		print "Closing server socket ..."
		server_sock.close()

	except Exception, e:
		print str(e)

def cleanupGPIO():
	try:
		print "Cleaning up GPIO ..."
		GPIO.cleanup()

	except Exception, e:
		print str(e)

def stop():
	try:
		print "[ Stop ] making fencer discoverable ..."
		p_discoverable = subprocess.Popen(["sudo","hciconfig","hci0","noscan"])
		p_discoverable.wait()

		if (p_discoverable.returncode != 0):
			print "[ Error ] could not stop discoverable"
			return

	except Exception, e:
		print str(e)

def unix_time(dt):
	try:
		epoch = datetime.datetime.utcfromtimestamp(0)
		delta = dt - epoch
		return delta.total_seconds()

	except Exception, e:
	        print str(e)
		return 0.0

def unix_time_millis(dt):
	try:
		return unix_time(dt) * 1000.0

	except Exception, e:
		print str(e)
		return 0.0

# Gets bluetooth MAC address based on friendly name
def getBTAddress(name):
	address = None

	nearby_devices = bluetooth.discover_devices()

	for bdaddr in nearby_devices:
		if name == bluetooth.lookup_name( bdaddr ):
			address = bdaddr
			break

	if address is not None:
		print "found %s with address %s" % (name, address)
	else:
		print "could not find %s nearby" % (name)

	return address

def inquiry():
	print("performing inquiry...")

	nearby_devices = bluetooth.discover_devices(
	        duration=8, lookup_names=True, flush_cache=True, lookup_class=False)

	print("found %d devices" % len(nearby_devices))

	for addr, name in nearby_devices:
	    try:
	        print("  %s - %s" % (addr, name))
	    except UnicodeEncodeError:
        	print("  %s - %s" % (addr, name.encode('utf-8', 'replace')))

### START ###
main()