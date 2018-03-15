package com.example.rexchen.myim.ui.fragment;

import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.rexchen.myim.server.widget.SelectableRoundedImageView;

/**
 * Created by Rex on 2018/3/15.
 * Email chenhm4444@gmail.com
 */

public class MineFragment extends Fragment implements View.OnClickListener {

    private static final int COMPARE_VERSION = 54;
    public static final String SHOW_RED = "SHOW_RED";
    private SharedPreferences sp;
    private SelectableRoundedImageView imageView;
    private TextView mName;
    private ImageView mNewVersionView;
    private boolean isHasNewVersion;
    private String url;
    private boolean isDebug;

    @Override
    public void onClick(View view) {

    }
}
