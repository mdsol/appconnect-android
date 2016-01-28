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
  
See [Medidata AppConnect Home](https://learn.mdsol.com/display/APPCONNECTprd/Medidata+AppConnect+Home) for more information. 

## Prerequisites

If you are running this application, it is assumed that:

- You were provided Artifactory credentials by a Medidata representative.
- You have a valid Rave installation with Patient Cloud functionality enabled.

## Building ##

To get this app up and running:

1. Launch Android Studio and use the "Import project" option.
2. Add the following lines to your local.properties file, using the provided Artifactory credentials:

```gradle
artifactory.username=yourusername
artifactory.password=yourpassword
```
3. Build and run.

## Using the Sample CRF ##

The sample app can be used to log in with any user and fill out and submit any form using the multi-page UI, with the fields presented sequentially, similarly to how Medidata Patient Cloud does it.

This SDK also comes with a sample CRF that can be used to exercise the one-page UI, where all the fields are presented on one screen. To use it, follow these steps:

1. Import the accompanied CRF into Rave for a subject of your choosing.
2. Log in with the sample app using the credentials of the subject you chose.
3. You should see two forms, Form 1 and Form 2. The former is hardcoded to open as a one-page form. The latter will open as a multi-page form.

# Using the API in your own application #

This is a guide to the basics of Babbage - intialization, making network requests, and loading data from the datastore.

## Installation
To install Babbage, include it in the `build.gradle` file, using the credentials provided to you. When you next sync your gradle project, the library will be downloaded and installed.

```groovy
repositories {
    maven {
        credentials {
            username "myusername"
            password "mypassword"
        }

        url 'https://etlhydra-artifactory-sandbox.imedidata.net/artifactory/p-cloud-release'
    }
}
```

An example of how to load these credentials from the `local.properties` file can be found in the sample app's [build.gradle](https://github.com/mdsol/appconnect-android/blob/develop/app/build.gradle) file.

## Initialization
Babbage must be initialized with four arguments:
- The `Application` instance
- The directory in which to store the data
- The encryption key, which must be unique for each installation, and remain the same on each launch.
- An instance of `Babbage.Listener` to handle migration events

```java
// In App.Java
File filesDir = getFilesDir();

// Set up the encryption key used for data at rest
byte[] key = BabbageKeyStore.getInstance().getKey(this);

// Load the native Babbage library
Babbage.start(this, filesDir, key, new MyCustomBabbageListener());
```


## Loading Data from the Datastore
You can store and retrieve persistent data using the Datastore class.

```java
Datastore datastore = DatastoreFactory.create()
User user = datastore.getUser(username);
```

>**Important Considerations:** 
  - Although there can be multiple Datastore instances, they are all communicating with the same persistent store (a local SQlite database).
  - Datastore instances are not thread-safe. If you are creating a new thread - perhaps to make a network request asynchronously - then you should create a new Datastore to accompany it.
  - Instances loaded from a Datastore are not thread-safe. Instead of passing an instance to a separate thread, pass the instance's ID - for example, Java: `user.getID()`, Swift: `user.objectID` - and use a separate Datastore to load the instance.


## Network Requests
Babbage talks to back-end services to retrieve all information, such as users, subjects, forms, and so on. A normal application flow goes something like this:

1. Log in using a username / password 
2. Load subjects for the logged in user
3. Load forms and present them to the user

The following code replicates this process:
```java
User user = client.logIn(datastore, username, password);
List<Subject> subject = client.loadSubjects(user).get(0)
List<Form> forms = loadForms(subject)
```

>**Important Considerations:**
  - The preceding example assumes the user is associated with a single subject. In reality they may have multiple subjects associated with them.
  - The example assumes a best-case scenario where each request is successful. A robust application should have adequate error handling throughout the process.
  - To avoid interfering with the UI, make all requests asynchronously on a background thread.
  - The Java network requests are not synchronous and should be performed in a background thread.

## API Documentation ##

Please refer to the documentation for detailed instruction on how to use the various APIs.
