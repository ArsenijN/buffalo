# TODO

This To-Do file will contain the main feature set to implement over time

# MVP - current TODO for the dev project

## Mobile app - Android

*sorted by priority*

- [ ] Button work as self-hold until pressed again
- [ ] Instead of separate tabs for settings, make a simple setting menu to 
change them; they persists in the app data

## OBS Plugin

*sorted by priority*

- [x] Fix the issues with connectivity (aka no one listens, everyone talks)
- [x] Fix a version text being a text with input bar on side
- [ ] Make video feed work actually
- [ ] Make a version text be just the section name so it will look better
    - [ ] Same for the status
        - [ ] Auto-update the status without need of reload



# Basis - when project reaches full specs implementation and backend works as 
should

## Mobile app - Android

### UI
- [ ] Main menu
    - [ ] Infos: e.g. device's local IP, what network it's connected to, what 
    type (2.4/5 GHz), etc.
    - [ ] Buttons: settings, help page
    - [ ] Externals (link to github repo, author's support page, etc.)
- [ ] Connected menu
    - [ ] Camera preview (WYSIWYG)
    - [ ] Controls (buttons; AE, AF, pan/zoom, settings, etc.)
    - [ ] Stats text: bitrate, dropouts (packets or phone's), connection time, 
    audio/video codecs, buffer size, etc.
    - [ ] Lock button: will not allow to change settings/control the phone 
    until unlocked; handy when phone is handled also on touchscreen
    - [ ] Dimm button: dimm the display, if supported - turn off the screen 
    entirely (if device supports background camera access) or set minimum 
    screen brightness 
### Backend
- [ ] Ability to change the bitrate for both audio and video as preset or any 
number in real time, without interuptions
- [ ] More codec options for different needs (e.g. HEVC, AV1, etc.)
- [ ] Low-latency mode (do not use the buffer; usable only with Ethernet/USB, 
Wi-Fi may become quirky)

### Feature set
- [ ] Ability to rotate the UI to other side (which DroidCam unable to)


## OBS plugin

### UI
- [ ] Main menu for settings
    - [ ] Basic setup tab - easy for use settings, that are not specific for 
    user's knowledge
        - [ ] Device list with a button to update the list
        - [ ] Resolution set
    - [ ] Advanced setup tab - more settings, for advanced users
        - [ ] 

### Backend

### Feature set
- [ ] Ability to control the pan/zoom, AE, AF, camera module, buffer size, LL 
mode (low-latency), bitrates (in real time), etc. from the OBS plugin without 
need to touch the device