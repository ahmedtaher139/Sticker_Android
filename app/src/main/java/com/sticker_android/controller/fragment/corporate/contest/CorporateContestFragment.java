package com.sticker_android.controller.fragment.corporate.contest;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sticker_android.R;
import com.sticker_android.controller.fragment.base.BaseFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class CorporateContestFragment extends BaseFragment {


    public CorporateContestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_corporate_contest, container, false);
    }

    @Override
    protected void setViewListeners() {

    }

    @Override
    protected void setViewReferences(View view) {

    }

    @Override
    protected boolean isValidData() {
        return false;
    }
}