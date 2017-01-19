package com.motondon.tablayoutdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import java.io.Serializable;

/**
 * Created by Joca on 12/20/2016.
 */

public abstract class BaseFragment extends Fragment implements Serializable {

    public abstract String getFragmentName();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // In case of using retaining fragment method, we need to set all fragments to setRetainInstance(true). See ViewPagerAdapter class scope comments for details
        if (((MainActivity)getActivity()).ORIENTATION_CHANGE_METHOD == MainActivity.OrientationChangeMethod.RETAIN_FRAGMENT) {
            setRetainInstance(true);
        }
    }
}
