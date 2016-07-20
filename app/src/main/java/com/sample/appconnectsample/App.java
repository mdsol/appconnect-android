package com.sample.appconnectsample;

import android.app.Activity;
import android.app.Application;
import com.mdsol.babbage.Babbage;
import com.mdsol.babbage.model.Datastore;
import com.mdsol.babbage.model.DatastoreFactory;
import com.mdsol.babbage.net.Client;
import com.mdsol.babbage.net.ClientFactory;
import java.io.File;

/**
 * The Application subclass that manages things needed by all activities.
 */
public class App extends Application {

    private Client client;
    private Datastore UIDatastore;

    @Override
    public void onCreate() {
        super.onCreate();

        // *** AppConnect ***
        // Start AppConnect. This must be done as early as possible during the
        // lifetime of the app, so doing it in an Application subclass is ideal.
        // The passed directory is used to store the database. The key is used
        // to encrypt sensitive information in the database. It must be 32-bytes
        // long and must be the same between runs. It really should be stored
        // somewhere safe, and not created inline like here.
        File dir = getFilesDir();
        byte[] key = new byte[] {
            1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2};
        Babbage.start(this, Client.Environment.PRODUCTION, "API Token", dir, dir, key, null);

        // *** AppConnect ***
        // The client that will be used to make requests to the backend can be
        // created once and reused as needed throughout the app
        client = ClientFactory.getInstance().getClient(Client.Type.HYBRID);

        // *** AppConnect ***
        // In order for object states to be consistent between views, all UI
        // code must get objects from the same datastore, so it's a good idea to
        // create it once and make it available to the rest of the app
        UIDatastore = DatastoreFactory.create();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        UIDatastore.dispose();
    }

    public static Client getClient(Activity activity) {
        return ((App)activity.getApplication()).client;
    }

    public static Datastore getUIDatastore(Activity activity) {
        return ((App)activity.getApplication()).UIDatastore;
    }
}
