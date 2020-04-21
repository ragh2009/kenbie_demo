package com.kenbie.fragments;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.textfield.TextInputLayout;
import com.kenbie.KenbieApplication;
import com.kenbie.R;
import com.kenbie.adapters.AutoCompleteAdapter;
import com.kenbie.listeners.APIResponseHandler;
import com.kenbie.model.LocationItem;
import com.kenbie.util.Constants;
import com.kenbie.util.RuntimePermissionUtils;
import com.kenbie.util.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignUpStepThree extends BaseFragment implements View.OnClickListener, APIResponseHandler {
    private String IMAGE_CAPTURE_URI;
    private static AppCompatEditText etDOB;
    private ImageView profileImg, cameraImage;
    private AppCompatEditText emailEt, etName, etPassword, etCompany;
    private AutoCompleteTextView locationSearch;
    private AutoCompleteAdapter adapter;
    private ArrayList<LocationItem> locationItemArrayList;
    private CheckBox tAgreeCB, pAgreeCB;

    public SignUpStepThree() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View convertView = inflater.inflate(R.layout.fragment_sign_up_step_three, container, false);

        TextView title3 = (TextView) convertView.findViewById(R.id.title3);
        title3.setTypeface(KenbieApplication.S_NORMAL);
        title3.setText(activity.mPref.getString("33", "UPLOAD PHOTO"));

        LinearLayout companyLayout = convertView.findViewById(R.id.company_info);
        if (activity.userType == 2)
            companyLayout.setVisibility(View.VISIBLE);

        TextView companyTitle = (TextView) convertView.findViewById(R.id.company_title);
        companyTitle.setText(activity.mPref.getString("186", "COMPANY") + getString(R.string.asteriskred));
        companyTitle.setTypeface(KenbieApplication.S_SEMI_BOLD);

        etCompany = convertView.findViewById(R.id.et_company);
        etCompany.setTypeface(KenbieApplication.S_NORMAL);
        etCompany.setHint(activity.mPref.getString("155", "Company"));
        etCompany.setFocusableInTouchMode(true);

        TextView emailTitle = (TextView) convertView.findViewById(R.id.email_title);
        emailTitle.setText(activity.mPref.getString("12", "EMAIL ADDRESS") + getString(R.string.asteriskred));
        emailTitle.setTypeface(KenbieApplication.S_SEMI_BOLD);

        emailEt = convertView.findViewById(R.id.et_email);
        emailEt.setTypeface(KenbieApplication.S_NORMAL);
        emailEt.setHint(activity.mPref.getString("16", "Email Address"));
        emailEt.setFocusableInTouchMode(true);

        TextView pwdTitle = (TextView) convertView.findViewById(R.id.pwd_title);
        pwdTitle.setTypeface(KenbieApplication.S_SEMI_BOLD);
        pwdTitle.setText(activity.mPref.getString("13", "PASSWORD") + getString(R.string.asteriskred));

        etPassword = convertView.findViewById(R.id.et_password);
        etPassword.setTypeface(KenbieApplication.S_NORMAL);
        etPassword.setHint(activity.mPref.getString("17", "Password"));
        etPassword.setFocusableInTouchMode(true);

        TextView nameTitle = (TextView) convertView.findViewById(R.id.name_title);
        nameTitle.setTypeface(KenbieApplication.S_SEMI_BOLD);
        nameTitle.setText(activity.mPref.getString("34", "NAME") + getString(R.string.asteriskred));

        etName = convertView.findViewById(R.id.et_name);
        etName.setTypeface(KenbieApplication.S_NORMAL);
        etName.setHint(activity.mPref.getString("35", "Name"));
        etName.setFocusableInTouchMode(true);

        TextView birthTitle = (TextView) convertView.findViewById(R.id.birth_title);
        birthTitle.setTypeface(KenbieApplication.S_SEMI_BOLD);
        birthTitle.setText(activity.mPref.getString("36", "BIRTHDATE") + getString(R.string.asteriskred));

        etDOB = convertView.findViewById(R.id.et_birthday);
        etDOB.setTypeface(KenbieApplication.S_NORMAL);
        etDOB.setHint(activity.mPref.getString("37", "Birthday"));
        etDOB.setOnClickListener(this);
        etDOB.setFocusableInTouchMode(true);

        TextView locTitle = (TextView) convertView.findViewById(R.id.loc_title);
        locTitle.setTypeface(KenbieApplication.S_SEMI_BOLD);
        locTitle.setText(activity.mPref.getString("38", "LOCATION") + getString(R.string.asteriskred));

        locationSearch = (AutoCompleteTextView) convertView.findViewById(R.id.et_city_search);
        locationSearch.setHint(activity.mPref.getString("39", "Location"));
        locationSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LocationItem cityValue = adapter.getItem(position);
                locationSearch.setText(cityValue.getCity() + ", " + cityValue.getStateProv() + ", " + cityValue.getCountryName());
                locationSearch.setSelection(locationSearch.getText().toString().length());
                locationSearch.setTag(cityValue);
                hideKeyboard(activity);
            }
        });

        locationSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int
                    count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                gettingLocationData(s.toString());

