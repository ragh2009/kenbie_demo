package com.kenbie.util;

import android.app.Activity;
import android.content.Context;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class RuntimePermissionUtils {
    public static final int REQUEST_WRITE_EXTERNAL_STORAGE = 203;
    public static final int REQUEST_WRITE_EXTERNAL_STORAGE_GALLERY = 206;
    public static final int REQUEST_CAMERA = 204;
    public static final int REQUEST_ACCESS_LOCATION = 205;

    public static int checkPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission);

    }

    public static void requestForPermission(Activity activity, String permission, int requestCode) {
        ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
//        ActivityCompat.shouldShowRequestPermissionRationale(activity,permission);
    }

    public static void requestForPermission(Activity activity, String[] permission, int requestCode) {
        ActivityCompat.requestPermissions(activity, permission, requestCode);
//        ActivityCompat.shouldShowRequestPermissionRationale(activity,permission);
    }
}
