package com.example.traductor;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Translator translator;
    private LanguageIdentifier languageIdentifier;
    private TranslationService translationService;
    private boolean isServiceBound = false;
    private String targetLanguageCode = TranslateLanguage.ENGLISH; // Idioma de destino predeterminado

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EditText inputText = findViewById(R.id.editText);
        TextView translatedText = findViewById(R.id.translatedText);
        setupLanguageSpinner();
        Intent serviceIntent = new Intent(this, TranslationService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        findViewById(R.id.translateButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isServiceBound) {
                    String text = inputText.getText().toString();
                    translationService.identifyLanguageAndTranslate(text, translatedText, targetLanguageCode);
                } else {
                    Toast.makeText(MainActivity.this, "Servicio no vinculado", Toast.LENGTH_SHORT).show();
                }
            }
        });

        languageIdentifier = LanguageIdentification.getClient(
                new LanguageIdentificationOptions.Builder()
                        .setConfidenceThreshold(0.34f)
                        .build());
    }

    private void setupLanguageSpinner() {
        Spinner languageSpinner = findViewById(R.id.languageSpinner);

        final List<String> languageCodes = Arrays.asList(
                TranslateLanguage.AFRIKAANS,
                TranslateLanguage.ARABIC,
                TranslateLanguage.BELARUSIAN,
                TranslateLanguage.BENGALI,
                TranslateLanguage.BULGARIAN,
                TranslateLanguage.CATALAN,
                TranslateLanguage.CHINESE,
                TranslateLanguage.CROATIAN,
                TranslateLanguage.CZECH,
                TranslateLanguage.WELSH,
                TranslateLanguage.DANISH,
                TranslateLanguage.DUTCH,
                TranslateLanguage.ENGLISH,
                TranslateLanguage.ESTONIAN,
                TranslateLanguage.FINNISH,
                TranslateLanguage.FRENCH,
                TranslateLanguage.GERMAN,
                TranslateLanguage.GREEK,
                TranslateLanguage.GUJARATI,
                TranslateLanguage.HEBREW,
                TranslateLanguage.HINDI,
                TranslateLanguage.HUNGARIAN,
                TranslateLanguage.ICELANDIC,
                TranslateLanguage.INDONESIAN,
                TranslateLanguage.IRISH,
                TranslateLanguage.ITALIAN,
                TranslateLanguage.JAPANESE,
                TranslateLanguage.KANNADA,
                TranslateLanguage.KOREAN,
                TranslateLanguage.LATVIAN,
                TranslateLanguage.LITHUANIAN,
                TranslateLanguage.MACEDONIAN,
                TranslateLanguage.MALAY,
                TranslateLanguage.MALTESE,
                TranslateLanguage.MARATHI,
                TranslateLanguage.NORWEGIAN,
                TranslateLanguage.PERSIAN,
                TranslateLanguage.POLISH,
                TranslateLanguage.PORTUGUESE,
                TranslateLanguage.ROMANIAN,
                TranslateLanguage.RUSSIAN,
                TranslateLanguage.SLOVAK,
                TranslateLanguage.SLOVENIAN,
                TranslateLanguage.SPANISH,
                TranslateLanguage.SWAHILI,
                TranslateLanguage.SWEDISH,
                TranslateLanguage.TAMIL,
                TranslateLanguage.TELUGU,
                TranslateLanguage.THAI,
                TranslateLanguage.TURKISH,
                TranslateLanguage.UKRAINIAN,
                TranslateLanguage.URDU,
                TranslateLanguage.VIETNAMESE
        );


        List<String> languageNames = Arrays.asList(
                "Afrikáans",
                "Árabe",
                "Bielorruso",
                "Bengalí",
                "Búlgaro",
                "Catalán",
                "Chino",
                "Croata",
                "Checo",
                "Galés",
                "Danés",
                "Holandés",
                "Inglés",
                "Estonio",
                "Finés",
                "Francés",
                "Alemán",
                "Griego",
                "Gujarati",
                "Hebreo",
                "Hindi",
                "Húngaro",
                "Islandés",
                "Indonesio",
                "Irlandés",
                "Italiano",
                "Japonés",
                "Kannada",
                "Coreano",
                "Letón",
                "Lituano",
                "Macedonio",
                "Malayo",
                "Malayalam",
                "Maltés",
                "Marathi",
                "Noruego",
                "Persa",
                "Polaco",
                "Portugués",
                "Rumano",
                "Ruso",
                "Serbio",
                "Eslovaco",
                "Esloveno",
                "Español",
                "Suajili",
                "Sueco",
                "Tamil",
                "Telugu",
                "Tailandés",
                "Turco",
                "Ucraniano",
                "Urdu",
                "Vietnamita"
        );

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, languageNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(adapter);

        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                targetLanguageCode = languageCodes.get(position);
                updateTranslatorLanguage(targetLanguageCode);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void updateTranslatorLanguage(String targetLanguageCode) {
        this.targetLanguageCode = targetLanguageCode;
        Toast.makeText(this, targetLanguageCode, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
