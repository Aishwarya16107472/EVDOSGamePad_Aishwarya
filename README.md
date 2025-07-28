Okay, let's strip it down to the essentials for a simple README.

---

# Evdos BLE Gamepad

This is an Android application that turns your phone into a Bluetooth controller for a remote car or robot.

## Features

* **Accelerometer Steering:** Tilt your phone left/right to steer.
* **Forward/Backward Buttons (X and B):**
    * **Hold:** Press and hold for continuous movement.
    * **Tap (Accessibility/Toggle):** Tap once to start movement, tap again to stop.
* **Speed Control (Circular Slider):** Adjust the speed (throttle). It resets to neutral when released.
* **Bluetooth Low Energy (BLE):** Your phone acts as a BLE server to send commands to your car.

## How it Works (for Developers)

The app sends commands in a specific text format over Bluetooth.

**Command Format:** `"steer:X_VALUE,speed:Y_VALUE"`

* `X_VALUE`: Comes from your phone's left/right tilt (`accelX`). Uses a period (`.`) for decimals (e.g., `0.50`).
* `Y_VALUE`: Combines your forward/backward button input (`direction`) and the speed slider (`throttleValue`).
    * Positive numbers: Forward (e.g., `150`)
    * Negative numbers: Backward (e.g., `-150`)
    * Zero: Stop (`0`)

**Important:** Your car's code needs to be programmed to understand and react to this exact `steer:X,speed:Y` format.

## Setup & Running

1.  **Open in Android Studio:** Load this project.
2.  **Permissions:** On Android 12+, make sure Bluetooth permissions are granted to the app. (You might need to add code to ask the user for these if not already done).
3.  **Run:** Deploy the app to your Android device.
4.  **Connect:** Your car (BLE Client) should connect to the app (BLE Server) using the defined Service and Characteristic UUIDs.

## Troubleshooting

* **No Bluetooth connection:** Check phone's Bluetooth, app permissions, and car's power/BLE status.
* **Car doesn't respond correctly:** Double-check the command parsing code on your car's side, especially for the number formats (always expects a period for decimals).

---
