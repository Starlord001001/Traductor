package com.example.traductor;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentificationOptions;
import com.google.mlkit.nl.languageid.LanguageIdentifier;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

public class MainActivity extends AppCompatActivity {

    private Translator translator;
    private LanguageIdentifier languageIdentifier;
    private TranslationService translationService;
    private boolean isServiceBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText inputText = findViewById(R.id.editText);
        TextView translatedText = findViewById(R.id.translatedText);

        // Vincula el servicio cuando se inicia la actividad
        Intent serviceIntent = new Intent(this, TranslationService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        findViewById(R.id.translateButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = inputText.getText().toString();
                identifyLanguageAndTranslate(text, translatedText);
            }
        });

        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage("")  // or setSourceLanguage(null)
                .setTargetLanguage(TranslateLanguage.ENGLISH)
                .build();
        translator = Translation.getClient(options);

        languageIdentifier = LanguageIdentification.getClient(
                new LanguageIdentificationOptions.Builder()
                        .setConfidenceThreshold(0.34f)
                        .build());
    }

    private void identifyLanguageAndTranslate(String text, TextView translatedTextView) {
        if (isServiceBound) {
            // Utiliza el servicio para realizar la identificación del idioma y la traducción
            translationService.identifyLanguageAndTranslate(text, translatedTextView);
        } else {
            // Realiza la identificación del idioma y la traducción localmente (como antes)
            languageIdentifier.identifyLanguage(text)
                    .addOnSuccessListener(new OnSuccessListener<String>() {
                        @Override
                        public void onSuccess(String languageCode) {
                            translateText(text, translatedTextView);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            translatedTextView.setText("Error al identificar el idioma: " + e.getMessage());
                        }
                    });
        }
    }

    private void translateText(String text, TextView translatedTextView) {
        translator.translate(text)
                .addOnSuccessListener(
                        new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(String translatedText) {
                                translatedTextView.setText(translatedText);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                translatedTextView.setText("Error: " + e.getMessage());
                            }
                        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Desvincula el servicio cuando se destruye la actividad
        if (isServiceBound) {
            unbindService(serviceConnection);
            isServiceBound = false;
        }
        translator.close();
        languageIdentifier.close();
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            TranslationService.LocalBinder binder = (TranslationService.LocalBinder) iBinder;
            translationService = binder.getService();
            isServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isServiceBound = false;
        }
    };
}
