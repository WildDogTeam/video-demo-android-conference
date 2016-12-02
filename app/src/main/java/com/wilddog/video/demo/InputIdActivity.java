package com.wilddog.video.demo;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class InputIdActivity extends AppCompatActivity {

    private Context context;

    private String mCid;

    @BindView(R.id.et_conference_id)
    EditText etConferenceId;

    @BindView(R.id.btn_join_conference)
    Button btnJoin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_id);

        ButterKnife.bind(this);

        context=InputIdActivity.this;
    }

    @OnClick(R.id.btn_join_conference)
    public void joinConference(){
        if (validateConferenceId(mCid=etConferenceId.getText().toString())) {
            Intent intent = new Intent(context,ConferenceActivity.class);
            intent.putExtra("conferenceId",mCid);
            startActivity(intent);
            btnJoin.setEnabled(false);
        }else {
            Toast.makeText(context,"会议 ID 不能为空",Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateConferenceId(String cid) {
        if (!TextUtils.isEmpty(cid)){
            return true;
        }
        return false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        btnJoin.setEnabled(true);
    }
}
