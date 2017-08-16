# ARP
Android Record and Play. A Java application to record android touch screen , hard key events and playback.
# ARP- Android Record N Play.


![](https://raw.githubusercontent.com/rils/ARP/master/arp.PNG)

## What is ARP?

ARP- Android Record N Play. Simple java tool helps to record android touchscreen/touchkey/homebutton/powerbutton events and can playback. 
## How it works ?

### Recording-
ARP captures events from input driver(touch,hard key,gpio keys,soft keys) `/dev/input/<input_file>` for corresponding device and redirects to a file.For this arp uses `adb shell getevent` to capture events. 

### Playback-
The events <type code value> are converted to decimal and send back to `/dev/input/<input_file>` using `adb shell sendevent`. But default `sendevent` utility opens the device file for each `"type code value"` triplet. This makes playback extremely slow if there are a series of events. 

 ARP uses a modified sendevent utility which accepts as much `type code value` triplets and open device file at once and close after write.Below is the diff of sendevent.c . The original sendevent.c is found at https://android.googlesource.com/platform/system/core/+/android-5.0.0_r2/toolbox/sendevent.c.

ARP also inserts delay between touch events by checking kernel timestamp diff of each events.


**sendevent.c diff, Original on right side**


![](https://raw.githubusercontent.com/rils/ARP/master/senevent_diff.PNG)

**default sendevent**

`sendevent /dev/input/event1  3 47 0`

`sendevent /dev/input/event1  3 57 13578`

`sendevent /dev/input/event1  1 330 1`

`sendevent /dev/input/event1  1 325 1`

**Modified sendevent**

`/data/local/tmp/mysendevent /dev/input/event1  3 47 0 3 57 13578 1 330 1 1 325 1`


## How it helps?

* Instant ui testing.
* Repeated pattern app ui testing.
* No coding required!

## How to Run?

Download ARP, Double click. [arp.jar](https://github.com/rils/ARP/blob/master/bin/com/ours/tester/arp.jar?raw=true). 


Next is pretty straight forward.

## How to run your recorded events without this tool?

1. Record your actions as mentioned above.
2. It will create a timestamp.mel file.
3. Press play once to verify it is working fine.
4. It would have created a timestamp.mes file in same location.
5. Push the timestamp.mes file to any location in your device.eg:-`adb push timestamp.mes /data/local/`
6. `adb shell sh /data/local/timestamp.mes`
7. Please note: Run timestamp.mes recorded with same device on same model device. 
   if you want try in different device, push `/arp_file_location/mysendevent` file to `/data/local/`

**bug?riwan03@gmail.com:enjoy; \\\\ Thanks :)**

