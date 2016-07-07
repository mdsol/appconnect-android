package com.sample.appconnectsample;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.mdsol.babbage.model.Datastore;
import com.mdsol.babbage.model.DatastoreFactory;
import com.mdsol.babbage.model.Subject;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * An activity to save an image to AppConnect
 */
public class ImageCaptureActivity extends AppCompatActivity {

    public static final String SUBJECT_ID_EXTRA = "appconnectsample.imagecaptureactivity.intent.extra.SUBJECT_ID";

    private static final String TAG = "ImageCaptureActivity";

    private long subjectID;
    private Bitmap currentImage;
    private ImageView currentImageView;
    private Button submitButton;
    private Button takePictureButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_capture_activity);

        currentImageView =  (ImageView)findViewById(R.id.currentThumbnail);
        submitButton = (Button)findViewById(R.id.imageSubmitButton);
        takePictureButton = (Button)findViewById(R.id.takePictureButton);

        // Get the ID of the subject to save images for
        Intent intent = getIntent();
        subjectID = intent.getLongExtra(SUBJECT_ID_EXTRA, 0);

        if (!getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY))
            takePictureButton.setEnabled(false);

        submitButton.setEnabled(false);

    }

    public void doSelectImageButton(View source) {
        dispatchSelectImageIntent();
    }

    public void doTakePictureButton(View source) {
        dispatchTakePictureIntent();
    }

    private static final int IMAGE_CAPTURE_INTENT = 1;
    private static final int SELECT_IMAGE_INTENT = 2;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, IMAGE_CAPTURE_INTENT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMAGE_CAPTURE_INTENT && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            currentImage = (Bitmap) extras.get("data");
            currentImageView.setImageBitmap(currentImage);
            submitButton.setEnabled(true);
        }
        else if (requestCode == SELECT_IMAGE_INTENT && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            try {
                InputStream imageStream = getContentResolver().openInputStream(selectedImage);
                currentImage = BitmapFactory.decodeStream(imageStream);
                currentImageView.setImageBitmap(currentImage);
                submitButton.setEnabled(true);
            } catch (FileNotFoundException exception) { }
        }
    }

    private void dispatchSelectImageIntent() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, SELECT_IMAGE_INTENT);
    }

    public void doSubmitButton(View source) {
        submitButton.setEnabled(false);
        new CollectAndSubmitTask().execute(currentImage);
    }

    /**
     * An asynchronous task that collects the responses and submits the form.
     */
    private class CollectAndSubmitTask extends AsyncTask<Bitmap,Void,Void> {

        private ProgressDialog progressDialog;
        private String message;
        private Exception exception;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(ImageCaptureActivity.this, ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(getString(R.string.image_capture_saving_message));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Bitmap... params) {
            // *** AppConnect ***
            // Each secondary thread must create its own datastore instance and
            // dispose of it when done
            Datastore datastore = null;

            try {
                datastore = DatastoreFactory.create();
                Subject subject = datastore.getSubject(subjectID);

                // debug test, in future convert the whole image not just the ninepatch
                subject.collectData(currentImage.getNinePatchChunk(), "sample metadata", "image/jpeg");
            }
            catch (Exception ex) {
                exception = ex;
                message = "";
            }
            finally {
                if (datastore != null)
                    datastore.dispose();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.cancel();

            if (message != null) {
                Log.e(TAG, message);
                new AlertDialog.Builder(ImageCaptureActivity.this).
                    setTitle(R.string.image_capture_error_title).
                    setMessage(message).
                    setPositiveButton(R.string.ok_button, null).
                    show();
            }
            else if (exception != null) {
                Log.e(TAG, "The submit task failed", exception);
                new AlertDialog.Builder(ImageCaptureActivity.this).
                    setTitle(R.string.image_capture_failed_title).
                    setMessage(R.string.image_capture_failed_message).
                    setPositiveButton(R.string.ok_button, null).
                    show();
            }
            else {
                new AlertDialog.Builder(ImageCaptureActivity.this).
                    setCancelable(false).
                    setTitle(R.string.image_capture_succeeded_title).
                    setMessage(R.string.image_capture_succeeded_message).
                    setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            currentImageView.setImageBitmap(null);
                            currentImage = null;

                            //finish();
                        }
                    }).
                    show();
            }
        }
    }
}
