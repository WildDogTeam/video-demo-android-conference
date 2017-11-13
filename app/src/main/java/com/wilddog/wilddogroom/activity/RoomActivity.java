package com.wilddog.wilddogroom.activity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
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

    private GridView gvStreams ;

    private boolean isLocalAttach = false;

    private MygridViewAdapter adapter ;
    private List<StreamHolder> streamHolders = new ArrayList<>();

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
               switch (msg.what){
                   case 0:
                       adapter.notifyDataSetChanged();
                       break;
                   default:
                       break;
               }
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


    private void joinRoom() {
        room = new WilddogRoom(roomId, new WilddogRoom.Listener() {
            @Override
            public void onConnected(WilddogRoom wilddogRoom) {
                Toast.makeText(RoomActivity.this,"已经连接上服务器", Toast.LENGTH_SHORT).show();
                // 此时服务器返回用户id
                setLocalStreamId();
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
                Toast.makeText(RoomActivity.this,"服务器连接断开", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onStreamAdded(WilddogRoom wilddogRoom, RoomStream roomStream) {
             room.subscribe(roomStream);
            }

            @Override
            public void onStreamRemoved(WilddogRoom wilddogRoom, RoomStream roomStream) {
                removeRemoteStream(roomStream.getStreamId());
                handler.sendEmptyMessage(0);
            }

            @Override
            public void onStreamReceived(WilddogRoom wilddogRoom, RoomStream roomStream) {
                // 在控件中显示
                StreamHolder holder = new StreamHolder(false, System.currentTimeMillis(),roomStream);
                holder.setId(roomStream.getStreamId());
                streamHolders.add(holder);
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

    private void removeRemoteStream(long streamId) {
        for(StreamHolder holder:streamHolders){
            if(streamId==holder.getId()){
                streamHolders.remove(holder);
                break;
            }
        }
    }

    private void setLocalStreamId() {
        for(StreamHolder holder:streamHolders){
            if(holder.isLocal()){
               holder.setId(((LocalStream)holder.getStream()).getStreamId());
            }
        }
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
        //将本地媒体流绑定到WilddogVideoView中
        StreamHolder holder = new StreamHolder(true, System.currentTimeMillis(),localStream);
        streamHolders.add(holder);
        handler.sendEmptyMessage(0);
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
        gvStreams = (GridView) findViewById(R.id.gv_streams);
        gvStreams.setSelector(new ColorDrawable(Color.TRANSPARENT));
        adapter = new MygridViewAdapter(this,streamHolders);
        gvStreams.setAdapter(adapter);
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
                    localStream.enableAudio(isVideoEnable);
                }
                break;
            case R.id.btn_operation_hangup:
                leaveRoom();
                break;
            default:
                break;
        }
    }

    public class  MygridViewAdapter extends BaseAdapter {
        private List<StreamHolder> mlist;
        private Context mContext;
        MygridViewAdapter(Context context, List<StreamHolder> list){
        mContext = context;
        mlist = list;
        }

        @Override
        public int getCount() {
            return mlist.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holder;
            StreamHolder streamHolder = mlist.get(i);
            if(view==null){
                view = View.inflate(mContext,R.layout.listitem_video,null);
                holder = new ViewHolder();
                holder.wilddogVideoView = (WilddogVideoView) view.findViewById(R.id.wvv_video);
                view.setTag(holder);
            }else {
                holder = (ViewHolder) view.getTag();
            }
            if(streamHolder.isLocal()){
                // 本地流detach 需要时间,频繁detach再attach,可能detach完成在attch之后,导致本地视频画面卡住,所以如果是本地流attch之后就不反复操作了
                if(isLocalAttach ==false){
                streamHolder.getStream().attach(holder.wilddogVideoView);
                isLocalAttach = true;}
            }else {
            streamHolder.getStream().detach();
            streamHolder.getStream().attach(holder.wilddogVideoView);}
            return view;
        }
        class ViewHolder{
            WilddogVideoView wilddogVideoView;
        }
    }
    private void leaveRoom(){
        if(room!=null){
            room.disconnect();
            room=null;
        }
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
