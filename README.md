# DSDL Final project

![](https://thumbs.gfycat.com/SaneSpanishCrayfish-max-1mb.gif)

This repo contains our DSDL(Digital System Design and Lab) course's final project, which is making a cold wallet out of Rspberrypi3.

## Implementation with BLE

[BLE_server](https://github.com/Nicetiesniceties/DSDL/blob/master/BLE_server) contains the python file we put in pi, which is mostly copied from the sample code of [BLUEZ5.4.3](http://www.bluez.org).

[BLE_APP](https://github.com/Nicetiesniceties/DSDL/tree/master/BLE_APP) is the app that can connect and exchange messages with pi.

## Implementation with Bluetooth

[Bluetooth3.0_server.py](https://github.com/Nicetiesniceties/DSDL/blob/master/Bluetooth3.0_server.py) contains the python file we put in pi, which is build with "bluetooth" module in python.

[bluetooth3.0_app](https://github.com/Nicetiesniceties/DSDL/tree/master/bluetooth3.0_app) is another app that pair the above server.