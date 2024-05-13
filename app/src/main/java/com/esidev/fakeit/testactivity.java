package com.esidev.fakeit;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class testactivity extends AppCompatActivity {
    private VideoView videoView;
    private TextView resultView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_testactivity);

        videoView = findViewById(R.id.videoView);
        resultView = findViewById(R.id.resultView);

        // Load the video resource
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.loadingscan);
        videoView.setVideoURI(videoUri);

        // Set up video completion listener
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // Show the result on a Toast message
                String result = "Your image is genuine."; // Replace with your actual result
                Toast.makeText(testactivity.this, result, Toast.LENGTH_LONG).show();
            }
        });

        // Start playing the video
        videoView.start();

    }
    }
