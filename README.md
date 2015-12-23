# AppConnectSample for Android #

## Introduction ##

This is a sample project for Android that demonstrates how to build a complete application using the AppConnect SDK.

In particular, this project shows how to:

1. Add the Babbage library dependency.
2. Start Babbage when the application launches.
3. Log in a user.
4. Sync the subjects and forms.
5. Fill out and submit a form, either with:
  * All fields on one page, where you use the StepSequencer only at the end.
  * One field per page, where you use the StepSequencer to navigate from field to field.

## Building ##

To get this app up and running:

1. Launch Android Studio and use the "Import project" option.
2. Build and run.

## Using the Sample CRF ##

The sample app can be used to log in with any user and fill out and submit any form using the multi-page UI, with the fields presented sequentially, similarly to how Medidata Patient Cloud does it.

This SDK also comes with a sample CRF that can be used to exercise the one-page UI, where all the fields are presented on one screen. To use it, follow these steps:

1. Import the accompanied CRF into Rave for a subject of your choosing.
2. Log in with the sample app using the credentials of the subject you chose.
3. You should see two forms, Form 1 and Form 2. The former is hardcoded to open as a one-page form. The latter will open as a multi-page form.

## Documentation ##

Please refer to the documentation for detailed instruction on how to use the various APIs.
