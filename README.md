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