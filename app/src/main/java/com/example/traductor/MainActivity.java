package com.example.traductor;

import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText inputText = findViewById(R.id.editText);
        TextView translatedText = findViewById(R.id.translatedText);

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
                        .setConfidenceThreshold(0.20f)
                        .build());
    }

    private void identifyLanguageAndTranslate(String text, TextView translatedTextView) {
        languageIdentifier.identifyLanguage(text)
        .addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String languageCode) {
                if (languageCode == null || languageCode.equals("und")) {
                    translatedTextView.setText("Idioma no reconocido o no soportado.");
                    return;
                }

                if (!languageCode.equals(TranslateLanguage.ENGLISH)) {
                    TranslatorOptions options = new TranslatorOptions.Builder()
                            .setSourceLanguage(languageCode)
                            .setTargetLanguage(TranslateLanguage.ENGLISH)
                            .build();
                    translator = Translation.getClient(options);

                    translator.downloadModelIfNeeded()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    translateText(text, translatedTextView);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    translatedTextView.setText("Error al descargar el modelo: " + e.getMessage());
                                }
                            });
                } else {
                    translatedTextView.setText(text); // No se necesita traducción
                }
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                translatedTextView.setText("Error al identificar el idioma: " + e.getMessage());
            }
        });
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
        translator.close();
        languageIdentifier.close();
    }
}
