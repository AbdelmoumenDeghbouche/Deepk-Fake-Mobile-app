package com.esidev.fakeit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.esidev.fakeit.ml.Generator;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;

public class GenearteImgaesActivity extends AppCompatActivity {
    private static final String TAG = "";
    private Generator model;
    private ImageView image_generated_fake;
    private Button generate_fake_images_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_genearte_imgaes);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initViews();
        generate_fake_images_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateImagesByModel();

            }
        });
    }

    private void initViews() {
        image_generated_fake = (ImageView) findViewById(R.id.image_generated_fake);
        generate_fake_images_btn = (Button) findViewById(R.id.generate_fake_images_btn);
    }

    private void generateImagesByModel() {

        try {
            model = Generator.newInstance(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Generate and display a fake image
        generateAndDisplayFakeImage();
    }

    private void generateAndDisplayFakeImage() {
        new Thread(() -> {
            try {
                // Generate the image on a separate thread
                Bitmap generatedImage = generateSingleImage();

                // Update the UI on the main thread
                runOnUiThread(() -> {
                    // Display the generated image
                    image_generated_fake.setImageBitmap(generatedImage);

                    // Release model resources
                    model.close();
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private Bitmap generateSingleImage() throws IOException {
        // Create a random latent vector
        int latentDim = 32; // Replace with your actual latent dimension
        float[] randomLatentVector = new float[latentDim];
        Random random = new Random();
        for (int i = 0; i < latentDim; i++) {
            randomLatentVector[i] = random.nextFloat() * 2 - 1; // Random values between -1 and 1
        }

        // Convert the latent vector to a ByteBuffer
        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(randomLatentVector.length * 4);
        inputBuffer.order(ByteOrder.nativeOrder());
        for (float value : randomLatentVector) {
            inputBuffer.putFloat(value);
        }
        inputBuffer.rewind();

        // Create input tensor buffer
        TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, latentDim}, DataType.FLOAT32);
        inputFeature0.loadBuffer(inputBuffer);

        // Run model inference
        Generator.Outputs outputs = model.process(inputFeature0);
        TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

        // Convert output tensor to a Bitmap
        ByteBuffer outputBuffer = outputFeature0.getBuffer();
        byte[] outputBytes = new byte[outputBuffer.remaining()];
        outputBuffer.get(outputBytes);
        Bitmap generatedImage = BitmapFactory.decodeByteArray(outputBytes, 0, outputBytes.length);
        Log.d(TAG, "Output Tensor Shape: " + outputFeature0.getShape());
        Log.d(TAG, "Output Tensor Data Type: " + outputFeature0.getDataType());
// You can also log the tensor buffer or print its contents
        return generatedImage;
    }
}