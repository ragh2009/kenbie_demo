package com.kenbie.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.kenbie.KenbieActivity;
import com.kenbie.SignUpActivity;

public class BaseFragment extends Fragment {
    public SignUpActivity activity;
    public KenbieActivity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() instanceof SignUpActivity)
            activity = ((SignUpActivity) getActivity());
        else if (getActivity() instanceof KenbieActivity)
            mActivity = ((KenbieActivity) getActivity());
    }
}
