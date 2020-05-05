package com.example.sis;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

public class SupportTabFragment1 extends Fragment {
    Button btn_test;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.support_tab_fragment_1, container, false);
        btn_test = (Button) rootView.findViewById(R.id.btn_sup_guide);
        // btn_test.setText("yes");
        return rootView;
    }
}