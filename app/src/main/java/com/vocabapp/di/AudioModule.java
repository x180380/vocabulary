package com.vocabapp.di;

import android.content.Context;

import com.vocabapp.audio.AudioSessionManager;
import com.vocabapp.audio.TtsManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class AudioModule {

    @Provides
    @Singleton
    public TtsManager provideTtsManager(@ApplicationContext Context context) {
        return new TtsManager(context);
    }

    @Provides
    @Singleton
    public AudioSessionManager provideAudioSessionManager(@ApplicationContext Context context) {
        return new AudioSessionManager(context);
    }
}
