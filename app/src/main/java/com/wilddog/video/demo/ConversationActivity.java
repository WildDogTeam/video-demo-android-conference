package com.wilddog.video.demo;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wilddog.client.SyncReference;
import com.wilddog.client.WilddogSync;
import com.wilddog.video.Conference;
import com.wilddog.video.LocalStream;
import com.wilddog.video.Participant;
import com.wilddog.video.RemoteStream;
import com.wilddog.video.WilddogVideo;
import com.wilddog.video.WilddogVideoClient;
import com.wilddog.video.WilddogVideoView;
import com.wilddog.video.WilddogVideoViewLayout;
import com.wilddog.video.bean.ConnectOptions;
import com.wilddog.video.bean.LocalStreamOptions;
import com.wilddog.video.bean.VideoException;
import com.wilddog.video.listener.CompleteListener;
import com.wilddog.wilddogauth.WilddogAuth;

import org.webrtc.EglBase;
import org.webrtc.RendererCommon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ConversationActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_INVITE = 2;

    public final static int LAYOUT_H = 48;
    public final static int LAYOUT_W = 32;
    public final static int LAYOUT_SPLIT_H = 1;
    public final static int LAYOUT_SPLIT_V = 1;

    public static final String TAG = "M2M";
    @BindView(R.id.tv_uid)
    TextView tvUid;
    @BindView(R.id.tv_wait_for_accept)
    TextView tvWaitForAccept;

    @BindView(R.id.btn_operation_mic)
    Button btnMic;
    @BindView(R.id.btn_operation_video)
    Button btnVideo;

    @BindView(R.id.btn_operation_hangup)
    Button btnHangup;
    @BindView(R.id.ll_in_conversation)
    LinearLayout llInConversation;
    @BindView(R.id.ll_invite)
    LinearLayout llInvite;

    @BindView(R.id.btn_invite_and_cancel)
    Button btnInvite;

    @BindView(R.id.local_video_layout)
    WilddogVideoViewLayout localRenderLayout;
    @BindView(R.id.remote_video_layout1)
    WilddogVideoViewLayout remoteRenderLayout1;
    @BindView(R.id.remote_video_layout2)
    WilddogVideoViewLayout remoteRenderLayout2;
    @BindView(R.id.remote_video_layout3)
    WilddogVideoViewLayout remoteRenderLayout3;
    @BindView(R.id.remote_video_layout4)
    WilddogVideoViewLayout remoteRenderLayout4;
    @BindView(R.id.remote_video_layout5)
    WilddogVideoViewLayout remoteRenderLayout5;

    @BindView(R.id.local_video_view)
    WilddogVideoView local_video_view;
    @BindView(R.id.remote_video_view1)
    WilddogVideoView remote_video_view1;
    @BindView(R.id.remote_video_view2)
    WilddogVideoView remote_video_view2;
    @BindView(R.id.remote_video_view3)
    WilddogVideoView remote_video_view3;
    @BindView(R.id.remote_video_view4)
    WilddogVideoView remote_video_view4;
    @BindView(R.id.remote_video_view5)
    WilddogVideoView remote_video_view5;


    private WilddogVideo video;
    private WilddogVideoClient client;

    private LocalStream localStream;
    private List<WilddogVideoView> videoViews;
    private List<WilddogVideoViewLayout> renderLayouts;
    private String uid;
    private VideoViewPool videoViewPool;

    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            btnInvite.setEnabled(true);
            btnInvite.setText("加入会议");
            llInConversation.setVisibility(View.VISIBLE);
            llInvite.setVisibility(View.GONE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set window styles for fullscreen-window size. Needs to be done before
        // adding content.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams
                .FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams
                .FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View
                .SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        setContentView(R.layout.activity_conversation);

        //初始化控件
        ButterKnife.bind(this);
        SyncReference reference = WilddogSync.getReference();
        String path = reference.getRoot().toString();
        int startIndex = path.indexOf("https://") == 0 ? 8 : 7;
        String appid = path.substring(startIndex, path.length() - 14);
        //初始化Video
        WilddogVideo.initializeWilddogVideo(getApplicationContext(), appid);
        //初始化视频展示控件
        videoViews = new ArrayList<>();
        videoViews.add(remote_video_view1);
        videoViews.add(remote_video_view2);
        videoViews.add(remote_video_view3);
        videoViews.add(remote_video_view4);
        videoViews.add(remote_video_view5);
        renderLayouts = new ArrayList<>();
        renderLayouts.add(remoteRenderLayout1);
        renderLayouts.add(remoteRenderLayout2);
        renderLayouts.add(remoteRenderLayout3);
        renderLayouts.add(remoteRenderLayout4);
        renderLayouts.add(remoteRenderLayout5);
        videoViewPool = VideoViewPool.getVideoViewPool();
        videoViewPool.setView(videoViews);
        initVideoRender();
        //获取当前用户的 Wilddog ID
        uid = WilddogAuth.getInstance().getCurrentUser().getUid();
        tvUid.setText(uid);
        tvWaitForAccept.setVisibility(View.INVISIBLE);

        //获取video实例
        video = WilddogVideo.getInstance();
        //通过video获取client实例
        client = video.getClient();
        //创建本地视频流，通过video对象获取本地视频流
        LocalStreamOptions.Builder builder = new LocalStreamOptions.Builder();

        LocalStreamOptions options = builder.height(240).width(320).build();
        localStream = video.createLocalStream(options, eglBaseContext, new CompleteListener() {
            @Override
            public void onCompleted(VideoException s) {

            }
        });
        localStream.enableAudio(isAudioEnable);
        //将视频对象绑定到VideoRenderer.Callbacks中
        localStream.attach(local_video_view);
        //为client对象设置InviteListener ，监听邀请事件变化，在使用inviteToConversation 前必须先设置监听

    }

    private EglBase.Context eglBaseContext = EglBase.create().getEglBaseContext();

    private void initVideoRender() {
        local_video_view.init(eglBaseContext, null);
        localRenderLayout.setPosition(1, 1, 32, 48);
        local_video_view.setZOrderMediaOverlay(true);
        local_video_view.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        local_video_view.setMirror(true);
        local_video_view.requestLayout();


        for (WilddogVideoView videoView : videoViews) {
            videoView.init(eglBaseContext, null);
            videoView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
            videoView.setMirror(false);
            videoView.requestLayout();
        }
        //计算并初始化Layout位置、大小
        for (int i = 0; i < renderLayouts.size(); i++) {

            WilddogVideoViewLayout layout = renderLayouts.get(i);
            if (i < 2) {
                layout.setPosition(LAYOUT_SPLIT_H + (i + 1) * (LAYOUT_W + LAYOUT_SPLIT_H), LAYOUT_SPLIT_H, LAYOUT_W,
                        LAYOUT_H);
            } else {
                layout.setPosition(LAYOUT_SPLIT_H + (i - 2) * (LAYOUT_W + LAYOUT_SPLIT_H), LAYOUT_H + LAYOUT_SPLIT_V,
                        LAYOUT_W, LAYOUT_H);
            }

        }

    }

    Set<String> participantSet = new HashSet<String>();
    private Conference mConference;
    private Map<String, WilddogVideoView> mPartiViewMap = new ArrayMap<>();

    private void inviteToConference(String conferenceId) {

        //创建 ConnectOptions 对象，此对象包含邀请所需要的参数
        ConnectOptions options = new ConnectOptions(localStream, "chaih");

        mConference = client.connectToConference(conferenceId, options, new Conference.Listener() {
            @Override
            public void onConnected(Conference conference) {
                Log.e(TAG, "onConnected:" + conference);
                mHandler.sendEmptyMessage(0);
            }

            @Override
            public void onConnectFailed(Conference conference, VideoException exception) {
                Log.e(TAG, "onConnectFailed:" + exception);
            }

            @Override
            public void onDisconnected(Conference conference, VideoException exception) {
                Log.e(TAG, "onDisconnected:" + exception);
            }

            @Override
            public void onParticipantConnected(Conference conference, final Participant participant) {
                Log.e(TAG, "onParticipantConnected:" + participant.getParticipantId());
                participantSet.add(participant.getParticipantId());
                participant.setListener(new Participant.Listener() {
                    @Override
                    public void onStreamAdded(RemoteStream remoteStream) {
                        Log.e(TAG, "onStreamAdded:" + remoteStream);
                        WilddogVideoView view = videoViewPool.getView();
                        mPartiViewMap.put(participant.getParticipantId(), view);

                        if (view != null) {
                            mHandler.sendEmptyMessage(0);
                            remoteStream.attach(view);
                        }
                    }

                    @Override
                    public void onStreamRemoved(RemoteStream remoteStream) {
                        remoteStream.detach();
                        String participantId = participant.getParticipantId();
                        WilddogVideoView videoView = mPartiViewMap.get(participantId);
                        mPartiViewMap.remove(participantId);
                        videoViewPool.returnView(videoView);
                    }

                    @Override
                    public void onError(VideoException exception) {

                    }
                });
            }

            @Override
            public void onParticipantDisconnected(Conference conference, Participant participant) {
                Log.e(TAG, "onParticipantDisconnected" + participant.getParticipantId());
                String participantId = participant.getParticipantId();
                WilddogVideoView videoView = mPartiViewMap.get(participantId);
                mPartiViewMap.remove(participantId);
                videoViewPool.returnView(videoView);
            }
        });
    }

    @OnClick(R.id.btn_invite_and_cancel)
    public void invite() {
        showInputDialog();
        //inviteToConference("333222111");


    }

    @OnClick(R.id.btn_flip_camera)
    public void flipCamera() {
        //切换摄像头
        video.flipCamera();
    }

    boolean isAudioEnable = false;

    @OnClick(R.id.btn_operation_mic)
    public void micClick() {
        //关闭/启用本地流音频，此操作影响所有人收到的音频流
        isAudioEnable = !isAudioEnable;
        localStream.enableAudio(isAudioEnable);
    }

    boolean isVideoEnable = true;

    @OnClick(R.id.btn_operation_video)
    public void videoClick() {
        // 关闭/启用本地流视频，此操作影响所有人收到的视频流
        isVideoEnable = !isVideoEnable;
        localStream.enableVideo(isVideoEnable);
    }

    //挂断会议
    @OnClick(R.id.btn_operation_hangup)
    public void hangupClick() {
        //挂断
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mConference != null) {
            mConference.disconnect();
        }
        localStream.detach();
        localStream.close();
        video.dispose();
        videoViewPool.dispose();
    }

    private void showInputDialog(){
        final EditText inputServer = new EditText(this);
        inputServer.setInputType(InputType.TYPE_CLASS_NUMBER);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请输入会议ID").setIcon(android.R.drawable.ic_menu_call).setView(inputServer)
                .setNegativeButton("取消", null);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                String conferenceId = inputServer.getText().toString();
                inviteToConference(conferenceId);
                btnInvite.setEnabled(false);
                btnInvite.setText("加入会议中");
            }
        });
        builder.show();
    }

}
