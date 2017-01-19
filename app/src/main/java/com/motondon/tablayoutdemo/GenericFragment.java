package com.motondon.tablayoutdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.motondon.tablayoutdemo_part_2.R;

/**
 * Created by Joca on 4/6/2016.
 */
public class GenericFragment extends BaseFragment {

    // Create an array of pre-defined icons in order to set it at real-time by using the appropriated icon.
    private static int[] ICON_ID_LIST = new int[] {
            R.drawable.ic_filter_1_white_24dp,
            R.drawable.ic_filter_2_white_24dp,
            R.drawable.ic_filter_3_white_24dp,
            R.drawable.ic_filter_4_white_24dp};

    public int ICON_ID = ICON_ID_LIST[0];
    public String ITEM_TEXT = "Generic ";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    public void mountFragmentName(int genericFragmentCount) {
    	ITEM_TEXT = ITEM_TEXT + genericFragmentCount;
        ICON_ID = ICON_ID_LIST[genericFragmentCount -1];
    }

    /**
     * Used when ORIENTATION_CHANGE_METHOD flag is RECREATE_FRAGMENT. See ViewPagerAdapter class scope comments for details.
     *
     * @param fragmentName
     */
    public void setFragmentName(String fragmentName) {
        ITEM_TEXT = fragmentName;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_generic, container, false);

        // Set frag name to the textView so that we can visualize the name of the frag easily
        TextView textView = (TextView) root.findViewById(R.id.tv_frag_name);
        textView.setText(ITEM_TEXT);

        return root;
    }

    @Override
    public String getFragmentName() {
        return ITEM_TEXT;
    }
}
