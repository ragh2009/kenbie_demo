package com.kenbie.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import com.kenbie.KenbieApplication;
import com.kenbie.R;

public class SignUpStepOne extends BaseFragment implements View.OnClickListener, RadioButton.OnCheckedChangeListener {
    private int userType = 0;
    private RadioButton rbModel, rbPhoto, rbAgency;
    private TextView saveBtn;

    public SignUpStepOne() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View convertView = inflater.inflate(R.layout.fragment_sign_up_step_one, container, false);


        TextView title1 = (TextView) convertView.findViewById(R.id.title1);
        title1.setTypeface(KenbieApplication.S_SEMI_BOLD);
        title1.setText(activity.mPref.getString("5", "Create Account"));

        TextView title2 = (TextView) convertView.findViewById(R.id.title2);
        title2.setTypeface(KenbieApplication.S_NORMAL);
        title2.setText(activity.mPref.getString("26", "Please enter your details for explore the world of modeling."));

        TextView title3 = (TextView) convertView.findViewById(R.id.title3);
        title3.setTypeface(KenbieApplication.S_NORMAL);
        title3.setText(activity.mPref.getString("27", "TELL US WHO YOU ARE?"));

        rbModel = (RadioButton) convertView.findViewById(R.id.model_checked);
        rbModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userType = 1;
                saveBtn.performClick();
            }
        });

        rbModel.setOnCheckedChangeListener(this);

        TextView rbModelTxt = (TextView) convertView.findViewById(R.id.model_title);
        rbModelTxt.setTypeface(KenbieApplication.S_NORMAL);
        rbModelTxt.setText(activity.mPref.getString("28", "Model"));

        rbPhoto = (RadioButton) convertView.findViewById(R.id.photo_checked);
        rbPhoto.setOnCheckedChangeListener(this);
        rbPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userType = 3;
                saveBtn.performClick();
            }
        });

        TextView rbPhotoTxt = (TextView) convertView.findViewById(R.id.photo_title);
        rbPhotoTxt.setTypeface(KenbieApplication.S_NORMAL);
        rbPhotoTxt.setText(activity.mPref.getString("29", "Photographer"));

        rbAgency = (RadioButton) convertView.findViewById(R.id.agency_checked);
        rbAgency.setOnCheckedChangeListener(this);
        rbAgency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userType = 2;
                saveBtn.performClick();
            }
        });

        TextView rbAgencyTxt = (TextView) convertView.findViewById(R.id.agency_title);
        rbAgencyTxt.setTypeface(KenbieApplication.S_NORMAL);
        rbAgencyTxt.setText(activity.mPref.getString("30", "Agency"));

        saveBtn = (TextView) convertView.findViewById(R.id.save_btn);
        saveBtn.setTypeface(KenbieApplication.S_SEMI_BOLD);
        saveBtn.setText(activity.mPref.getString("225", "SAVE"));
        saveBtn.setOnClickListener(this);
        saveBtn.setVisibility(View.GONE);

        TextView stepCountTxt = (TextView) convertView.findViewById(R.id.step_count);
        stepCountTxt.setTypeface(KenbieApplication.S_NORMAL);

        if (activity.loginType != 0)
            stepCountTxt.setVisibility(View.INVISIBLE);
        return convertView;
    }

    @Override
    public void onResume() {
        activity.step = 1;
        if (activity.userType == 1)
            rbModel.setChecked(true);
        else if (activity.userType == 2)
            rbAgency.setChecked(true);
        else if (activity.userType == 3)
            rbPhoto.setChecked(true);
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.save_btn) {
            if (userType != 0)
                activity.nextNavigation(2, userType);
            else
                activity.showMessageWithTitle(getActivity(), activity.mPref.getString("20", "Alert!"), activity.mPref.getString("304", "The Tell us who you are ? field is required."));
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.model_checked) {
            if (isChecked) {
                rbModel.setChecked(true);
                rbPhoto.setChecked(false);
                rbAgency.setChecked(false);
//                saveBtn.performClick();
            }
        } else if (buttonView.getId() == R.id.photo_checked) {
            if (isChecked) {
//                userType = 2;
                rbModel.setChecked(false);
                rbPhoto.setChecked(true);
                rbAgency.setChecked(false);
//                saveBtn.performClick();
            }
        } else if (buttonView.getId() == R.id.agency_checked) {
            if (isChecked) {
//                userType = 3;
                rbModel.setChecked(false);
                rbPhoto.setChecked(false);
                rbAgency.setChecked(true);

//                saveBtn.performClick();
            }
        }
    }
}
