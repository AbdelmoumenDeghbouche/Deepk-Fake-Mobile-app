package com.esidev.fakeit;

import static android.content.ContentValues.TAG;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Outline;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.esidev.fakeit.ml.ModelTflite;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imageView;
    private Interpreter tflite;
    private ProgressBar progressBar;
    private TextView fake_real_image_text_status;
    private TextView fake_real_image_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        imageView = findViewById(R.id.imageView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        Button selectImageButton = findViewById(R.id.selectImageButton);
        fake_real_image_text_status = findViewById(R.id.fake_real_image_text_status);
        fake_real_image_text = findViewById(R.id.fake_real_image_text);
        final Handler handler = new Handler();
        makeImageCornerRound();
        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
    }


    private void makeImageCornerRound() {
        float radius = 30f;

        // Set the rounded outline for the ImageView
        imageView.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
            }
        });
        imageView.setClipToOutline(true);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                progressBar.setVisibility(View.VISIBLE);
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageView.setImageBitmap(bitmap);
                runInference(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void runInference(Bitmap bitmap) {
        // Preprocess the input image
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true);
        ByteBuffer byteBuffer = bitmapToByteBuffer(resizedBitmap);

        try {
            ModelTflite model = ModelTflite.newInstance(this);

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            ModelTflite.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            // Process the output predictions
            float[] predictions = outputFeature0.getFloatArray();
            float score = predictions[0]; // Assuming a single output value
            Log.d(TAG, "runInference: "+score);
            String prediction;
            Log.d(TAG, "output: "+ predictions[0]);
            if (score < 0.5) {
                Log.d(TAG, "i am here: ");
                prediction = "Fake";
                fake_real_image_text_status.setTextColor(getResources().getColor(R.color.red));
                fake_real_image_text_status.setText("Fake");
            } else {
                Log.d(TAG, "i am there: ");
                prediction = "Real";
                fake_real_image_text_status.setTextColor(getResources().getColor(R.color.green));
                fake_real_image_text_status.setText("Real");
            }

            // Display the prediction result in a Toast message
            Toast.makeText(this, "Prediction: " + prediction, Toast.LENGTH_SHORT).show();

            fake_real_image_text.setVisibility(View.VISIBLE);
            fake_real_image_text_status.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            // Releases model resources
            model.close();
        } catch (IOException e) {
            // Handle the exception
            e.printStackTrace();
        }
    }

    public ByteBuffer bitmapToByteBuffer(Bitmap bitmap) {
        // Resize the bitmap to the required input size of your model
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true);

        // Allocate space for the ByteBuffer
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3);
        byteBuffer.order(ByteOrder.nativeOrder());

        // Extract pixel values and normalize
        int[] intValues = new int[224 * 224];
        float[] floatValues = new float[224 * 224 * 3];
        resizedBitmap.getPixels(intValues, 0, resizedBitmap.getWidth(), 0, 0, resizedBitmap.getWidth(), resizedBitmap.getHeight());
        for (int i = 0; i < intValues.length; ++i) {
            final int val = intValues[i];
            floatValues[i * 3 + 0] = ((val >> 16) & 0xFF) / 255.0f;
            floatValues[i * 3 + 1] = ((val >> 8) & 0xFF) / 255.0f;
            floatValues[i * 3 + 2] = (val & 0xFF) / 255.0f;
        }

        // Copy floatValues array into the ByteBuffer
        for (float floatValue : floatValues) {
            byteBuffer.putFloat(floatValue);
        }

        // Rewind the buffer before returning
        byteBuffer.rewind();

        return byteBuffer;
    }
}
