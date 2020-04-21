package com.kenbie;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.kenbie.listeners.InfoListener;
import com.kenbie.util.Constants;
import com.kenbie.views.CameraPreview;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressWarnings("deprecation")
public class CameraActivity extends AppCompatActivity implements InfoListener {
    private static final String TAG = "Camera_functions";
    private Camera mCamera;
    private CameraPreview mPreview;
    private String mCurrentPhotoPath;
    private ImageButton captureButton, cancelBtn, doneBtn, btnCameraFace, btnCameraFlash;
    private int bitmapRotation = 0;
    private int currentCameraId = 0;
    private FrameLayout preview;
    private File pictureFile;
    private ImageView previewImage;
    private boolean isFlash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Create an instance of Camera
        releaseCamera();
        mCamera = getCameraInstance();

        preview = (FrameLayout) findViewById(R.id.camera_preview);
        previewImage = (ImageView) findViewById(R.id.image_preview);

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera, isFlash, this);
        preview.addView(mPreview);

        btnCameraFace = (ImageButton) findViewById(R.id.btn_camera_face);
        btnCameraFace.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                            currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
                            isFlash = false;
                            btnCameraFlash.setBackgroundResource(R.drawable.ic_flash_off);
                            switchCamera();
                        } else {
                            currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
                            switchCamera();
                        }
                    }
                }
        );

        btnCameraFlash = (ImageButton) findViewById(R.id.btn_camera_flash);
        btnCameraFlash.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        if (!isFlash && currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                            isFlash = true;
//                            surfaceChanged();
//                            switchCamera();
                            flashOnCamera();
                            btnCameraFlash.setBackgroundResource(R.drawable.ic_flash_on);
                        } else {
                            isFlash = false;
                            btnCameraFlash.setBackgroundResource(R.drawable.ic_flash_off);
                            flashOnCamera();

//                            switchCamera();
                        }
                    }
                }
        );

        captureButton = (ImageButton) findViewById(R.id.btn_camera);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        try {
                            mCamera.takePicture(null, null, mPicture);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
        );

        cancelBtn = (ImageButton) findViewById(R.id.btn_camera_cancel);
        cancelBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.putExtra("Cancel", true);
                        setResult(Constants.CAMERA_MANUAL, intent);
                        finish();
                    }
                }
        );

        doneBtn = (ImageButton) findViewById(R.id.btn_camera_done);
        doneBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // After capture done
                        Intent intent = new Intent();
                        intent.putExtra("Cancel", false);
                        intent.putExtra("Path", mCurrentPhotoPath);
                        setResult(Constants.CAMERA_MANUAL, intent);
                        finish();
                    }
                }
        );
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(currentCameraId); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] imgData, Camera camera) {
            updateCameraPreviewUi();

            pictureFile = getOutputMediaFile();
            if (pictureFile == null) {
                Log.d(TAG, "Error creating media file, check storage permissions");
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(imgData);
                fos.close();
                galleryAddPic();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    private void updateCameraPreviewUi() {
        try {
            cancelBtn.setVisibility(View.VISIBLE);
            doneBtn.setVisibility(View.VISIBLE);
            captureButton.setVisibility(View.GONE);
            btnCameraFace.setVisibility(View.GONE);
            btnCameraFlash.setVisibility(View.GONE);
            preview.setVisibility(View.VISIBLE);
            previewImage.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void switchCamera() {
        try {
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
            }
            mCamera = getCameraInstance();
//            setCameraDisplayOrientation(this, currentCameraId, mCamera);
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(currentCameraId, info);
//            Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_FRONT, info);
            int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
            int degrees = 0;
            switch (rotation) {
                case Surface.ROTATION_0:
                    degrees = 0;
                    break; //Natural orientation
                case Surface.ROTATION_90:
                    degrees = 90;
                    break; //Landscape left
                case Surface.ROTATION_180:
                    degrees = 180;
                    break;//Upside down
                case Surface.ROTATION_270:
                    degrees = 270;
                    break;//Landscape right
            }

            int rotate = (info.orientation - degrees + 360) % 360;

            //STEP #2: Set the 'rotation' parameter
            Camera.Parameters params = mCamera.getParameters();
            params.setRotation(rotate);
            if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK)
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

            try {
                mCamera.setPreviewDisplay(mPreview.getHolder());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            mCamera.setParameters(params);
            mCamera.setDisplayOrientation(90);
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void flashOnCamera() {
        mCamera.stopPreview();

        Camera.Parameters p = mCamera.getParameters();

        if (isFlash) {
            Log.i("info", "torch is turn off!");
            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            mCamera.setParameters(p);
            mCamera.startPreview();
        } else {
            Log.i("info", "torch is turn on!");
            p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(p);
            mCamera.startPreview();
        }
    }


    private File getOutputMediaFile() {
        try {
            // Create an image file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );

            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = image.getAbsolutePath();
            return image;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseCamera();
    }

    private void releaseCamera() {
        try {
            currentCameraId = 0;
            if (mCamera != null) {
                mCamera.release();        // release the camera for other applications
                mCamera = null;
                mPreview = null;
                System.gc();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("Cancel", true);
        setResult(Constants.CAMERA_MANUAL, intent);
        finish();
        super.onBackPressed();
    }

    private Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        mediaScanIntent.setData(Uri.fromFile(f));
        sendBroadcast(mediaScanIntent);
    }

    @Override
    public void getInfoValue(int parentPos, int childPos) {
        switchCamera();
    }
}
