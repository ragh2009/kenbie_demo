package com.kenbie.util;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.kenbie.KenbieApplication;
import com.kenbie.R;

import org.apache.commons.codec.binary.Base64;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.WIFI_SERVICE;
import static java.util.Calendar.DATE;
import static java.util.Calendar.MONTH;

/**
 * Created by rajaw on 6/6/2017.
 */

public class Utility {
    public static final String GALLERY_FOLDER_PATH = Environment.getExternalStorageDirectory().getPath() + "/" + "kenbie" + "/";

    public static Uri getOutputMediaFileUri() {
        return Uri.fromFile(getOutputMediaFile());
    }

    public static File getOutputMediaFile() {
        long current = System.currentTimeMillis();
        File dir = new File(GALLERY_FOLDER_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (dir.exists()) {
            File file = null;
            try {
                file = new File(GALLERY_FOLDER_PATH + current + ".jpg");
                if (file.exists())
                    file.delete();
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return file;
        } else {
            return null;
        }
    }

    public static File saveTempOutputMediaFile(String fileName) {
        File dir = new File(GALLERY_FOLDER_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (dir.exists()) {
            File file = null;
            try {
                file = new File(GALLERY_FOLDER_PATH + fileName + ".jpg");
                if (file.exists())
                    file.delete();
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return file;
        } else {
            return null;
        }
    }

    public String createDirectoryAndSaveFile(Bitmap imageToSave, String fileName) {

        File direct = new File(GALLERY_FOLDER_PATH);

        if (!direct.exists()) {
            File wallpaperDirectory = new File("/sdcard/DirName/");
            wallpaperDirectory.mkdirs();
        }

        File file = new File("/sdcard/DirName/", fileName + ".jpg");
        if (file.exists()) {
            file.delete();
        }

//        File myImgPath = saveTempOutputMediaFile(fileName);
        try {
            FileOutputStream out = new FileOutputStream(file);
            imageToSave.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return file.getAbsolutePath();
    }

    public File createImageFile(Context context) {
        File image = null;
        try {
            // Create an image file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Save a file: path for use with ACTION_VIEW intents
//        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public static int getColorWithAlpha(int color, float ratio) {
        int newColor = 0;
        int alpha = Math.round(Color.alpha(color) * ratio);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        newColor = Color.argb(alpha, r, g, b);
        return newColor;
    }

    public static float dpToPx(Context context, float valueInDp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
    }

    public static void showPictureDialog(Context context, final View.OnClickListener onCameraClick,
                                         final View.OnClickListener onGalleryClick) {
        SharedPreferences mPref = context.getSharedPreferences("kPrefs", MODE_PRIVATE);

        final Dialog dialog = new Dialog(context);
        dialog.setCancelable(true);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_dialog_picker);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView txtTitle = (TextView) dialog.findViewById(R.id.txtTitle);
        txtTitle.setText(mPref.getString("48", "Upload Photo"));
        txtTitle.setTypeface(KenbieApplication.S_BOLD);

        TextView txtTitle2 = (TextView) dialog.findViewById(R.id.txtTitle2);
        txtTitle2.setText("");
        txtTitle2.setTypeface(KenbieApplication.S_NORMAL);
        txtTitle2.setVisibility(View.GONE);

        TextView txtCamera = (TextView) dialog.findViewById(R.id.txtCamera);
        txtCamera.setTypeface(KenbieApplication.S_NORMAL);
        txtCamera.setText(mPref.getString("49", "Camera"));

        TextView txtGallery = (TextView) dialog.findViewById(R.id.txtGallery);
        txtGallery.setText(mPref.getString("50", "Gallery"));
        txtGallery.setTypeface(KenbieApplication.S_NORMAL);

        txtCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (onCameraClick != null) {
                    onCameraClick.onClick(v);
                }
            }
        });

        txtGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (onGalleryClick != null) {
                    onGalleryClick.onClick(v);
                }
            }
        });

