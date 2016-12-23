package com.wilddog.video.demo;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.wilddog.client.SyncReference;
import com.wilddog.client.WilddogSync;
import com.wilddog.wilddogauth.WilddogAuth;
import com.wilddog.wilddogauth.core.Task;
import com.wilddog.wilddogauth.core.listener.OnCompleteListener;
import com.wilddog.wilddogauth.core.result.AuthResult;
import com.wilddog.wilddogauth.core.result.GetTokenResult;
import com.wilddog.wilddogcore.WilddogApp;
import com.wilddog.wilddogcore.WilddogOptions;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.et_app_id)
    EditText etAppId;
    @BindView(R.id.tv_prompt)
    TextView tvPrompt;
    private static final int REQUEST_CODE = 0; // 请求码
    private String mAppId;
    private SyncReference mRef;
    private WilddogAuth auth;
    // 所需的全部权限
    static final String[] PERMISSIONS = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
//动态申请权限
        int sdk=android.os.Build.VERSION.SDK_INT;
        if (sdk>=23){
            Intent intent=new Intent(this,PermissionActivity.class);

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Bundle bundle=new Bundle();
            bundle.putStringArray("permission",PERMISSIONS);
            PermissionActivity.startActivityForResult(this,REQUEST_CODE,PERMISSIONS);
        }
    }

    @OnClick(R.id.btn_login_anonymously)
    public void loginAnonymously() {
        mAppId = etAppId.getText().toString();
        if (TextUtils.isEmpty(mAppId)) {
            Toast.makeText(MainActivity.this, "请输入你的AppId", Toast.LENGTH_SHORT).show();
            etAppId.setText("");
            return;
        }

        //初始化WilddogApp,完成初始化之后可在项目任意位置通过getInstance()获取Sync & Auth对象
        WilddogOptions.Builder builder = new WilddogOptions.Builder().setSyncUrl("http://" + mAppId + "" +
                ".wilddogio.com");
        WilddogOptions options = builder.build();
        WilddogApp.initializeApp(getApplicationContext(), options);
        //获取Sync & Auth 对象
        mRef = WilddogSync.getInstance().getReference();
        auth = WilddogAuth.getInstance();
        //通过匿名登录方式登录，可选择以下任意方式登录系统
        /*auth.signInWithEmailAndPassword();*/
        /*auth.signInWithCredential();*/
        /*auth.signInWithCustomToken();*/
        auth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    tvPrompt.setVisibility(View.INVISIBLE);
                    String uid = auth.getCurrentUser().getUid();
                    Log.e("Login", "authWithPassword uid ::" + uid);
                    if (!TextUtils.isEmpty(uid)) {
                        writeToUsers(uid);
                        Intent intent = new Intent(getApplicationContext(), InputIdActivity.class);
                        startActivity(intent);
                    }
                } else {
                    //throw new RuntimeException("auth 失败" + task.getException().getMessage());
                    Toast.makeText(MainActivity.this, "auth 失败，错误详情：" + task.getException().getMessage(), Toast
                            .LENGTH_SHORT).show();
                }
            }
        });

    }

    /**
     *
     *
    * */
    private void writeToUsers(String uid) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(uid, true);
        mRef.child("users").updateChildren(map);
        mRef.child("users/" + uid).onDisconnect().removeValue();
    }
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 拒绝时, 关闭页面, 缺少主要权限, 无法运行
        if (requestCode == REQUEST_CODE && resultCode == PermissionHelper.PERMISSIONS_DENIED) {
            finish();
        }else {

        }
    }
}
