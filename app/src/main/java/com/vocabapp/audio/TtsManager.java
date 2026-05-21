package com.vocabapp.audio;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import com.vocabapp.domain.enums.PronunciationAccent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.qualifiers.ApplicationContext;

public class TtsManager {

    private final TextToSpeech tts;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    // Speak requests that arrived before the engine finished initializing, replayed on init.
    private final List<Runnable> pendingSpeak = new ArrayList<>();
    private boolean isReady = false;
    private boolean muted = false;

    public TtsManager(@ApplicationContext Context context) {
        tts = new TextToSpeech(context.getApplicationContext(), status -> {
            isReady = (status == TextToSpeech.SUCCESS);
            if (isReady) mainHandler.post(this::flushPending);
        });
    }

    // Returns true (and queues the action) if the engine isn't ready yet. A flush request
    // starts a new logical playback, so it supersedes any earlier pending segments.
    private boolean deferIfNotReady(boolean flush, Runnable action) {
        if (isReady) return false;
        if (flush) pendingSpeak.clear();
        pendingSpeak.add(action);
        return true;
    }

    private void flushPending() {
        if (pendingSpeak.isEmpty()) return;
        List<Runnable> toRun = new ArrayList<>(pendingSpeak);
        pendingSpeak.clear();
        for (Runnable r : toRun) r.run();
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
        if (muted) stop();
    }

    public boolean isMuted() {
        return muted;
    }

    public void speakEnglish(String text, PronunciationAccent accent, String utteranceId) {
        if (muted || text == null || text.isEmpty()) return;
        if (deferIfNotReady(true, () -> speakEnglish(text, accent, utteranceId))) return;
        Locale locale = accent == PronunciationAccent.BRITISH ? Locale.UK : Locale.US;
        tts.setLanguage(locale);
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }

    public void speakChinese(String text, String utteranceId) {
        if (muted || text == null || text.isEmpty()) return;
        if (deferIfNotReady(true, () -> speakChinese(text, utteranceId))) return;
        tts.setLanguage(Locale.SIMPLIFIED_CHINESE);
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }

    public void speakChineseQueued(String text, boolean flush, long pauseAfterMs, String utteranceId) {
        if (muted || text == null || text.isEmpty()) return;
        if (deferIfNotReady(flush, () -> speakChineseQueued(text, flush, pauseAfterMs, utteranceId))) return;
        tts.setLanguage(Locale.SIMPLIFIED_CHINESE);
        tts.speak(text, flush ? TextToSpeech.QUEUE_FLUSH : TextToSpeech.QUEUE_ADD, null, utteranceId);
        if (pauseAfterMs > 0) {
            tts.playSilentUtterance(pauseAfterMs, TextToSpeech.QUEUE_ADD, utteranceId + "_pause");
        }
    }

    public void stop() {
        pendingSpeak.clear();
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
