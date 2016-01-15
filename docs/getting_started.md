This is a guide to the basics of Babbage - intialization, making network requests, and loading data from the datastore.

## Initialization
Babbage must be initialized with four arguments:
- The `Application` instance
- The directory in which to store the data
- The encryption key
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
Babbage stores data in a SQLite database. You can retrieve this data by using a Datastore.

```java
Datastore datastore = DatastoreFactory.create()
User user = datastore.getUser(username);
```

### Important Considerations
- Although there can be multiple Datastore instances, they are all communicating with the same persistent store (a local SQlite database).
- Datstore instances are not thread safe. If you are creating a new thread (perhaps to make a network request asynchronously) then you should create a new Datastore to accompany it.
- Instances loaded from a Datstore are not thread safe. Instead of passing an instance to a separate thread, pass the instance's ID (e.g. Java: `user.getID()`, Swift: `user.objectID`) and use a separate Datastore to load the instance.


## Network Requests
Babbage works by talking to backend services to retrieve all information (Users, Subjects, Forms, etc..). A normal application flow goes something like this:

1. Log in using a username / password 
2. Load subjects for the logged in user
3. Load forms and present them to the user

```java
User user = client.logIn(datastore, username, password);
List<Subject> subject = client.loadSubjects(datastore, user).get(0)
List<Form> forms = loadForms(datastore, subject)
```

### Important Considerations
- This example assumes the user is associated with a single subject - in reality they may have multiple.
- The example assumes a best-case scenario where each request is successful. A robust application should have adequate error handling throughout the process.
- In an actual app, all requests should be made asynchronously on a background thread to avoid interfering with the UI.
- The Java network requests are not synchronous and should be performed in a background thread.

