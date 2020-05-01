package com.example.capturetranslate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private static final int RESULT_LOAD_IMG = 1 ;
    private Button captureImageBtn, detectTextBtn, uploadBtn;
    private ImageView imageView;

    private Bitmap imageBitmap, galleryBitmap, camera, gallery;
    private BitmapDrawable frmCamera, frmGallery;

    static final int REQUEST_IMAGE_CAPTURE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        captureImageBtn = findViewById(R.id.capture_image_btn);
        detectTextBtn = findViewById(R.id.detect_text_image_btn);
        imageView = findViewById(R.id.image_view);

        //textView.setMovementMethod(new ScrollingMovementMethod());
        uploadBtn = findViewById(R.id.upload_image_btn);

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });


        captureImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dispatchTakePictureIntent();
                //TranslateActivity.mSourceText.setText("");

            }
        });

        detectTextBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (imageView.getDrawable()!= null){
                BitmapDrawable check = (BitmapDrawable) imageView.getDrawable();

                if (check == frmGallery ) {
                    detectTextFromGallery();

                }
                else if(check == frmCamera){
                    detectTextFromCamera();
                }
                else{
                    Toast.makeText(MainActivity.this, "No Text :(", Toast.LENGTH_LONG).show();
                }
                Intent activity2Intent = new Intent(getApplicationContext(), TranslateActivity.class);
                startActivity(activity2Intent);
            }}
        });

    }

    private void chooseImage() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, 0);
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, 1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult( requestCode, resultCode, data );
        switch(requestCode){
            case 0:
                if(data != null){
                try {
                    final Uri imageUri = data.getData();
                    final InputStream imageStream = getContentResolver().openInputStream( imageUri );
                    galleryBitmap = BitmapFactory.decodeStream( imageStream );
                    imageView.setImageBitmap( galleryBitmap );
                    frmGallery = (BitmapDrawable) imageView.getDrawable();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText( MainActivity.this, "Something went wrong", Toast.LENGTH_LONG ).show();
                }}// Do your stuff here...
                break;
            case 1:
                if(data != null){
                Bundle extras = data.getExtras();
                imageBitmap = (Bitmap) extras.get( "data" );
                imageView.setImageBitmap( imageBitmap );
                frmCamera = (BitmapDrawable) imageView.getDrawable();// Do your other stuff here...
                break;
            }
            ///case etc:
                //break;
        }
    }

    private void detectTextFromCamera(){

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        detector.processImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                processTxt(firebaseVisionText);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "No Text Detected: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void detectTextFromGallery(){

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(galleryBitmap);
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        detector.processImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                processTxt(firebaseVisionText);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "No Text Detected: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

        private void processTxt(FirebaseVisionText firebaseVisionText){
            List<FirebaseVisionText.TextBlock> blocks = firebaseVisionText.getTextBlocks();
                if (blocks.size() == 0) {
                        Toast.makeText(MainActivity.this, "No Text :(", Toast.LENGTH_LONG).show();
                        return;
                    } else {
                        for (FirebaseVisionText.TextBlock block : firebaseVisionText.getTextBlocks()) {
                            String txt = block.getText();
                            TranslateActivity.mSourceText.setTextSize(20);
                            TranslateActivity.mSourceText.setText(txt);

                        }
                    }
                }
            }
