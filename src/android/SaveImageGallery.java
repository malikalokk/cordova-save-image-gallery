package com.agomezmoron.saveImageGallery;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.Arrays;
import java.util.List;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.app.DownloadManager;
import android.content.Context;

/**
 * SaveImageGallery.java
 *
 * Extended Android implementation of the Base64ToGallery for iOS.
 * Inspirated by StefanoMagrassi's code
 * https://github.com/Nexxa/cordova-base64-to-gallery
 *
 * @author Alejandro Gomez <agommor@gmail.com>
 */
public class SaveImageGallery extends CordovaPlugin {

    // Consts
    public static final String EMPTY_STR = "";

    public static final String JPG_FORMAT = "JPG";
    public static final String PNG_FORMAT = "PNG";

    // actions constants
    public static final String SAVE_BASE64_ACTION = "saveImageDataToLibrary";
    public static final String REMOVE_IMAGE_ACTION = "removeImageFromLibrary";

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        if (action.equals(SAVE_BASE64_ACTION)) {
            this.saveBase64Image(args, callbackContext);
        } else if (action.equals(REMOVE_IMAGE_ACTION)) {
            this.removeImage(args, callbackContext);
        } else { // default case: SAVE_BASE64_ACTION
            this.saveBase64Image(args, callbackContext);
        }

        return true;
    }

    /**
     * It deletes an image from the given path.
     */
    private void removeImage(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String filename = args.optString(0);

        // isEmpty() requires API level 9
        if (filename.equals(EMPTY_STR)) {
            callbackContext.error("Missing filename string");
        }

        File file = new File(filename);
        if (file.exists()) {
            try {
                file.delete();
            } catch (Exception ex) {
                callbackContext.error(ex.getMessage());
            }
        }

        callbackContext.success(filename);

    }

    /**
     * It saves a Base64 String into an image.
     */
    private void saveBase64Image(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String filename = "IG-293.jpg";
String downloadUrlOfImage = "https://gitlab.com/api/v4/projects/10758754/repository/files/lol%2FIG-293.jpg/raw?ref=master&private_token=2xwEekJZhJKxB9BxDcXa";
    File direct =
            new File(Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .getAbsolutePath());


    if (!direct.exists()) {
        direct.mkdir();
    }

    DownloadManager dm = new DownloadManager(getContext().getSystemService(Context.DOWNLOAD_SERVICE));
    Uri downloadUri = Uri.parse(downloadUrlOfImage);
    DownloadManager.Request request = new DownloadManager.Request(downloadUri);
    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
            .setAllowedOverRoaming(false)
            .setTitle(filename)
            .setMimeType("image/jpeg")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES,
                    File.separator + filename);

    dm.enqueue(request);
    }

    /**
     * Private method to save a {@link Bitmap} into the photo library/temp folder with a format, a prefix and with the given quality.
     */
    private File savePhoto(Bitmap bmp, String prefix, String format, int quality) {
        File retVal = null;

        try {
            String deviceVersion = Build.VERSION.RELEASE;
            Calendar c = Calendar.getInstance();
            String date = EMPTY_STR + c.get(Calendar.YEAR) + c.get(Calendar.MONTH) + c.get(Calendar.DAY_OF_MONTH)
                    + c.get(Calendar.HOUR_OF_DAY) + c.get(Calendar.MINUTE) + c.get(Calendar.SECOND);

            int check = deviceVersion.compareTo("2.3.3");

            File folder;

            /*
             * File path = Environment.getExternalStoragePublicDirectory(
             * Environment.DIRECTORY_PICTURES ); //this throws error in Android
             * 2.2
             */
            if (check >= 1) {
                folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

                if (!folder.exists()) {
                    folder.mkdirs();
                }

            } else {
                folder = Environment.getExternalStorageDirectory();
            }

            // building the filename
            String fileName = prefix + date;
            // switch for String is not valid for java < 1.6, so we avoid it
            if (format.equalsIgnoreCase(PNG_FORMAT)) {
                fileName += ".png";
            } else {
                // default case
                fileName += ".jpg";
            }

            // now we create the image in the folder
            File imageFile = new File(folder, fileName);
            FileOutputStream out = new FileOutputStream(imageFile);
            out.flush();
            out.close();

            retVal = imageFile;

        } catch (Exception e) {
            Log.e("SaveImageToGallery", "An exception occured while saving image: " + e.toString());
        }

        return retVal;
    }

    /**
     * Invoke the system's media scanner to add your photo to the Media Provider's database,
     * making it available in the Android Gallery application and to other apps.
     */
    private void scanPhoto(File imageFile) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(imageFile);

        mediaScanIntent.setData(contentUri);

        cordova.getActivity().sendBroadcast(mediaScanIntent);
    }
}
