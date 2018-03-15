package com.example.rexchen.myim.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.rexchen.myim.R;
import com.example.rexchen.myim.server.HomeWatcherReceiver;
import com.example.rexchen.myim.server.broadcast.BroadcastManager;
import com.example.rexchen.myim.server.widget.DragPointView;
import com.example.rexchen.myim.server.widget.LoadDialog;
import com.example.rexchen.myim.server.widget.MorePopWindow;
import com.example.rexchen.myim.ui.adapter.ConversationListAdapterEx;
import com.example.rexchen.myim.ui.fragment.ContactsFragment;
import com.example.rexchen.myim.ui.fragment.MineFragment;

import java.util.ArrayList;
import java.util.List;

import io.rong.common.RLog;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imkit.manager.IUnReadMessageObserver;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.message.ContactNotificationMessage;

public class MainActivity extends FragmentActivity implements ViewPager.OnPageChangeListener,
        View.OnClickListener,
        DragPointView.OnDragListencer,
        IUnReadMessageObserver {

    public static ViewPager mViewPager;
    private List<Fragment> mFragment = new ArrayList<>();
    private ImageView moreImage, mImageChats, mImageContact, mImageFind, mImageMe, mMineRed;
    private TextView mTextChats, mTextContact, mTextFind, mTextMe;
    private DragPointView mUnreadNumView;
    private ImageView mSearchImageView;
    /**
     * 会话列表的fragment
     */
    private ConversationListFragment mConversationListFragment = null;
    private boolean isDebug;
    private Context mContext;
    private Conversation.ConversationType[] mConversationsTypes = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        isDebug = getSharedPreferences("config", MODE_PRIVATE).getBoolean("isDebug", false);
        // 绑定控件
        initViews();
        // 设置颜色
        changeTextViewColor();
        // 默认选中第一个
        changeSelectedTabState(0);
        // 初始化 viewPager
        initMainViewPager();
        registerHomeKeyReceiver(this);
    }

    private void initViews() {
        // 绑定控件
        RelativeLayout chatRLayout =  findViewById(R.id.seal_chat);
        RelativeLayout contactRLayout =  findViewById(R.id.seal_contact_list);
//        RelativeLayout foundRLayout =  findViewById(R.id.seal_find);
        RelativeLayout mineRLayout =  findViewById(R.id.seal_me);
        mImageChats =  findViewById(R.id.tab_img_chats);
        mImageContact =  findViewById(R.id.tab_img_contact);
//        mImageFind =  findViewById(R.id.tab_img_find);
        mImageMe =  findViewById(R.id.tab_img_me);
        mTextChats =  findViewById(R.id.tab_text_chats);
        mTextContact =  findViewById(R.id.tab_text_contact);
//        mTextFind =  findViewById(R.id.tab_text_find);
        mTextMe =  findViewById(R.id.tab_text_me);
        mMineRed =  findViewById(R.id.mine_red);
        moreImage =  findViewById(R.id.seal_more);
        mSearchImageView =  findViewById(R.id.ac_iv_search);

        // 设置点击
        chatRLayout.setOnClickListener(this);
        contactRLayout.setOnClickListener(this);
//        foundRLayout.setOnClickListener(this);
        mineRLayout.setOnClickListener(this);
        moreImage.setOnClickListener(this);
        mSearchImageView.setOnClickListener(this);
        BroadcastManager.getInstance(mContext).addAction(MineFragment.SHOW_RED, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mMineRed.setVisibility(View.VISIBLE);
            }
        });
    }

    private void changeTextViewColor() {
        mImageChats.setBackgroundDrawable(getResources().getDrawable(R.mipmap.tab_chat));
        mImageContact.setBackgroundDrawable(getResources().getDrawable(R.mipmap.tab_contacts));
//        mImageFind.setBackgroundDrawable(getResources().getDrawable(R.mipmap.tab_found));
        mImageMe.setBackgroundDrawable(getResources().getDrawable(R.mipmap.tab_me));
        mTextChats.setTextColor(Color.parseColor("#abadbb"));
        mTextContact.setTextColor(Color.parseColor("#abadbb"));
//        mTextFind.setTextColor(Color.parseColor("#abadbb"));
        mTextMe.setTextColor(Color.parseColor("#abadbb"));
    }
    private void changeSelectedTabState(int position) {
        switch (position) {
            case 0:
                mTextChats.setTextColor(Color.parseColor("#0099ff"));
                mImageChats.setBackgroundDrawable(getResources().getDrawable(R.mipmap.tab_chat_hover));
                break;
            case 1:
                mTextContact.setTextColor(Color.parseColor("#0099ff"));
                mImageContact.setBackgroundDrawable(getResources().getDrawable(R.mipmap.tab_contacts_hover));
                break;
//            case 2:
//                mTextFind.setTextColor(Color.parseColor("#0099ff"));
//                mImageFind.setBackgroundDrawable(getResources().getDrawable(R.mipmap.tab_found_hover));
//                break;
            case 2:
                mTextMe.setTextColor(Color.parseColor("#0099ff"));
                mImageMe.setBackgroundDrawable(getResources().getDrawable(R.mipmap.tab_me_hover));
                break;
        }
    }

    // 初始化 viewPager
    private void initMainViewPager() {
        Fragment conversationList = initConversationList();
        mViewPager = (ViewPager) findViewById(R.id.main_viewpager);

        mUnreadNumView = (DragPointView) findViewById(R.id.seal_num);
        mUnreadNumView.setOnClickListener(this);
        mUnreadNumView.setDragListencer(this);

        // 底部 tab 对应的 fragment
        mFragment.add(conversationList);
        mFragment.add(new ContactsFragment());
//        mFragment.add(new DiscoverFragment());
        mFragment.add(new MineFragment());
        FragmentPagerAdapter fragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mFragment.get(position);
            }

            @Override
            public int getCount() {
                return mFragment.size();
            }
        };
        mViewPager.setAdapter(fragmentPagerAdapter);
        // 设置 viewPager 缓存界面个个数
        mViewPager.setOffscreenPageLimit(mFragment.size());
        mViewPager.setOnPageChangeListener(this);
        initData();
    }

    protected void initData() {
        // 消息类型
        final Conversation.ConversationType[] conversationTypes = {
                Conversation.ConversationType.PRIVATE,
                Conversation.ConversationType.GROUP,
                Conversation.ConversationType.SYSTEM,
                Conversation.ConversationType.PUBLIC_SERVICE,
                Conversation.ConversationType.APP_PUBLIC_SERVICE
        };

        RongIM.getInstance().addUnReadMessageCountChangedObserver(this, conversationTypes);
        getConversationPush();// 获取 push 的 id 和 target
        getPushMessage();
    }

    private void getConversationPush() {
        if (getIntent() != null && getIntent().hasExtra("PUSH_CONVERSATIONTYPE") && getIntent().hasExtra("PUSH_TARGETID")) {

            final String conversationType = getIntent().getStringExtra("PUSH_CONVERSATIONTYPE");
            final String targetId = getIntent().getStringExtra("PUSH_TARGETID");


            RongIM.getInstance().getConversation(Conversation.ConversationType.valueOf(conversationType), targetId, new RongIMClient.ResultCallback<Conversation>() {
                @Override
                public void onSuccess(Conversation conversation) {

                    if (conversation != null) {

                        if (conversation.getLatestMessage() instanceof ContactNotificationMessage) { //好友消息的push
                            startActivity(new Intent(MainActivity.this, NewFriendListActivity.class));
                        } else {
                            Uri uri = Uri.parse("rong://" + getApplicationInfo().packageName).buildUpon().appendPath("conversation")
                                    .appendPath(conversationType).appendQueryParameter("targetId", targetId).build();
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(uri);
                            startActivity(intent);
                        }
                    }
                }

                @Override
                public void onError(RongIMClient.ErrorCode e) {

                }
            });
        }
    }

    /**
     * 得到不落地 push 消息
     */
    private void getPushMessage() {
        Intent intent = getIntent();
        if (intent != null && intent.getData() != null && intent.getData().getScheme().equals("rong")) {
            String path = intent.getData().getPath();
            if (path.contains("push_message")) {
                SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
                String cacheToken = sharedPreferences.getString("loginToken", "");
                if (TextUtils.isEmpty(cacheToken)) {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                } else {
                    if (!RongIM.getInstance().getCurrentConnectionStatus().equals(RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED)) {
                        LoadDialog.show(mContext);
                        RongIM.connect(cacheToken, new RongIMClient.ConnectCallback() {
                            @Override
                            public void onTokenIncorrect() {
                                LoadDialog.dismiss(mContext);
                            }

                            @Override
                            public void onSuccess(String s) {
                                LoadDialog.dismiss(mContext);
                            }

                            @Override
                            public void onError(RongIMClient.ErrorCode e) {
                                LoadDialog.dismiss(mContext);
                            }
                        });
                    }
                }
            }
        }
    }

    // 设置 会话 fragment
    private Fragment initConversationList() {
        if (mConversationListFragment == null) {
            ConversationListFragment listFragment = new ConversationListFragment();
            listFragment.setAdapter(new ConversationListAdapterEx(RongContext.getInstance()));
            Uri uri;
            if (isDebug) {
                uri = Uri.parse("rong://" + getApplicationInfo().packageName).buildUpon()
                        .appendPath("conversationlist")
                        .appendQueryParameter(Conversation.ConversationType.PRIVATE.getName(), "true") //设置私聊会话是否聚合显示
                        .appendQueryParameter(Conversation.ConversationType.GROUP.getName(), "true")//群组
                        .appendQueryParameter(Conversation.ConversationType.PUBLIC_SERVICE.getName(), "false")//公共服务号
                        .appendQueryParameter(Conversation.ConversationType.APP_PUBLIC_SERVICE.getName(), "false")//订阅号
                        .appendQueryParameter(Conversation.ConversationType.SYSTEM.getName(), "true")//系统
                        .appendQueryParameter(Conversation.ConversationType.DISCUSSION.getName(), "true")
                        .build();
                mConversationsTypes = new Conversation.ConversationType[]{Conversation.ConversationType.PRIVATE,
                        Conversation.ConversationType.GROUP,
                        Conversation.ConversationType.PUBLIC_SERVICE,
                        Conversation.ConversationType.APP_PUBLIC_SERVICE,
                        Conversation.ConversationType.SYSTEM,
                        Conversation.ConversationType.DISCUSSION
                };

            } else {
                uri = Uri.parse("rong://" + getApplicationInfo().packageName).buildUpon()
                        .appendPath("conversationlist")
                        .appendQueryParameter(Conversation.ConversationType.PRIVATE.getName(), "false") //设置私聊会话是否聚合显示
                        .appendQueryParameter(Conversation.ConversationType.GROUP.getName(), "false")//群组
                        .appendQueryParameter(Conversation.ConversationType.PUBLIC_SERVICE.getName(), "false")//公共服务号
                        .appendQueryParameter(Conversation.ConversationType.APP_PUBLIC_SERVICE.getName(), "false")//订阅号
                        .appendQueryParameter(Conversation.ConversationType.SYSTEM.getName(), "true")//系统
                        .build();
                mConversationsTypes = new Conversation.ConversationType[]{Conversation.ConversationType.PRIVATE,
                        Conversation.ConversationType.GROUP,
                        Conversation.ConversationType.PUBLIC_SERVICE,
                        Conversation.ConversationType.APP_PUBLIC_SERVICE,
                        Conversation.ConversationType.SYSTEM
                };
            }
            listFragment.setUri(uri);
            mConversationListFragment = listFragment;
            return listFragment;
        } else {
            return mConversationListFragment;
        }
    }

    private HomeWatcherReceiver mHomeKeyReceiver = null;
    //如果遇见 Android 7.0 系统切换到后台回来无效的情况 把下面注册广播相关代码注释或者删除即可解决。下面广播重写 home 键是为了解决三星 note3 按 home 键花屏的一个问题
    private void registerHomeKeyReceiver(Context context) {
        if (mHomeKeyReceiver == null) {
            mHomeKeyReceiver = new HomeWatcherReceiver();
            final IntentFilter homeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            try {
                context.registerReceiver(mHomeKeyReceiver, homeFilter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }
    @Override
    public void onPageSelected(int position) {
        // 改变 tabBar 的选中状态
        changeTextViewColor();
        changeSelectedTabState(position);
    }
    @Override
    public void onPageScrollStateChanged(int state) { }

    long firstClick = 0;
    long secondClick = 0;

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.seal_chat:
                if (mViewPager.getCurrentItem() == 0) {
                    if (firstClick == 0) {
                        firstClick = System.currentTimeMillis();
                    } else {
                        secondClick = System.currentTimeMillis();
                    }
                    RLog.i("MainActivity", "time = " + (secondClick - firstClick));
                    // 好像是设置消息未读条数
                    if (secondClick - firstClick > 0 && secondClick - firstClick <= 800) {
                        mConversationListFragment.focusUnreadItem();
                        firstClick = 0;
                        secondClick = 0;
                    } else if (firstClick != 0 && secondClick != 0) {
                        firstClick = 0;
                        secondClick = 0;
                    }
                }
                mViewPager.setCurrentItem(0, false);
                break;
            case R.id.seal_contact_list:
                mViewPager.setCurrentItem(1, false);
                break;
//            case R.id.seal_find:
//                mViewPager.setCurrentItem(2, false);
//                break;
            case R.id.seal_me:
                mViewPager.setCurrentItem(2, false);
                mMineRed.setVisibility(View.GONE);
                break;
            case R.id.seal_more:
                MorePopWindow morePopWindow = new MorePopWindow(MainActivity.this);
                morePopWindow.showPopupWindow(moreImage);
                break;
            case R.id.ac_iv_search:
                startActivity(new Intent(MainActivity.this, SealSearchActivity.class));
                break;
        }
    }

    @Override
    public void onDragOut() {

    }

    @Override
    public void onCountChanged(int i) {

    }
}
