package com.example.capturetranslate;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.languageid.FirebaseLanguageIdentification;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;
import java.util.Locale;

public class TranslateActivity extends AppCompatActivity {

    public static EditText mSourceText;
    private TextView mSourceLang;
    private Button mTranslateBtn;
    private ImageButton addVoice, listen, stop, share, shareTop;
    private TextView mTranslatedText;
    private String sourceText;
    TextToSpeech mTTS;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_translate );
        mSourceLang = findViewById( R.id.sourceLang );
        mSourceText = findViewById( R.id.sourceText );
        mTranslateBtn = findViewById( R.id.translate );
        addVoice = findViewById( R.id.voice );
        listen = findViewById( R.id.listen );
        stop = findViewById( R.id.stop );
        share = findViewById( R.id.share );
        shareTop = findViewById( R.id.shareTop );
        mTranslatedText = findViewById( R.id.translatedText );
        mSourceText.setMovementMethod( new ScrollingMovementMethod() );
        mTranslatedText.setMovementMethod( new ScrollingMovementMethod() );


        mTTS = new TextToSpeech( getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    mTTS.setLanguage( Locale.US );
                } else {
                    Toast.makeText( TranslateActivity.this, "Error", Toast.LENGTH_SHORT ).show();
                }

            }
        } );

        listen.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String toSpeak = mTranslatedText.getText().toString().trim();
                if (toSpeak.equals( "" )) {
                    Toast.makeText( TranslateActivity.this, "No translated text!", Toast.LENGTH_SHORT ).show();
                } else {
                    Toast.makeText( TranslateActivity.this, toSpeak, Toast.LENGTH_SHORT ).show();
                    //speak the text
                    mTTS.speak( toSpeak, TextToSpeech.QUEUE_FLUSH, null );
                }
            }
        } );

        stop.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTTS.isSpeaking()) {
                    mTTS.stop();

                } else {
                    Toast.makeText( TranslateActivity.this, "Not Speaking!", Toast.LENGTH_SHORT ).show();
                }
            }
        } );

        share.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = mTranslatedText.getText().toString();
                Intent sharingIntent = new Intent( Intent.ACTION_SEND);
                sharingIntent.setType( "text/plain" );
                sharingIntent.putExtra( Intent.EXTRA_SUBJECT, "Subject Here" );
                sharingIntent.putExtra(Intent.EXTRA_TEXT, text);
                startActivity( Intent.createChooser( sharingIntent, "Share translated text via" ) );
            }
        } );

        shareTop.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = mSourceText.getText().toString();
                Intent sharingIntent = new Intent( Intent.ACTION_SEND);
                sharingIntent.setType( "text/plain" );
                sharingIntent.putExtra( Intent.EXTRA_SUBJECT, "Subject Here" );
                sharingIntent.putExtra(Intent.EXTRA_TEXT, text);
                startActivity( Intent.createChooser( sharingIntent, "Share translated text via" ) );
            }
        } );


        mTranslateBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                identifyLanguage();
            }
        } );

        addVoice.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent voiceIntent = new Intent( RecognizerIntent.ACTION_RECOGNIZE_SPEECH );
                voiceIntent.putExtra( RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM );
                voiceIntent.putExtra( RecognizerIntent.EXTRA_PROMPT, "Speech to Text" );
                startActivityForResult( voiceIntent, 1 );
            }
        } );
    }



    @Override
    protected void onPause() {
        if (mTTS != null || mTTS.isSpeaking()) {
            mTTS.stop();
        super.onPause();
    }}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult( requestCode, resultCode, data );
        if(requestCode == 1 && resultCode == RESULT_OK){
            ArrayList<String> matches = data.getStringArrayListExtra( RecognizerIntent.EXTRA_RESULTS );
            mSourceText.setText( matches.get(0).toString() );


        }
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
                        //mTranslatedText.setTextSize( 20 );
                        mTranslatedText.setText( s );
                    }
                } );
            }
        } );
    }
}
