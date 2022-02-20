# ARCape in Development

The objective of the project is to develop an augmented reality based hint system which can aid the people playing in an escape room to plan their escape by giving them suitable hints to solve the puzzle through AR overlays on scanned images. This method of overlaying AR on a scanned image is called image augmentation. 

The AR object is overlayed on the image based on the internal state of the image which is obtained through subscription of MQTT messages. Each image has an MQTT topic associated with the puzzle it\s being used for and receives messages on this MQTT topic pertaining to the internal state of the puzzle:
- Not active
- Active
- Solved

The image to be scanned will be placed next to the puzzles and can be scanned through the android app. On scanning the image, the app recognizes the image and subscribes to the topic associated with the image and overlays an Augmented Reality projection which gives the following information:

1. Not activated state: In this state, the puzzle has not been activated and is not playable. The application will place an AR object with the text “Not Activated” to highlight the state of the puzzle.
2. Activated state: In this state, the puzzle has been activated and is playable. The application does the following:
    (a) Tap to Show Hint: The AR projection displays “Tap to show hint”, in case the player doesn’t want to use the hint to solve the puzzle.
    (b) Hint: When the user taps the screen, the application places an object with the hint text. In case of multiple hints, when the next hint is active, the application goes back to 2(a) and this repeats until all the hints have been obtained.
3. Solved state: When the puzzle has been completely solved it reaches the solved state. The AR object now displays the text “solved!!”.

In order to make changes, once you have downloaded the repository make your changes and then generate a new APK file and continue from there.
