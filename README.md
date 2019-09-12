# wirelessfencingproto
Wireless Fencing System (the sport, not posts and pickets)

This represents work on a prototype that was done up to 2016. Since then I have started redesigning the system and future versions are envisioned with the following changes in mind:
  
  * replacing the Bluetooth with Wifi as it offers a better foundation for secure and robust communications
  * utilizing HTTPS over a local LAN for clients
  * token-based authentication
  * device and user registration
  * management console
  * increasing the scope to cover more aspects of the fencer's efforts including tournaments, queuing for strips, profiles, etc.

## client
WirelessFencingClient is code and configuration on a raspberry pi device interfacing with standard fencing gear (swords, cables, etc.) running python. It connects to and communicates time sync, hit, and grounding data with a scorebox server over bluetooth. The raspberry pi is loaded with raspbian and includes the following environment setup

### installation
```bash
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
```

### startup
```bash
(hciconfig hci0 name WirelessFencingRed
hciconfig hci0 piscan ) &
```

## server
WirelessFencingSandbox is the prototype of a wireless fencing scorebox app. It requests and establishes connections to player devices as well, configures match details, and processes client data as it comes in.
