package com.example.rexchen.myim.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.rexchen.myim.App;

import com.example.rexchen.myim.SealConst;
import com.example.rexchen.myim.SealUserInfoManager;
import com.example.rexchen.myim.server.SealAction;
import com.example.rexchen.myim.server.broadcast.BroadcastManager;
import com.example.rexchen.myim.server.network.async.AsyncTaskManager;
import com.example.rexchen.myim.server.network.async.OnDataListener;
import com.example.rexchen.myim.server.network.http.HttpException;
import com.example.rexchen.myim.server.response.VersionResponse;
import com.example.rexchen.myim.server.widget.SelectableRoundedImageView;
import com.example.rexchen.myim.ui.activity.AboutRongCloudActivity;
import com.example.rexchen.myim.ui.activity.AccountSettingActivity;
import com.example.rexchen.myim.ui.activity.MyAccountActivity;
import com.jrmf360.rylib.JrmfClient;

import io.rong.imageloader.core.ImageLoader;
import io.rong.imkit.RongIM;
import io.rong.imlib.model.CSCustomServiceInfo;
import io.rong.imlib.model.UserInfo;
import com.example.rexchen.myim.R;

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


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_mine_seal, container, false);
        isDebug = getContext().getSharedPreferences("config", getContext().MODE_PRIVATE).getBoolean("isDebug", false);
        initViews(mView);
        initData();
        // 注册广播来更新个人信息
        BroadcastManager.getInstance(getActivity()).addAction(SealConst.CHANGEINFO, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateUserInfo();
            }
        });

        // 检查版本更新
        compareVersion();

        return mView;
    }

    private void initViews(View mView) {
        mNewVersionView =  mView.findViewById(R.id.new_version_icon);
        imageView =  mView.findViewById(R.id.mine_header);
        mName =  mView.findViewById(R.id.mine_name);
        LinearLayout mUserProfile =  mView.findViewById(R.id.start_user_profile);
        LinearLayout mMineSetting =  mView.findViewById(R.id.mine_setting);
        LinearLayout mMineService =  mView.findViewById(R.id.mine_service);
        LinearLayout mMineXN =  mView.findViewById(R.id.mine_xiaoneng);
        LinearLayout mMineAbout =  mView.findViewById(R.id.mine_about);
        if(isDebug){
            mMineXN.setVisibility(View.VISIBLE);
        }else{
            mMineXN.setVisibility(View.GONE);
        }
        mUserProfile.setOnClickListener(this);
        mMineSetting.setOnClickListener(this);
        mMineService.setOnClickListener(this);
        mMineAbout.setOnClickListener(this);
        mMineXN.setOnClickListener(this);
        mView.findViewById(R.id.my_wallet).setOnClickListener(this);
    }

    private void initData() {
        sp = getActivity().getSharedPreferences("config", Context.MODE_PRIVATE);
        updateUserInfo();
    }

    // 更新个人信息
    private void updateUserInfo() {
        String userId = sp.getString(SealConst.SEALTALK_LOGIN_ID, "");
        String username = sp.getString(SealConst.SEALTALK_LOGIN_NAME, "");
        String userPortrait = sp.getString(SealConst.SEALTALK_LOGING_PORTRAIT, "");
        mName.setText(username);
        if (!TextUtils.isEmpty(userId)) {
            String portraitUri = SealUserInfoManager.getInstance().getPortraitUri(new UserInfo(userId, username, Uri.parse(userPortrait)));
            ImageLoader.getInstance().displayImage(portraitUri, imageView, App.getOptions());
        }
    }

    // 检查版本更新
    private void compareVersion() {
        AsyncTaskManager.getInstance(getActivity()).request(COMPARE_VERSION, new OnDataListener() {
            @Override
            public Object doInBackground(int requestCode, String parameter) throws HttpException {
                return new SealAction(getActivity()).getSealTalkVersion();
            }

            @Override
            public void onSuccess(int requestCode, Object result) {
                if (result != null) {
                    VersionResponse response = (VersionResponse) result;
                    String[] s = response.getAndroid().getVersion().split("\\.");
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < s.length; i++) {
                        sb.append(i);
                    }

                    String[] s2 = getVersionInfo()[1].split("\\.");
                    StringBuilder sb2 = new StringBuilder();
                    for (int i = 0; i < s2.length; i++) {
                        sb2.append(s2[i]);
                    }
                    if (Integer.parseInt(sb.toString()) > Integer.parseInt(sb2.toString())) {
                        mNewVersionView.setVisibility(View.VISIBLE);
                        url = response.getAndroid().getUrl();
                        isHasNewVersion = true;
                        BroadcastManager.getInstance(getActivity()).sendBroadcast(SHOW_RED);
                    }
                }
            }

            @Override
            public void onFailure(int requestCode, int state, Object result) { }
        });
    }

    // 获取版本信息
    private String[] getVersionInfo() {
        String[] version = new String[2];

        PackageManager packageManager = getActivity().getPackageManager();

        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getActivity().getPackageName(), 0);
            version[0] = String.valueOf(packageInfo.versionCode);
            version[1] = packageInfo.versionName;
            return version;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return version;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_user_profile:
                startActivity(new Intent(getActivity(), MyAccountActivity.class));
                break;
            case R.id.mine_setting:
                startActivity(new Intent(getActivity(), AccountSettingActivity.class));
                break;
            case R.id.mine_service:
                CSCustomServiceInfo.Builder builder = new CSCustomServiceInfo.Builder();
                builder.province("北京");
                builder.city("北京");
                RongIM.getInstance().startCustomerServiceChat(getActivity(), "KEFU146001495753714", "在线客服", builder.build());
                // KEFU146001495753714 正式  KEFU145930951497220 测试  小能: zf_1000_1481459114694   zf_1000_1480591492399
                break;
            case R.id.mine_xiaoneng:
                CSCustomServiceInfo.Builder builder1 = new CSCustomServiceInfo.Builder();
                builder1.province("北京");
                builder1.city("北京");
                RongIM.getInstance().startCustomerServiceChat(getActivity(), "zf_1000_1481459114694", "在线客服", builder1.build());
                break;
            case R.id.mine_about:
                mNewVersionView.setVisibility(View.GONE);
                Intent intent = new Intent(getActivity(), AboutRongCloudActivity.class);
                intent.putExtra("isHasNewVersion", isHasNewVersion);
                if (!TextUtils.isEmpty(url)) {
                    intent.putExtra("url", url);
                }
                startActivity(intent);
                break;
            case R.id.my_wallet:
                JrmfClient.intentWallet(getActivity());
                break;
        }
    }
}
