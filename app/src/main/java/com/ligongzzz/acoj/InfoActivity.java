package com.ligongzzz.acoj;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String name = bundle.getString("name"),
                username = bundle.getString("username"),
                txt1 = bundle.getString("txt1"),
                txt2 = bundle.getString("txt2");

        ((TextView)findViewById(R.id.name_text)).setText(name);
        ((TextView)findViewById(R.id.username_text)).setText(username);
        ((TextView)findViewById(R.id.problem_text)).setText(txt1);
        ((TextView)findViewById(R.id.submit_text)).setText(txt2);
    }

    public void onClearClick(View view){
        new AlertDialog.Builder(this)
                .setTitle("清除用户信息")
                .setMessage("确认要清除用户信息吗？清除后需要重新登录您的账户。")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        setResult(RESULT_OK);
                        finish();
                    }
                })
                .show();
    }
}
