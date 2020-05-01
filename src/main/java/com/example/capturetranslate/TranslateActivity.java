package com.example.capturetranslate;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Paint;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.languageid.FirebaseLanguageIdentification;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

public class TranslateActivity extends AppCompatActivity {

    public static EditText mSourceText;
    private TextView mSourceLang;
    private Button mTranslateBtn;
    private TextView mTranslatedText;
    private String sourceText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_translate );
        mSourceLang = findViewById( R.id.sourceLang );
        mSourceText = findViewById( R.id.sourceText );
        mTranslateBtn = findViewById( R.id.translate );
        mTranslatedText = findViewById( R.id.translatedText );
        mSourceText.setMovementMethod(new ScrollingMovementMethod());
        mTranslatedText.setMovementMethod(new ScrollingMovementMethod());
        
        mTranslateBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                identifyLanguage();
            }
        } );

    }

    private void identifyLanguage() {
        sourceText = mSourceText.getText().toString();

        FirebaseLanguageIdentification identifier = FirebaseNaturalLanguage.getInstance().getLanguageIdentification();

        mSourceLang.setText( "Detecting... ");

        identifier.identifyLanguage( sourceText ).addOnSuccessListener( new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                if(s.equals( "und" )){
                    Toast.makeText( getApplicationContext(), "Language Not Identified", Toast.LENGTH_SHORT).show();
                }
                else {
                    getLanguageCode(s);
                }
            }
        } );

    }

    private void getLanguageCode(String language) {
        int langCode;
        switch (language){
            case "hi":
                langCode = FirebaseTranslateLanguage.HI;
                mSourceLang.setText( "Hindi" );
                break;

            case "fr":
                langCode = FirebaseTranslateLanguage.FR;
                mSourceLang.setText( "French" );
                break;

            case "es":
                langCode = FirebaseTranslateLanguage.ES;
                mSourceLang.setText( "Spanish" );
                break;
            default:
                langCode = 0;
        }

        translateText(langCode);
    }

    private void translateText(int langCode) {
        mTranslatedText.setText( "Translating..." );
        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
                //from language
                .setSourceLanguage( langCode )
                //to language
                .setTargetLanguage( FirebaseTranslateLanguage.EN )
                .build();

        final FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance().getTranslator( options );
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();

        translator.downloadModelIfNeeded(conditions).addOnSuccessListener( new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                translator.translate( sourceText ).addOnSuccessListener( new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        mTranslatedText.setTextSize( 20 );
                        mTranslatedText.setText( s );
                    }
                } );
            }
        } );
    }
}