//                handler.removeMessages(TRIGGER_AUTO_COMPLETE);
//                handler.sendEmptyMessageDelayed(TRIGGER_AUTO_COMPLETE,
//                        AUTO_COMPLETE_DELAY);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        tAgreeCB = (CheckBox) convertView.findViewById(R.id.t_agree);
        tAgreeCB.setTypeface(KenbieApplication.S_SEMI_LIGHT);
        tAgreeCB.setText(activity.mPref.getString("41", "I agree with the"));

        TextView termAction = (TextView) convertView.findViewById(R.id.terms_action);
        termAction.setText(activity.mPref.getString("8", "Terms of Service"));
        termAction.setTypeface(KenbieApplication.S_SEMI_LIGHT);
        termAction.setOnClickListener(this);

        pAgreeCB = (CheckBox) convertView.findViewById(R.id.p_agree);
        pAgreeCB.setTypeface(KenbieApplication.S_SEMI_LIGHT);
        pAgreeCB.setText(activity.mPref.getString("41", "I agree with the"));

        TextView ppAction = (TextView) convertView.findViewById(R.id.pp_action);
        ppAction.setText(activity.mPref.getString("9", "Privacy Policy"));
        ppAction.setTypeface(KenbieApplication.S_SEMI_LIGHT);
        ppAction.setOnClickListener(this);

        TextView submitBtn = (TextView) convertView.findViewById(R.id.submit_btn);
        submitBtn.setTypeface(KenbieApplication.S_SEMI_BOLD);
        submitBtn.setText(activity.mPref.getString("40", "Create An Account"));
        submitBtn.setOnClickListener(this);

        ((TextView) convertView.findViewById(R.id.step_count)).setTypeface(KenbieApplication.S_NORMAL);

        LinearLayout imgClick = (LinearLayout) convertView.findViewById(R.id.img_click);

