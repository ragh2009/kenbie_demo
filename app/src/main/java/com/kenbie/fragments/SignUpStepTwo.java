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

public class SignUpStepTwo extends BaseFragment implements View.OnClickListener, RadioButton.OnCheckedChangeListener {
    private int userType = 0;
    private RadioButton rbMale, rbFemale;
    private TextView saveBtn;

    public SignUpStepTwo() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View convertView = inflater.inflate(R.layout.fragment_sign_up_step_two, container, false);


        TextView title1 = (TextView) convertView.findViewById(R.id.title1);
        title1.setTypeface(KenbieApplication.S_SEMI_BOLD);
        title1.setText(activity.mPref.getString("5", "Create Account"));

        TextView title2 = (TextView) convertView.findViewById(R.id.title2);
        title2.setTypeface(KenbieApplication.S_NORMAL);
        title2.setText(activity.mPref.getString("26", "Please enter your details for explore the world of modeling."));

        TextView title3 = (TextView) convertView.findViewById(R.id.title3);
        title3.setTypeface(KenbieApplication.S_NORMAL);
        title3.setText(activity.mPref.getString("27", "TELL US WHO YOU ARE?"));

        rbMale = (RadioButton) convertView.findViewById(R.id.rb_male);
        rbMale.setOnCheckedChangeListener(this);
        rbMale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userType = 1;
                saveBtn.performClick();
            }
        });

        TextView rbMaleTxt = (TextView) convertView.findViewById(R.id.male_title);
        rbMaleTxt.setTypeface(KenbieApplication.S_NORMAL);
        rbMaleTxt.setText(activity.mPref.getString("31", "Male"));

        rbFemale = (RadioButton) convertView.findViewById(R.id.rb_female);
        rbFemale.setOnCheckedChangeListener(this);
        rbFemale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userType = 2;
                saveBtn.performClick();
            }
        });

        TextView rbFemaleTxt = (TextView) convertView.findViewById(R.id.female_title);
        rbFemaleTxt.setTypeface(KenbieApplication.S_NORMAL);
        rbFemaleTxt.setText(activity.mPref.getString("32", "Female"));

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
    public void onClick(View v) {
        if (v.getId() == R.id.save_btn) {
            if (userType != 0)
                activity.nextNavigation(3, userType);
            else
                activity.showMessageWithTitle(getActivity(), activity.mPref.getString("20", "Alert!"), activity.mPref.getString("305", "The Gender field is required."));
        }
    }

    @Override
    public void onResume() {
        activity.step = 2;
        if (activity.sexType == 1)
            rbMale.setChecked(true);
        else if (activity.sexType == 2)
            rbFemale.setChecked(true);
        super.onResume();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.rb_male) {
            if (isChecked) {
//                userType = 1;
                rbMale.setChecked(true);
                rbFemale.setChecked(false);
//                saveBtn.performClick();
            }
        } else if (buttonView.getId() == R.id.rb_female) {
            if (isChecked) {
//                userType = 2;
                rbMale.setChecked(false);
                rbFemale.setChecked(true);
//                saveBtn.performClick();
            }
        }
    }
}
