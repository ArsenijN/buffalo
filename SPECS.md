# Specifications and development infos

This file contains step-by-step guide of how to build a buffalo from ground up

# 1. Bare bones

Bare bones buffalo will work as testing thing, meaning the bare video feed from camera from buffalo app to the OBS's simple buffalo plugin that will just have simple settings maybe and that's all. On first, the USB connection will be mainstream, then Ethernet/Wi-Fi

# 2. Proof of Concept

PoC buffalo will start to develop the simple communication between the OBS and phone to change the camera settings






# Other things

## Comunications

The communications between the devices should be made via WebSocket or simple HTTP requests with JSON responces, with understandable endpoints/requests names

## Capturing

buffalo should capture the video feed in the best way possible, aka also use the phone's enhancements and other features for video

## Buffering and latency

buffalo should specifically use the RAM of the device and avoid to use the SWAP or Flash in any circumstances to avoid the excess memory exaustion by the buffer. The buffer can be made as client-side and server-side, where server is the phone and client is the OBS instance