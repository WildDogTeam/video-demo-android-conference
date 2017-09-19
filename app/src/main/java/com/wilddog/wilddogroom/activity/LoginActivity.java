package com.wilddog.wilddogroom.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.wilddog.wilddogauth.WilddogAuth;
import com.wilddog.wilddogauth.core.Task;
import com.wilddog.wilddogauth.core.listener.OnCompleteListener;
import com.wilddog.wilddogauth.core.result.AuthResult;
import com.wilddog.wilddogroom.R;

public class LoginActivity extends AppCompatActivity {
    private Button loginWithAnonymous;
    private boolean islogining = false;
    private EditText roomId;
    private static final int REQUEST_CODE = 0; // 请求码

    // 所需的全部权限
    static final String[] PERMISSIONS = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
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

    private void initView() {
        roomId = (EditText) findViewById(R.id.et_roomId);
        loginWithAnonymous = (Button) findViewById(R.id.btn_login_anonymous);
        loginWithAnonymous.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (islogining) {
                    return;
                }
                islogining = true;
                checkRoomID();
            }
        });
    }

    private void checkRoomID() {
        String strRoonId = roomId.getText().toString().trim();
        if (TextUtils.isEmpty(strRoonId)) {
            Toast.makeText(LoginActivity.this, "输入的房间号不能为空", Toast.LENGTH_SHORT).show();
            islogining = false;
            return;
        }

        if (strRoonId.length() > 20 || strRoonId.length() < 1) {
            Toast.makeText(LoginActivity.this, "输入的房间号长度应在1-20之间", Toast.LENGTH_SHORT).show();
            islogining = false;
            return;
        }

        if (strRoonId.matches("[A-Za-z0-9_]+")) {
            loginWithAnonymous(strRoonId);
        } else {
            Toast.makeText(LoginActivity.this, "房间号只能包含数组,字母,下划线", Toast.LENGTH_SHORT).show();
        }
    }

    private void loginWithAnonymous(final String strRoomId) {
        WilddogAuth auth = WilddogAuth.getInstance();
        auth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(Task<AuthResult> var1) {
                if (var1.isSuccessful()) {
                    islogining = false;
                    Intent intent = new Intent(LoginActivity.this, RoomActivity.class);
                    intent.putExtra("roomId", strRoomId);
                    startActivity(intent);
                } else {
                    islogining = false;
                    Toast.makeText(LoginActivity.this, "登录失败,请查看日志寻找失败原因", Toast.LENGTH_SHORT).show();
                    Log.e("error", var1.getException().getMessage());
                }
            }
        });
    }


}
