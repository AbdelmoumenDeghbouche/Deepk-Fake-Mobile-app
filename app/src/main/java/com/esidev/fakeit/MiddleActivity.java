package com.esidev.fakeit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MiddleActivity extends AppCompatActivity {
    private Button Detect_btn;
    private Button Generate_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_middle);

        initViews();
        Detect_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MiddleActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        Generate_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MiddleActivity.this, GenearteImgaesActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initViews() {
        Detect_btn = (Button) findViewById(R.id.Detect_btn);
        Generate_btn = (Button) findViewById(R.id.Generate_btn);
    }
}