package com.vocabapp.audio;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import com.vocabapp.domain.enums.PronunciationAccent;

import java.util.Locale;

import dagger.hilt.android.qualifiers.ApplicationContext;

public class TtsManager {

    private final TextToSpeech tts;
    private boolean isReady = false;
    private boolean muted = false;

    public TtsManager(@ApplicationContext Context context) {
        tts = new TextToSpeech(context.getApplicationContext(), status -> {
            isReady = (status == TextToSpeech.SUCCESS);
        });
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
        if (muted) stop();
    }

    public boolean isMuted() {
        return muted;
    }

    public void speakEnglish(String text, PronunciationAccent accent, String utteranceId) {
        if (!isReady || muted || text == null || text.isEmpty()) return;
        Locale locale = accent == PronunciationAccent.BRITISH ? Locale.UK : Locale.US;
        tts.setLanguage(locale);
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }

    public void speakChinese(String text, String utteranceId) {
        if (!isReady || muted || text == null || text.isEmpty()) return;
        tts.setLanguage(Locale.SIMPLIFIED_CHINESE);
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }

    public void speakQueue(String text, Locale locale, String utteranceId) {
        if (!isReady || text == null || text.isEmpty()) return;
        tts.setLanguage(locale);
        tts.speak(text, TextToSpeech.QUEUE_ADD, null, utteranceId);
    }

    public void stop() {
        if (isReady) tts.stop();
    }

    public void setProgressListener(UtteranceProgressListener listener) {
        tts.setOnUtteranceProgressListener(listener);
    }

    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

    public boolean isReady() {
        return isReady;
    }
}