//        ((LinearLayout) convertView.findViewById(R.id.img_click)).setOnClickListener(this);
        profileImg = (ImageView) convertView.findViewById(R.id.profile_img);
        cameraImage = (ImageView) convertView.findViewById(R.id.camera_image);

        if (activity.email != null)
            emailEt.setText(activity.email);
        if (activity.name != null)
            etName.setText(activity.name);
        if (activity.password != null)
            etPassword.setText(activity.password);
        if (activity.dob != null) {
            activity.dob = activity.utility.checkDateFormat(activity.dob);
            etDOB.setText(activity.dob);
        }

        if (activity.loginType != 0) {
            ((TextInputLayout) convertView.findViewById(R.id.et_password_layout)).setVisibility(View.GONE);
            etPassword.setVisibility(View.GONE);
            pwdTitle.setVisibility(View.GONE);
            title3.setVisibility(View.GONE);

            if (activity.imageUrl != null && activity.imageUrl.length() > 10) {
                cameraImage.setVisibility(View.GONE);
                profileImg.setVisibility(View.VISIBLE);
//                Glide.with(activity)
//                        .load(activity.imageUrl)
//                        .into(profileImg);
                Glide.with(activity).load(activity.imageUrl).apply(RequestOptions.circleCropTransform()).into(profileImg);
            } else {
                profileImg.setVisibility(View.GONE);
                imgClick.setVisibility(View.VISIBLE);
                imgClick.setOnClickListener(this);
            }
        } else {
            imgClick.setOnClickListener(this);
        }

        gettingLocationData(null);
        return convertView;
    }

    @Override
    public void onResume() {
        activity.step = 3;
        updateUIData();
        super.onResume();
    }


    private void gettingLocationData(String searchStr) {
        try {
            Map<String, String> params = new HashMap<String, String>();
            if (searchStr == null) {
                params.put("ip", activity.ip);
            } else
                params.put("city_search", searchStr);
            params.put("device_id", activity.deviceId == null ? "" : activity.deviceId);
            activity.mConnection.postRequestWithHttpHeaders(activity, "getLocation", this, params, 101);
        } catch (Exception e) {
            Log.d("HUS", "EXCEPTION " + e);
        }
    }

    private void updateUIData() {
        try {
            if (activity.email != null)
                emailEt.setText(activity.email);
            if (activity.name != null)
                etName.setText(activity.name);
            if (activity.password != null)
                etPassword.setText(activity.password);
            if (activity.dob != null)
                etDOB.setText(activity.dob);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_click:
                selectProfileImage();
                break;

            case R.id.submit_btn:
                activity.locationItem = (LocationItem) locationSearch.getTag();
                if (isInfoValid()) {
                    activity.email = emailEt.getText().toString();
                    activity.name = etName.getText().toString();
//                    activity.phone = etMobile.getText().toString();
                    activity.phone = "";
                    activity.password = etPassword.getText().toString();
                    activity.dob = etDOB.getText().toString();
                    activity.companyName = etCompany.getText().toString();
                    hideKeyboard(activity);

//                    if (activity.loginType == 0)
                    activity.startSignUpProcess(101);
//                    else
//                        activity.gettingSocialSignUpDetails();
                }
                break;

            case R.id.et_birthday:
                hideKeyboard(activity);
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(activity.getSupportFragmentManager(), "datePicker");
                break;
            case R.id.terms_action:
//                Intent intent = new Intent(activity, KenbieWebActivity.class);
//                intent.putExtra("Type", 2);
//                intent.putExtra("URL", Constants.TERMS_URL);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                startActivity(intent);
                break;
            case R.id.pp_action:
//                Intent pIntent = new Intent(activity, KenbieWebActivity.class);
//                pIntent.putExtra("Type", 1);
//                pIntent.putExtra("URL", Constants.PRIVACY_URL);
//                pIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                startActivity(pIntent);
                break;
            default:
                break;
        }
    }

    private boolean isInfoValid() {
        if (activity.loginType == 0 && activity.imgPath == null) {
            activity.showMessageWithTitle(activity, activity.mPref.getString("20", "Alert!"), activity.mPref.getString("47", "Please upload your profile photo"));
            return false;
        } else if (activity.loginType != 0 && activity.imageUrl == null && activity.imgPath == null) {
            activity.showMessageWithTitle(activity, activity.mPref.getString("20", "Alert!"), activity.mPref.getString("47", "Please upload your profile photo"));
            return false;
        } else if (activity.userType == 2 && etCompany.getText().toString().equalsIgnoreCase("")) {
            activity.showMessageWithTitle(activity, activity.mPref.getString("20", "Alert!"), activity.mPref.getString("360", "Company name is empty"));
            return false;
        } else if (emailEt.getText().toString().equalsIgnoreCase("")) {
            activity.showMessageWithTitle(activity, activity.mPref.getString("20", "Alert!"), activity.mPref.getString("42", "The Email Address field is required."));
            return false;
        } else if (!activity.isValidEmail(emailEt.getText().toString(), activity)) {
            activity.showMessageWithTitle(activity, activity.mPref.getString("20", "Alert!"), activity.mPref.getString("264", "Please enter valid email id"));
            return false;
        } else if (activity.loginType == 0 && etPassword.getText().toString().equalsIgnoreCase("")) {
            activity.showMessageWithTitle(activity, activity.mPref.getString("20", "Alert!"), activity.mPref.getString("44", "The Password field is required."));
            return false;
        } else if (etName.getText().toString().equalsIgnoreCase("")) {
            activity.showMessageWithTitle(activity, activity.mPref.getString("20", "Alert!"), activity.mPref.getString("43", "The Name field is required."));
            return false;
        } else if (etDOB.getText().toString().equalsIgnoreCase(activity.mPref.getString("37", "Birthday"))) {
            activity.showMessageWithTitle(activity, activity.mPref.getString("20", "Alert!"), activity.mPref.getString("45", "The Birthday field is required."));
            return false;
        } else if (activity.locationItem == null) {
            activity.showMessageWithTitle(activity, activity.mPref.getString("20", "Alert!"), activity.mPref.getString("46", "The Location field is required."));
            return false;
        } else if (!tAgreeCB.isChecked()) {
            activity.showMessageWithTitle(activity, activity.mPref.getString("20", "Alert!"), activity.mPref.getString("10", "Please check Privacy & Terms."));
            return false;
        } else if (!pAgreeCB.isChecked()) {
            activity.showMessageWithTitle(activity, activity.mPref.getString("20", "Alert!"), activity.mPref.getString("10", "Please check Privacy & Terms."));
            return false;
        } else if (!activity.isOnline()) {
            activity.showMessageWithTitle(activity, activity.mPref.getString("20", "Alert!"), activity.mPref.getString("269", "Network failed! Please try later."));
            return false;
        }

        return true;
    }

    private boolean checkPermissions() {
        List<String> neededPermissions = new ArrayList<>();

        if (RuntimePermissionUtils.checkPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            neededPermissions.add(Manifest.permission.CAMERA);
        }
        if (RuntimePermissionUtils.checkPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            neededPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        String[] permissions = neededPermissions.toArray(new String[neededPermissions.size()]);
        if (neededPermissions.size() > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permissions, RuntimePermissionUtils.REQUEST_CAMERA);
            }
        } else {
            return true;
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void selectProfileImage() {
        Utility.showPictureDialog(activity, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermissions())
                    startCamera();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkGalleryPermissions()) {
                    openPhoneGallery();
                }
            }
        });
    }
    private boolean checkGalleryPermissions() {
        List<String> neededPermissions = new ArrayList<>();

        if (RuntimePermissionUtils.checkPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            neededPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if (RuntimePermissionUtils.checkPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            neededPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        String[] permissions = neededPermissions.toArray(new String[neededPermissions.size()]);
        if (neededPermissions.size() > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permissions, RuntimePermissionUtils.REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        } else {
            return true;
        }
        return false;
    }

    private void startCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = activity.utility.createImageFile(activity);
                IMAGE_CAPTURE_URI = photoFile.getAbsolutePath();
            } catch (Exception ex) {
                // Error occurred while creating the File
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(activity,
                        "com.kenbie.provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, Constants.CAMERA_CLICK);
            }
        }

//        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
//        StrictMode.setVmPolicy(builder.build());
   /*     IMAGE_CAPTURE_URI = activity.utility.createImageFile(activity);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        IMAGE_CAPTURE_URI = Utility.getOutputMediaFileUri();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, IMAGE_CAPTURE_URI);
        startActivityForResult(intent, Constants.CAMERA_CLICK);*/
    }

    private void openPhoneGallery() {
        try {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, Constants.GALLERY_CLICK);
        } catch (Exception e) {
            e.printStackTrace();
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, Constants.GALLERY_CLICK);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case RuntimePermissionUtils.REQUEST_CAMERA:
                if (grantResults != null && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    if (grantResults.length > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        startCamera();
                    } else {
                        startCamera();
                    }
                break;
            case RuntimePermissionUtils.REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults != null && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    openPhoneGallery();
                break;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.CAMERA_CLICK && resultCode == Activity.RESULT_OK) {
//            galleryAddPic();
            try {
                File f = new File(IMAGE_CAPTURE_URI);
                Uri contentUri = Uri.fromFile(f);
//            Bundle extras = data.getExtras();
//            activity.profilePicBitmap = (Bitmap) extras.get("data");
                activity.profilePicBitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), contentUri);
