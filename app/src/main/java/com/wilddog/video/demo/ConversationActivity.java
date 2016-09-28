package com.wilddog.video.demo;

import android.content.DialogInterface;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wilddog.client.SyncReference;
import com.wilddog.client.WilddogSync;
import com.wilddog.video.Conversation;
import com.wilddog.video.ConversationClient;
import com.wilddog.video.IncomingInvite;
import com.wilddog.video.LocalStream;
import com.wilddog.video.OutgoingInvite;
import com.wilddog.video.Participant;
import com.wilddog.video.RemoteStream;
import com.wilddog.video.Video;
import com.wilddog.video.bean.ConversationException;
import com.wilddog.video.bean.ConversationMode;
import com.wilddog.video.bean.InviteOptions;
import com.wilddog.video.bean.LocalStreamOptions;
import com.wilddog.video.listener.CompleteListener;
import com.wilddog.video.listener.ConversationCallback;
import com.wilddog.wilddogauth.WilddogAuth;

import org.webrtc.RendererCommon;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ConversationActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_INVITE_TO_CONVERSATION = 0;
    public static final int REQUEST_CODE_INVITE = 1;
    //public static final int REQUEST_CODE_INVITE=0;
    @BindView(R.id.tv_uid)
    TextView tvUid;
    @BindView(R.id.tv_wait_for_accept)
    TextView tvWaitForAccept;
    @BindView(R.id.btn_invite_and_cancel)
    Button btnInviteAndCancle;
    @BindView(R.id.surface)
    GLSurfaceView mGLSurfaceView;

    @BindView(R.id.btn_operation_mic)
    Button btnMic;
    @BindView(R.id.btn_operation_video)
    Button btnVideo;
    @BindView(R.id.btn_operation_speaker)
    Button btnSpeaker;
    @BindView(R.id.btn_operation_invite)
    Button btnInvite;
    @BindView(R.id.btn_operation_hangup)
    Button btnHangup;
    @BindView(R.id.ll_in_conversation)
    LinearLayout llInConversation;
    @BindView(R.id.ll_invite)
    LinearLayout llInvite;


    private VideoRenderer.Callbacks localCallbacks;
    private VideoRenderer.Callbacks remoteCbFirst;
    private VideoRenderer.Callbacks remoteCbSecond;
    private VideoRenderer.Callbacks remoteCbThird;

    private Video video;
    private ConversationClient client;
    private Conversation mConversation;
    private OutgoingInvite outgoingInvite;
    private Map<IncomingInvite, AlertDialog> incomingDialogMap;
    private LocalStream localStream;


    private boolean isInviting = false;
    private Intent intent;
    private Map<String, VideoRenderer.Callbacks> callbacksMap = new ArrayMap<>();
    private Conversation.Listener conversationListener = new Conversation.Listener() {
        @Override
        public void onParticipantConnected(Conversation conversation, Participant participant) {
            RemoteStream remoteStream = participant.getRemoteStream();

            if (participantSet.size() == 0) {
                /*VideoRendererGui.update(remoteCbFirst, 75, 75, 25, 25, RendererCommon.ScalingType.SCALE_ASPECT_FILL,
                        false);*/
                remoteCbFirst=VideoRendererGui.create(75, 75, 25, 25, RendererCommon.ScalingType.SCALE_ASPECT_FILL,
                        false);
                remoteStream.attach(remoteCbFirst);
                participantSet.add(participant.getParticipantId());
                callbacksMap.put(participant.getParticipantId(), remoteCbFirst);
                llInvite.setVisibility(View.GONE);
                llInConversation.setVisibility(View.VISIBLE);
                //重置邀请按钮状态
                tvWaitForAccept.setVisibility(View.INVISIBLE);
                btnInviteAndCancle.setText("发起会话");
                isInviting = false;
            } else if (!participantSet.contains(participant.getParticipantId())) {
                participantSet.add(participant.getParticipantId());
                int size = participantSet.size();
                switch (size) {
                    case 2:
                        remoteCbSecond = VideoRendererGui.createGuiRenderer(50, 75, 25, 25, RendererCommon.ScalingType
                                .SCALE_ASPECT_FILL, false);
                        remoteStream.attach(remoteCbSecond);
                        callbacksMap.put(participant.getParticipantId(), remoteCbSecond);

                        break;
                    case 3:
                        remoteCbThird = VideoRendererGui.createGuiRenderer(25, 75, 25, 25, RendererCommon.ScalingType
                                .SCALE_ASPECT_FILL, false);
                        remoteStream.attach(remoteCbThird);
                        callbacksMap.put(participant.getParticipantId(), remoteCbThird);

                }
            }

        }

        @Override
        public void onFailedToConnectParticipant(Conversation conversation, Participant participant,
                                                 ConversationException exception) {

        }

        @Override
        public void onParticipantDisconnected(Conversation conversation, Participant participant) {

            VideoRenderer.Callbacks callbacks = callbacksMap.get(participant.getParticipantId());
            VideoRendererGui.remove(callbacks);

            participantSet.remove(participant.getParticipantId());
            callbacksMap.remove(callbacks);


        }

        @Override
        public void onConversationEnded(Conversation conversation, ConversationException exception) {
            isInviting=false;
            mConversation=null;
            participantSet.clear();
            incomingDialogMap.clear();
            callbacksMap.clear();
            llInConversation.setVisibility(View.GONE);
            llInvite.setVisibility(View.VISIBLE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        ButterKnife.bind(this);
        //初始化视频展示控件
        initVideoRender();

        String uid = WilddogAuth.getInstance().getCurrentUser().getUid();
        tvUid.setText(uid);

        Video.initializeWilddogVideo(getApplicationContext());
        //初始化视频根节点，mRef=WilddogSync.getReference().child([视频控制面板中配置的自定义根节点]);
        SyncReference mRef = WilddogSync.getReference().child("wilddog");
        ConversationClient.init(mRef, new CompleteListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(String s) {

            }
        });

        tvWaitForAccept.setVisibility(View.INVISIBLE);
        intent = new Intent(ConversationActivity.this, UserListActivity.class);
        //获取video实例
        video = Video.getInstance();
        //通过video获取client实例
        client = video.getClient();


        localStream = video.createLocalStream(LocalStreamOptions.DEFAULT_OPTIONS, new CompleteListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(String s) {

            }
        });

        localStream.attach(localCallbacks);

        incomingDialogMap = new HashMap<>();
        this.client.setInviteListener(new ConversationClient.Listener() {
            @Override
            public void onStartListeningForInvites(ConversationClient client) {

            }

            @Override
            public void onStopListeningForInvites(ConversationClient client) {

            }

            @Override
            public void onFailedToStartListening(ConversationClient client, ConversationException e) {

            }

            @Override
            public void onIncomingInvite(ConversationClient client, final IncomingInvite invite) {

                AlertDialog.Builder builder = new AlertDialog.Builder(ConversationActivity.this);
                builder.setMessage("邀请你加入会话");
                builder.setTitle("加入邀请");
                builder.setNegativeButton("拒绝邀请", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        invite.reject();
                    }
                });
                builder.setPositiveButton("确认加入", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        incomingDialogMap.remove(invite);

                        LocalStream stream = new LocalStream();
                        stream.setMediaStream(localStream.getMediaStream());
                        invite.accept(stream, new ConversationCallback() {
                            @Override
                            public void onConversation(Conversation conversation, ConversationException exception) {

                                mConversation = conversation;
                                conversation.setConversationListener(conversationListener);
                            }
                        });

                    }
                });
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {

                    }
                });
                //builder.setCancelable(false);
                AlertDialog alertDialog = builder.create();
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();
                incomingDialogMap.put(invite, alertDialog);
            }

            @Override
            public void onIncomingInviteCanceled(ConversationClient client, IncomingInvite invite) {
                AlertDialog alertDialog = incomingDialogMap.get(invite);
                alertDialog.dismiss();
                alertDialog = null;
                incomingDialogMap.remove(invite);
            }
        });
    }

    private void initVideoRender() {
        mGLSurfaceView = (GLSurfaceView) findViewById(R.id.surface);
        mGLSurfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mGLSurfaceView.setPreserveEGLContextOnPause(true);
        mGLSurfaceView.setKeepScreenOn(true);
        VideoRendererGui.setView(mGLSurfaceView, null);

        localCallbacks = VideoRendererGui.createGuiRenderer(0, 0, 100, 100, RendererCommon.ScalingType
                .SCALE_ASPECT_FILL, true);
        /*remoteCbFirst = VideoRendererGui.createGuiRenderer(75, 75, 25, 25, RendererCommon.ScalingType
                .SCALE_ASPECT_FILL, false);
        remoteCbSecond = VideoRendererGui.createGuiRenderer(50, 75, 25, 25, RendererCommon.ScalingType
                .SCALE_ASPECT_FILL, false);
        remoteCbThird = VideoRendererGui.createGuiRenderer(25, 75, 25, 25, RendererCommon.ScalingType
                .SCALE_ASPECT_FILL, false);*/
    }


    private void showLoginUsers() {

        intent.putExtra("list_state", UserListState.STATE_SELF_EXCLUDE);
        startActivityForResult(intent, REQUEST_CODE_INVITE_TO_CONVERSATION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            String participant = data.getStringExtra("participant");
            Set<String> participants = new HashSet<>();
            participants.add(participant);
            switch (requestCode) {
                case REQUEST_CODE_INVITE:
                    mConversation.invite(participants);
                    break;
                case REQUEST_CODE_INVITE_TO_CONVERSATION:
                    isInviting = true;
                    tvWaitForAccept.setVisibility(View.VISIBLE);
                    btnInviteAndCancle.setText("取消");
                    inviteParticipant(participants);

                    break;
            }
        }
    }

    Set<String> participantSet = new HashSet<String>();

    private void inviteParticipant(Set<String> participants) {

        LocalStream stream = new LocalStream();
        stream.setMediaStream(localStream.getMediaStream());
        InviteOptions options = new InviteOptions(ConversationMode.SERVER_BASED, participants, stream);
        outgoingInvite = client.inviteToConversation(options, new ConversationCallback() {
            @Override
            public void onConversation(Conversation conversation, ConversationException exception) {

                mConversation = conversation;
                mConversation.setConversationListener(conversationListener);
            }
        });
    }

    @OnClick(R.id.btn_invite_and_cancel)
    public void invite() {
        if (!isInviting) {

            showLoginUsers();


        } else {
            outgoingInvite.cancel();
            tvWaitForAccept.setVisibility(View.INVISIBLE);
            btnInviteAndCancle.setText("发起会话");
            isInviting = false;
        }
    }

    @OnClick(R.id.btn_flip_camera)
    public void flipCamera() {
        video.flipCamera();
    }

    boolean isAudioEnable = true;

    @OnClick(R.id.btn_operation_mic)
    public void micClick() {
        isAudioEnable = !isAudioEnable;
        localStream.enableAudio(isAudioEnable);
    }

    boolean isVideoEnable = true;

    @OnClick(R.id.btn_operation_video)
    public void videoClick() {
        isVideoEnable = !isVideoEnable;
        localStream.enableVideo(isVideoEnable);
    }

    //开启/关闭扬声器
    @OnClick(R.id.btn_operation_speaker)
    public void speakerClick() {

    }

    //在会议中邀请其他人加入会话
    @OnClick(R.id.btn_operation_invite)
    public void inviteClick() {
        intent.putExtra("list_state", UserListState.STATE_SELF_EXCLUDE);
        startActivityForResult(intent, REQUEST_CODE_INVITE);
    }

    //挂断会议
    @OnClick(R.id.btn_operation_hangup)
    public void hangupClick() {
        //localStream.enableAudio(false);

        Set<Map.Entry<String, VideoRenderer.Callbacks>> entries = callbacksMap.entrySet();
        Iterator<Map.Entry<String, VideoRenderer.Callbacks>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            VideoRendererGui.remove(iterator.next().getValue());
        }
        callbacksMap.clear();
        participantSet.clear();
        mConversation.disconnect();
        llInConversation.setVisibility(View.GONE);
        llInvite.setVisibility(View.VISIBLE);
    }
}
