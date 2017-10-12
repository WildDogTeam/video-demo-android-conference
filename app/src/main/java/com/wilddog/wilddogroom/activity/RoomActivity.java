package com.wilddog.wilddogroom.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.wilddog.video.base.LocalStream;
import com.wilddog.video.base.LocalStreamOptions;
import com.wilddog.video.base.WilddogVideoError;
import com.wilddog.video.base.WilddogVideoInitializer;
import com.wilddog.video.base.WilddogVideoView;
import com.wilddog.video.base.util.LogUtil;
import com.wilddog.video.base.util.logging.Logger;
import com.wilddog.video.room.CompleteListener;
import com.wilddog.video.room.RoomStream;
import com.wilddog.video.room.WilddogRoom;
import com.wilddog.wilddogauth.WilddogAuth;
import com.wilddog.wilddogroom.R;
import com.wilddog.wilddogroom.bean.StreamHolder;
import com.wilddog.wilddogroom.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class RoomActivity extends AppCompatActivity implements View.OnClickListener{
    private String roomId ;

    private TextView tvRoomId;
    private Button camera;
    private Button mic;
    private Button video;
    private Button leave;

    private LocalStream localStream;
    private boolean isAudioEnable = true;
    private boolean isVideoEnable = true;

    private WilddogVideoInitializer initializer;
    private WilddogRoom room;

    private WilddogVideoView localView;
    private WilddogVideoView remoteView1;
    private WilddogVideoView remoteView2;
    private WilddogVideoView remoteView3;
    private WilddogVideoView remoteView4;
    private WilddogVideoView remoteView5;
    private Map<Long, StreamHolder> mPartiViewMap = new TreeMap<>();
    private List<WilddogVideoView> remoteVideoViews = new ArrayList<>();
    private List<StreamHolder> remoteStreamHolders = new ArrayList<>();
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            deteachAll();
            showRemoteViews();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        roomId = getIntent().getStringExtra("roomId");
        initView();
        initRoomSDK();
        createLocalStream();
        joinRoom();
    }
    private void deteachAll(){
     for(StreamHolder streamHolder:remoteStreamHolders){
         streamHolder.getStream().detach();
     }
    }

    private void showRemoteViews(){
        for(int i = 0;i<remoteStreamHolders.size();i++){
            remoteStreamHolders.get(i).getStream().attach(remoteVideoViews.get(i));
        }
    }

    private void joinRoom() {
        room = new WilddogRoom(roomId, new WilddogRoom.Listener() {
            @Override
            public void onConnected(WilddogRoom wilddogRoom) {
                Toast.makeText(RoomActivity.this,"已经连接上服务器", Toast.LENGTH_SHORT).show();
                room.publish(localStream, new CompleteListener() {
                    @Override
                    public void onComplete(WilddogVideoError wilddogVideoError) {
                        if(wilddogVideoError!=null){
                            //失败
                            Toast.makeText(RoomActivity.this,"推送流失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onDisconnected(WilddogRoom wilddogRoom) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(RoomActivity.this,"服务器连接断开", Toast.LENGTH_SHORT).show();
                    }
                });
        //
              //  room.disconnect();
//                finish();
            }

            @Override
            public void onStreamAdded(WilddogRoom wilddogRoom, RoomStream roomStream) {
                //订阅流 如果超过6个就补订阅流
             room.subscribe(roomStream);
            }

            @Override
            public void onStreamRemoved(WilddogRoom wilddogRoom, RoomStream roomStream) {

                //具体流 超过六个的退出可能不包含,所以移除时候判断是否包含
                if(mPartiViewMap.containsKey(roomStream.getStreamId())){
                  remoteStreamHolders.remove(mPartiViewMap.remove(roomStream.getStreamId())  );
                handler.sendEmptyMessage(0);}
            }

            @Override
            public void onStreamReceived(WilddogRoom wilddogRoom, RoomStream roomStream) {
                // 在控件中显示
                if(mPartiViewMap.size()>=5){return;}
               StreamHolder holder = new StreamHolder(System.currentTimeMillis(),roomStream);
               mPartiViewMap.put(roomStream.getStreamId(),holder);
                remoteStreamHolders.add(holder);
                handler.sendEmptyMessage(0);
            }

            @Override
            public void onStreamChanged(WilddogRoom wilddogRoom, RoomStream roomStream) {
                // 混流使用
            }

            @Override
            public void onError(WilddogRoom wilddogRoom, WilddogVideoError wilddogVideoError) {
                Toast.makeText(RoomActivity.this,"发生错误,请产看日志", Toast.LENGTH_SHORT).show();
                Log.e("error","错误码:"+wilddogVideoError.getErrCode()+",错误信息:"+wilddogVideoError.getMessage());
            }
        });
        room.connect();

    }


    private void initRoomSDK() {
        LogUtil.setLogLevel(Logger.Level.DEBUG);
        WilddogVideoInitializer.initialize(RoomActivity.this, Constants.WILDDOG_VIDEO_ID, WilddogAuth.getInstance().getCurrentUser().getToken(false).getResult().getToken());
        initializer =  WilddogVideoInitializer.getInstance();
        initializer.addTokenListener(new WilddogVideoInitializer.TokenListener() {
            @Override
            public void onTokenChanged(String s) {

            }
        });
    }

    private void createLocalStream() {
        LocalStreamOptions options = new LocalStreamOptions.Builder().build();
        localStream = LocalStream.create(options);
        localStream.enableAudio(isAudioEnable);
        localStream.enableVideo(true);
        localStream.attach(localView);
        //将本地媒体流绑定到WilddogVideoView中
    }

    private void initView() {
        tvRoomId = (TextView) findViewById(R.id.tv_roomid);
        tvRoomId.setText(roomId);
        camera = (Button) findViewById(R.id.btn_flip_camera);
        camera.setOnClickListener(this);
        mic = (Button) findViewById(R.id.btn_operation_mic);
        mic.setOnClickListener(this);
        video = (Button) findViewById(R.id.btn_operation_video);
        video.setOnClickListener(this);
        leave = (Button) findViewById(R.id.btn_operation_hangup);
        leave.setOnClickListener(this);
        localView = (WilddogVideoView) findViewById(R.id.wvv_local);
        remoteView1 = (WilddogVideoView) findViewById(R.id.wvv_remote1);
        remoteView2 = (WilddogVideoView) findViewById(R.id.wvv_remote2);
        remoteView3 = (WilddogVideoView) findViewById(R.id.wvv_remote3);
        remoteView4 = (WilddogVideoView) findViewById(R.id.wvv_remote4);
        remoteView5 = (WilddogVideoView) findViewById(R.id.wvv_remote5);
        remoteVideoViews.add(remoteView1);
        remoteVideoViews.add(remoteView2);
        remoteVideoViews.add(remoteView3);
        remoteVideoViews.add(remoteView4);
        remoteVideoViews.add(remoteView5);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_flip_camera:
                if(localStream!=null){
                    localStream.switchCamera();
                }
                break;
            case R.id.btn_operation_mic:
                if(localStream!=null){
                    isAudioEnable = !isAudioEnable;
                    localStream.enableAudio(isAudioEnable);
                }
                break;
            case R.id.btn_operation_video:
                if(localStream!=null){
                    isVideoEnable = !isVideoEnable;
                    localStream.enableVideo(isVideoEnable);
                }
                break;
            case R.id.btn_operation_hangup:
                leaveRoom();
                break;
            default:
                break;
        }
    }



    private void leaveRoom(){
        if(room!=null){
            room.disconnect();
            room=null;
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        leaveRoom();
        if(!localStream.isClosed()){
            localStream.close();
        }
    }
}
