package com.ligongzzz.acoj;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void onSaveClick(View view){
        Intent intent = new Intent();
        intent.putExtra("username",((TextView)findViewById(R.id.username)).getText().toString());
        intent.putExtra("password",((TextView)findViewById(R.id.password)).getText().toString());
        setResult(RESULT_OK, intent);
        finish();
    }
}
