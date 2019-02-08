# FirebaseVision

FirebaseVision - A framework making it easy to recognize text, images, faces, barcodes and more easy - using Firebase ML Kit.

  - Activate vision features in activities in one line of code.
  - Scan faces, text, barcodes, images and more.
  - Automatically handles asking for permissions (works like magic).

# Getting started

  - Make sure Firebase is set up in your Android project. For a full guide, follow this link: https://firebase.google.com/docs/android/setup?hl=da,
    you DON'T need any gradles depencies regarding Firebase ML Kit.

  - Add gradle dependency ```implementation 'com.github.makeabledk:firebasevision:latest.version.number'```
  - (optional) For quicker recognition, consider adding the follow metadata to your Manifest file:
    ``` 
            <application
            ...
            <!-- The following META Data makes the app download the ML kit models to the device upon installing the app. -->
            <meta-data
                android:name="com.google.firebase.ml.vision.DEPENDENCIES"
                android:value="ocr"
                />
    ```

  - Create a RecognitionProcessor to process results from on of the Firebase ML Kit detector classes, for examples, look in the examples folder in this repo.
  - Create any Graphics classes that can draw views or elements to the camera overlay.
  - Create an activity extending the ```FirebaseVisionActivity``` class, in this activity, include a layout which contains a ```CameraSourcePreview + GraphicOverlay``` combo. To start the camera preview and begin processing results from your new activity, call ```setupVisionDetection()``` with the parameters needed.
  - # Thats it, now your RecognitionProcessor will send out detection events to any listener.
