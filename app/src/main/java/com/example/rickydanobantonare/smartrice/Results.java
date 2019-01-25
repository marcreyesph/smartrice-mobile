package com.example.rickydanobantonare.smartrice;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Results extends AppCompatActivity {

    private static final String MODEL_PATH = "graph.lite";
    private static final String LABEL_PATH = "labels.txt";
    private static final int INPUT_SIZE = 224;

    private Classifier classifier;
    private Executor executor = Executors.newSingleThreadExecutor();

    TextView textView;
    private int IMAGE_GALLERY_REQUEST = 20;
    int CAPTURE_IMAGE_REQUEST = 100;
    ImageView imgPicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        imgPicture = (ImageView)findViewById(R.id.imageView3);

        textView = (TextView) findViewById(R.id.result);
        String text = "";
        textView.setText(text);
        textView.setMovementMethod(new ScrollingMovementMethod());

        ImageButton androidImageButton = (ImageButton) findViewById(R.id.imageButton3);
        androidImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backActivity();
            }
        });


        androidImageButton = (ImageButton) findViewById(R.id.buttonCamera);
        androidImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAPTURE_IMAGE_REQUEST);
            }
        });

        /* TODO:
        Read ImageView data and show results.
         */

        Button detectButton = (Button) findViewById(R.id.button3);
        detectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bitmap = ((BitmapDrawable)imgPicture.getDrawable()).getBitmap();
                bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);
                final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);
                textView.setText(results.toString());
                initTensorFlowAndLoadModel();
            }
        });

    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap = (Bitmap)data.getExtras().get("data");
        imgPicture.setImageBitmap(bitmap);
    }*/

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == CAPTURE_IMAGE_REQUEST){
            Bitmap image = (Bitmap)data.getExtras().get("data");
            imgPicture.setImageBitmap(image);
        }

        if (resultCode == RESULT_OK && requestCode == IMAGE_GALLERY_REQUEST) {
            // if we are here, we are hearing back from the image gallery.

            // the address of the image on the SD Card.
            Uri imageUri = data.getData();

            // declare a stream to read the image data from the SD Card.
            InputStream inputStream;

            // we are getting an input stream, based on the URI of the image.
            try {
                inputStream = getContentResolver().openInputStream(imageUri);

                // get a bitmap from the stream.
                Bitmap image = BitmapFactory.decodeStream(inputStream);


                // show the image to the user
                imgPicture.setImageBitmap(image);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }


    }
    public void btnClick(View v){
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);

        // where do we want to find the data?
        File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String pictureDirectoryPath = pictureDirectory.getPath();
        // finally, get a URI representation
        Uri data = Uri.parse(pictureDirectoryPath);

        // set the data and type.  Get all image types.
        photoPickerIntent.setDataAndType(data, "image/*");

        // we will invoke this activity, and get something back from it.
        startActivityForResult(photoPickerIntent, IMAGE_GALLERY_REQUEST);
    }

    public void backActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
    @Override
    public void onBackPressed(){
        Intent intent = new Intent(Results.this, MainActivity.class );
        startActivity(intent);
    }

    private void initTensorFlowAndLoadModel() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier = TensorFlowImageClassifier.create(
                            getAssets(),
                            MODEL_PATH,
                            LABEL_PATH,
                            INPUT_SIZE);
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
    }
}