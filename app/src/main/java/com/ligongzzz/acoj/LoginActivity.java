package com.ligongzzz.acoj;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void onSaveClick(View view){
        Intent intent = new Intent();
        String username = ((TextView)findViewById(R.id.username)).getText().toString(),
                password = ((TextView)findViewById(R.id.password)).getText().toString();

        if(username.isEmpty()||password.isEmpty()){
            Toast.makeText(this,"用户名或密码不能为空！",Toast.LENGTH_SHORT).show();
        }
        else {
            intent.putExtra("username", username);
            intent.putExtra("password", password);
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}
