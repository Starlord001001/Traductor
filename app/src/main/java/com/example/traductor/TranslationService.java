package com.example.traductor;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentificationOptions;
import com.google.mlkit.nl.languageid.LanguageIdentifier;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

public class TranslationService extends Service {

    private final IBinder binder = new LocalBinder();
    private LanguageIdentifier languageIdentifier;
    private Translator translator;

    public class LocalBinder extends Binder {
        TranslationService getService() {
            return TranslationService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initializeMLKit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        translator.close();
        languageIdentifier.close();
    }

    private void initializeMLKit() {
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

    public void identifyLanguageAndTranslate(String text, final TextView translatedTextView, String targetLanguageCode) {
        languageIdentifier.identifyLanguage(text)
            .addOnSuccessListener(new OnSuccessListener<String>() {
                @Override
                public void onSuccess(String languageCode) {
                    if (languageCode == null || languageCode.equals("und")) {
                        translatedTextView.setText("Idioma no reconocido o no soportado.");
                        return;
                    }

                    TranslatorOptions options = new TranslatorOptions.Builder()
                            .setSourceLanguage(languageCode)
                            .setTargetLanguage(targetLanguageCode)
                            .build();
                    translator = Translation.getClient(options);

                    translator.downloadModelIfNeeded()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                // Traduce el texto
                                translateText(text, translatedTextView);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                translatedTextView.setText("Error al descargar el modelo: " + e.getMessage());
                            }
                        });
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    translatedTextView.setText("Error al identificar el idioma: " + e.getMessage());
                }
            });
    }

    private void translateText(String text, final TextView translatedTextView) {
        translator.translate(text)
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String translatedText) {
                        translatedTextView.setText(translatedText);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        translatedTextView.setText("Error: " + e.getMessage());
                    }
                });
    }
}
