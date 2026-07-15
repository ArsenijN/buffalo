# buffalo

***buffalo - OBS-oriented solution for mobile camera streaming***

buffalo is heavily OBS-oriented software for streaming the phone's or tablet's 
or whatever Android device that have camera to OBS via Wi-Fi/Ethernet or USB 
connection. It's inspired from DroidCam mobile app and their's OBS plugin, but 
with the addition of features that will help to reuse broken devices with good 
cameras, fixing annoying problems with DroidCam usage and help everyone to get 
their records or streams become better than before.

# Why buffalo exists?

This comes from both my current job and wish of having an alternative to the 
app that I currently use, and rely on.

I work as sound engineer, but also, I work in technical and media. And we have 
OBS doing the records for us (later also the livestreams but right now our 
audio is not ready fully)

The thing is that every time I use the DroidCam, I use it on my Samsung S10, 
because, as turns out, iPhones (even iPhone 11 or iPhone 13) doesn't like the 
rattling surface, resulting in the "smudged image every time bass frequencies 
is played". Samsung S10, turns out, withstands that problem and also outputs 
much better (in terms of quality and noise) image than an iPhone. 

I don't know how, why or what to do with that fact, but this can both say that 
old "open by the wild" tech in form of my old Samsung S10 can be much better 
than "proprietary ecosystem-targeted" tech in form of iPhones. Or because 
DroidCam used wrong pipeline for a camera on iPhones, I don't know and maybe we 
will never know

Additionally, this particular Samsung S10 have a broken screen, so... I can't 
use it fully without any kind of external point device (e.g. KDE Connect's 
feature for remote control or physically connect a mouse to phone via 
OTG/Bluetooth (latter may not be viable since Bluetooth may not connect after 
reboot before phone is unlocked))

And... this is a time when I decided that "I tired of watching the ads to get a 
premium in app that sometimes bugs out with UI where you can't see the nav 
buttons to control the camera image and also it doesn't work over USB and 
also..." and started to brainstorm the idea with my assistent of choice, and I 
started to realize, that:
- I don't know how to make an OBS plugin
- I don't know how to make an Android app except as via 
[MIT App Inventor](https://appinventor.mit.edu/) (as I did before, back in the 
2023-2024)
- Particularly, I don't know how to capture highest quality video from a phone 
and then somehow feed it into the OBS

Those questions now almost resolved with chosen step-by-step codebase changes 
and advantage that my assistent can handle that large amount of code from 
codebase and additionally develop Rust-based OBS addon (yes, it should be 
easier to maintain as turns out - only some of the calls are in `unsafe` case) 
should just work (in theory) after the code for everything will be finished

Please wait until the more-less stable release to use the buffalo - I will not 
take responcibility on this software until it's released public version that 
indeed should work without any problems (aka V1.x or higher)

If you found a bug, security problem, want to improve the software or need any 
help with it's usage (e.g. "No documentation is available for X feature", or "Y 
thing doesn't work in my environment" - feel free to open an issue on GitHub - 
I'll be happy to hear anyone's thoughts and pleases for buffalo, and will try 
to assist with it

buffalo is open for new contributors and idea inspirations

Current buffalo state: bare-bones changes to the Android example, flavoring it -
works via the Python listener code; OBS plugin is still in-progress (doesn't 
compile)

---

# What's done

Currently, you can build the Android app and run the following command on 
PC/laptop to get the video feed from phone when it connects to it (please 
change the `PreviewFragment.kt`'s `private val obsHost = "192.168.31.154"` to 
your actual PC's/laptop's IP address!!!): `python3 test_receiver.py | ffplay -f 
h264 -framerate 30 -i -` (change the 30 to your phone's framerate that it will 
output, otherwise FFmpeg may start to play the video feed faster/slower than 
the actual speed if leaved with wrong value on unspecified (fallbacks to 25 
fps?))

To use buffalo Android app: choose the camera and the video resolution (I 
recommend stick to the common 16:9 resolutions like 1280x720 or 1920x1080, 
otherwise if phone doesn't actually support the res - app should handle that 
gracefully), then click on it - click "MediaCodec" - click the codec of choice 
(use H264 for compatibility reasons, and this is what I was only given) - 
select "Single-stream" - select "Portrait Filter Off" (you don't want the 
filters to be on, didn't ya?)

After all of that config state, you're in the preview window - before pressing 
that pleasuable, alluring in appearance Big Gray Button, let me explain what 
you see:
- on top is the debug info panel - it shows the logs of the buffalo's backend, 
e.g. if the stream is send, is receiver is connected, etc.
- most of the screen is covered with preview from the camera. Important: if you 
see only the black screen and not your actual camera's preview - this means that
the current selected resolution is NOT SUPPORTED and you need to change it to 
something else to work
- In lower half of the screen, you can see that Big Gray Button. It starts the 
video feed send when you HOLD it

By holding the Big Gray Button, you can see the next logs appearing in the info 
panel:

```
Record Button Pressed
Video TCP target: [IP]:[port]
Control WS target: [IP]:[port]
Streaming started
...
```

Those messages means that you're actively sends the video feed from your device 
over the LAN!

When the receiver IS connected, you'll see the "Video TCP: CONNECTED" once, 
instead of repeating "Video TCP: disconnected (retrying...)"

`Encoded x frames so far (last size=n)` is a statistics info - you can compare 
those values with the FFmpeg's values to understand if phone/LAN/FFmpeg is not 
throttling (or by the Time Of Appearance in logs - this message should appear 
every 1 second at 30 fps)

OBS's plugin right now is not ready - it acts completely backwards to the 
current system (where by the specs the phone is client that connects to the 
OBS, which is should be the server, and gives to it the video feed, but current 
system uses backwards way)