//            activity.profilePicBitmap = Utility.getImageFromCamera(activity, IMAGE_CAPTURE_URI);
                if (activity.profilePicBitmap != null) {
                    activity.imgPath = IMAGE_CAPTURE_URI;
                    activity.profilePicBitmap = rotateImageIfRequired(activity.profilePicBitmap, contentUri);
                    activity.profilePicBitmap = getResizedBitmap(activity.profilePicBitmap, 500);

                    cameraImage.setVisibility(View.GONE);
                    profileImg.setVisibility(View.VISIBLE);
                    profileImg.setImageBitmap(activity.profilePicBitmap);
                } else {
                    cameraImage.setVisibility(View.VISIBLE);
                    profileImg.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (requestCode == Constants.GALLERY_CLICK && resultCode == activity.RESULT_OK) {
            try {
                if (data != null) {
                    Uri _uri = data.getData();
                    if (_uri != null) {
                        Cursor cursor = activity.getContentResolver().query(_uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
                        if (cursor != null)
                            cursor.moveToFirst();
                        try {
                            activity.imgPath = cursor.getString(0);
                            cursor.close();

                            activity.profilePicBitmap = getBitmapFromUri(_uri);

//                            profilePicBitmap = StaticUtils.getResizeImage(activity, StaticUtils.PROFILE_IMAGE_SIZE, StaticUtils.PROFILE_IMAGE_SIZE, ScalingUtilities.ScalingLogic.CROP, true, imagePath, _uri);
                            if (activity.profilePicBitmap != null) {
                                cameraImage.setVisibility(View.GONE);
                                profileImg.setVisibility(View.VISIBLE);
                                profileImg.setImageBitmap(activity.profilePicBitmap);
                            } else {
                                cameraImage.setVisibility(View.VISIBLE);
                                profileImg.setVisibility(View.GONE);
                            }
                        } catch (Exception e) {
                            e.getStackTrace();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static Bitmap rotateImageIfRequired(Bitmap img, Uri selectedImage) throws IOException {

        ExifInterface ei = new ExifInterface(selectedImage.getPath());
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 0) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(IMAGE_CAPTURE_URI);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        activity.sendBroadcast(mediaScanIntent);
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        try {
            ParcelFileDescriptor parcelFileDescriptor =
                    activity.getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
            return image;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void getError(String error, int APICode) {

    }

    @Override
    public void getResponse(String response, int APICode) {
        try {
            if (response != null) {
                JSONObject jo = new JSONObject(response);
                if (jo.getBoolean("status")) {
                    JSONArray jsonArray = jo.getJSONArray("data");
                    locationItemArrayList = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jo1 = jsonArray.getJSONObject(i);
                        LocationItem locationItem = new LocationItem();
                        locationItem.setCity(jo1.getString("city"));
                        locationItem.setStateProv(jo1.getString("stateprov"));
                        locationItem.setCountry(jo1.getString("country"));
                        locationItem.setZipCode(jo1.getString("zipcode"));
                        locationItem.setLatitude((float) jo1.getDouble("latitude"));
                        locationItem.setLongitude((float) jo1.getDouble("longitude"));
                        locationItem.setCountryName(jo1.getString("country_name_en"));
                        locationItem.setCountryId(jo1.getInt("country_id"));
                        locationItemArrayList.add(locationItem);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        bindData();

    }

    private void bindData() {
        if (adapter == null) {
//            adapter = new AutoCompleteAdapter(activity, android.R.layout.simple_dropdown_item_1line, locationItemArrayList);
            adapter = new AutoCompleteAdapter(activity, android.R.layout.simple_spinner_dropdown_item, locationItemArrayList);
            locationSearch.setAdapter(adapter);
        } else {
            adapter.refreshData(locationItemArrayList);
        }
    }

    @Override
    public void networkError(String error, int APICode) {

    }


    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR) - 16;
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datepickerdialog = new DatePickerDialog(getActivity(),
                    R.style.datepicker, this, year, month, day);
            datepickerdialog.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000);
            return datepickerdialog;
            // Create a new instance of DatePickerDialog and return it
//            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            StringBuilder date = new StringBuilder().append(day).append("-")
                    .append(month + 1).append("-").append(year);
            etDOB.setText(Utility.getDateFormat(date.toString()));
        }
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
