package com.vocabapp.audio;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.vocabapp.domain.enums.PlaybackMode;
import com.vocabapp.domain.model.Word;

import java.util.List;

public class AudioServiceConnection implements ServiceConnection {

    public interface ServiceConnectedCallback {
        void onConnected(AudioService service);
    }

    private AudioService audioService;
    private ServiceConnectedCallback callback;
    private boolean bound = false;

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        audioService = ((AudioService.LocalBinder) binder).getService();
        bound = true;
        if (callback != null) callback.onConnected(audioService);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        audioService = null;
        bound = false;
    }

    public void bind(Context context, ServiceConnectedCallback cb) {
        this.callback = cb;
        Intent intent = new Intent(context, AudioService.class);
        context.bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    public void unbind(Context context) {
        if (bound) {
            context.unbindService(this);
            bound = false;
        }
    }

    public void startPlayback(Context context, long vocabBookId, PlaybackMode mode, List<Word> words) {
        Intent intent = new Intent(context, AudioService.class);
        intent.setAction(AudioService.ACTION_START);
        intent.putExtra(AudioService.EXTRA_VOCAB_BOOK_ID, vocabBookId);
        intent.putExtra(AudioService.EXTRA_PLAYBACK_MODE, mode.name());
        context.startForegroundService(intent);
        if (audioService != null) audioService.setWords(words);
    }

    public void stop(Context context) {
        Intent intent = new Intent(context, AudioService.class);
        intent.setAction(AudioService.ACTION_STOP);
        context.startService(intent);
    }
}