        dialog.show();
    }

    public static String getDateFormat(String date) { // 11\/29\/1987
        SimpleDateFormat getFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
//        SimpleDateFormat setFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        Date convertedDate = new Date();

        try {
            convertedDate = getFormat.parse(date);
        } catch (java.text.ParseException e) {
            return date;
        }

        String convertedDateString = getFormat.format(convertedDate);
        return convertedDateString;
    }

    public static String getDayMonthDateFormat(String date) { // 11\/29\/1987
        SimpleDateFormat getFormat = new SimpleDateFormat("MM-dd-yyyy", Locale.US);
        SimpleDateFormat setFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        Date convertedDate = new Date();

        try {
            convertedDate = getFormat.parse(date);
        } catch (java.text.ParseException e) {
            return date;
        }

        String convertedDateString = setFormat.format(convertedDate);
        return convertedDateString;
    }

    public static String getMonthDateFormat(String date) { // 31-08-2019
        SimpleDateFormat getFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        SimpleDateFormat setFormat = new SimpleDateFormat("dd MMM", Locale.US);
        Date convertedDate = new Date();

        try {
            convertedDate = getFormat.parse(date);
        } catch (java.text.ParseException e) {

            e.printStackTrace();
        }
        String convertedDateString = setFormat.format(convertedDate);

        return convertedDateString;
    }

    public static String getMonthFormat(String date) { // 31-08-2019
        SimpleDateFormat getFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        SimpleDateFormat setFormat = new SimpleDateFormat("dd/MM", Locale.US);
        Date convertedDate = new Date();

        try {
            convertedDate = getFormat.parse(date);
        } catch (java.text.ParseException e) {

            e.printStackTrace();
        }
        String convertedDateString = setFormat.format(convertedDate);

        return convertedDateString;
    }

    public static String getMessageTime() {
        String value = "";
        try { // "01 Feb, 2019, 06:18",
            Calendar cal = Calendar.getInstance();
            TimeZone tz = cal.getTimeZone();
            DateFormat sdf = new SimpleDateFormat("dd MMM, yyyy, HH:mm", Locale.getDefault());
            Date date = new Date();

            try {
                sdf.setTimeZone(tz);
                value = sdf.format(date);
            } catch (Exception e) {

                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public int getYearsCountFromDate(String year, String month, String day) {
        int value = 0;
        try {
            SimpleDateFormat getFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
            Date date = new Date();
            date = getFormat.parse(day + "-" + month + "-" + year);
//            Calendar birthDay = new GregorianCalendar(year, (Integer.valueOf(month) - 1), Integer.valueOf(day));
            Calendar birthDay = new GregorianCalendar();
            birthDay.setTime(date);
            Calendar today = new GregorianCalendar();

            today.setTime(new Date());

            value = today.get(Calendar.YEAR)
                    - birthDay.get(Calendar.YEAR);
       //     int diff = b.get(YEAR) - a.get(YEAR);
            if (birthDay.get(MONTH) > today.get(MONTH) ||
                    (birthDay.get(MONTH) == today.get(MONTH) && birthDay.get(DATE) > today.get(DATE))) {
                value--;
            }

//            value = value - 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static String getDeviceId(SharedPreferences mPref, Context context) {
        String value = "";
        try {
            value = mPref.getString("DeviceId", "");
            if (value != null && value.length() == 0)
                value = Settings.Secure.getString(context.getContentResolver(),
                        Settings.Secure.ANDROID_ID);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    private String getTermsDataFromAssets(Context context, int type) {
        String mLine = null;
        try {
            StringBuilder buf = new StringBuilder();
            InputStream json;
            if (type == 1) // 1- terms
                json = context.getAssets().open("terms.txt");
            else // 2 - privacy
                json = context.getAssets().open("privacy.txt");

            BufferedReader in =
                    new BufferedReader(new InputStreamReader(json, "UTF-8"));
            String str;

            while ((str = in.readLine()) != null) {
                buf.append(str);
            }
            mLine = buf.toString();

            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mLine;
    }

    public static Bitmap getImageFromCamera(Context mContext, Uri IMAGE_CAPTURE_URI) {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (manufacturer.equalsIgnoreCase("samsung") || model.equalsIgnoreCase("samsung")) {
            int rotation = getCameraPhotoOrientation(mContext, IMAGE_CAPTURE_URI, IMAGE_CAPTURE_URI.getPath());
            Log.e("Rotate", String.valueOf(rotation));
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 8;
            final Bitmap bitmap = BitmapFactory.decodeFile(IMAGE_CAPTURE_URI.getPath(), options);
            Bitmap orignalBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            return orignalBitmap;
        } else {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 8;
            final Bitmap bitmap = BitmapFactory.decodeFile(IMAGE_CAPTURE_URI.getPath(), options);
            return bitmap;
        }
    }

    public static int getCameraPhotoOrientation(Context context, Uri imageUri, String imagePath) {
        int rotate = 0;
        try {
            try {
                if (imageUri != null)
                    context.getContentResolver().notifyChange(imageUri, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            File imageFile = new File(imagePath);
            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
                case ExifInterface.ORIENTATION_NORMAL:
                    rotate = 0;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }


    public String checkDateFormat(String dob) {
        String value = dob;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            Date sourceDate = null;
            try {
                sourceDate = dateFormat.parse(dob);
            } catch (Exception e) {
                e.printStackTrace();
                return getDisplayDateFormat(dob);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public String getDisplayDateFormat(String givenDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        Date sourceDate = null;
        try {
            sourceDate = dateFormat.parse(givenDate);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        SimpleDateFormat targetFormat = new SimpleDateFormat("dd-MM-yyyy");
        return targetFormat.format(sourceDate);
    }

    public String getDisplayDDMMYYYYFormat(String givenDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date sourceDate = null;
        try {
            sourceDate = dateFormat.parse(givenDate);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        SimpleDateFormat targetFormat = new SimpleDateFormat("dd-MM-yyyy");
        return targetFormat.format(sourceDate);
    }

    public String md5(String s) {
        try {
            MessageDigest digest = java.security.MessageDigest
                    .getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public boolean isOnline(Context context) {
        boolean isConnected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            isConnected = cm.getActiveNetworkInfo().isConnectedOrConnecting();
        } catch (Exception ex) {
            isConnected = false;
        }
        return isConnected;
    }

    public String getIpAddress(Context context) {
        WifiManager wm = (WifiManager) context.getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        return ip;
    }
}
