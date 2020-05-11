package com.example.capturetranslate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {


    private static final int RESULT_LOAD_IMG = 1 ;
    private Button captureImageBtn, detectTextBtn, uploadBtn;
    private ImageView imageView;

    private Bitmap imageBitmap, galleryBitmap, camera, gallery;
    private BitmapDrawable frmCamera, frmGallery, frmScreen;
    String cameraFilePath;

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
                try{
                if (imageView.getDrawable()!= null){
                BitmapDrawable check = (BitmapDrawable) imageView.getDrawable();

                if (check == frmGallery ) {
                    detectTextFromGallery();

                }
                else if(check == frmCamera){
                    try {
                        detectTextFromCamera();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }}
                else{
                    Toast.makeText(MainActivity.this, "No Image Uploaded!", Toast.LENGTH_LONG).show();
                }
                    Intent activity2Intent = new Intent(getApplicationContext(), TranslateActivity.class);
                    startActivity(activity2Intent);
            } catch (ClassCastException e){

                }
        }} );
    }
        //frmScreen = (BitmapDrawable) imageView.getDrawable();



    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        cameraFilePath = image.getAbsolutePath();
        return image;
    }


    private void chooseImage() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, 0);
    }


    private void dispatchTakePictureIntent() {
        /*try {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(this,
                    BuildConfig.APPLICATION_ID + ".provider", createImageFile()));
            startActivityForResult(intent, 1);
        } catch (IOException ex) {
            ex.printStackTrace();
        }*/

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, 1);
            }
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
                File file = new File(cameraFilePath);
                try {
                    imageBitmap =  MediaStore.Images.Media.getBitmap(this.getContentResolver(),Uri.fromFile(file));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (imageBitmap !=null){
                    imageView.setImageBitmap( rotateImage(imageBitmap) );
                    frmCamera = (BitmapDrawable) imageView.getDrawable();
                }
                break;
            }
            ///case etc:
                //break;
        }

    private Bitmap rotateImage(Bitmap bitmap) {
        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface( cameraFilePath );

        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = exifInterface.getAttributeInt( ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED );
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate( 90 );
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate( 180 );
                break;

            default:
        }
        Bitmap rotatedBitmap = Bitmap.createBitmap( bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true );
        return rotatedBitmap;

    }

    private void detectTextFromCamera() throws IOException {

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap( imageBitmap );
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
                            //TranslateActivity.mSourceText.setTextSize(20);
                            TranslateActivity.mSourceText.setText(txt);

                        }
                    }
                }
            }
