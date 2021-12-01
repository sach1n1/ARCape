# ARCape in Development

The objective of the project is to develop an augmented reality based hint system which can aid the people playing in an escape room to plan their escape by giving them suitable hints to solve the puzzle through AR overlays on scanned images. This method of overlaying AR on a scanned image is called image augmentation. 

The AR object is overlayed on the image based on the internal state of the image which is obtained through subscription of MQTT messages. Each image has an MQTT topic associated with the puzzle it\s being used for and receives messages on this MQTT topic pertaining to the internal state of the puzzle:
- Inactive
- Active
- Solved

The image to be scanned will be placed next to the puzzles and can be scanned through the android app. On scanning the image, the app recognizes the image and subscribes to the topic associated with the image and overlays an Augmented Reality projection which gives the following information:
1. If puzzle is not activated, display `Not Active`.
2. If puzzle is activated project the relevant hint.
3. If puzzle has been solved, display `Solved`.
