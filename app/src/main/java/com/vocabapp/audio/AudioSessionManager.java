package com.vocabapp.audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;

import dagger.hilt.android.qualifiers.ApplicationContext;

public class AudioSessionManager {

    public interface AudioFocusCallback {
        void onFocusGained();
        void onFocusLost();
        void onFocusLostTransient();
    }

    private final AudioManager audioManager;
    private AudioFocusRequest focusRequest;
    private AudioFocusCallback callback;

    private final AudioManager.OnAudioFocusChangeListener focusChangeListener = focusChange -> {
        if (callback == null) return;
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                callback.onFocusGained();
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                callback.onFocusLost();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                callback.onFocusLostTransient();
                break;
        }
    };

    public AudioSessionManager(@ApplicationContext Context context) {
        audioManager = (AudioManager) context.getApplicationContext()
                .getSystemService(Context.AUDIO_SERVICE);
    }

    public boolean requestFocus(AudioFocusCallback cb) {
        this.callback = cb;
        focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build())
                .setOnAudioFocusChangeListener(focusChangeListener)
                .build();
        return audioManager.requestAudioFocus(focusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    public void abandonFocus() {
        if (focusRequest != null) {
            audioManager.abandonAudioFocusRequest(focusRequest);
        }
        callback = null;
    }
}
