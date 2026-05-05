package com.vocabapp.audio;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.speech.tts.UtteranceProgressListener;

import androidx.core.app.NotificationCompat;

import com.vocabapp.R;
import com.vocabapp.data.local.preferences.UserPreferencesManager;
import com.vocabapp.data.repository.WordRepository;
import com.vocabapp.domain.enums.PlaybackMode;
import com.vocabapp.domain.enums.PronunciationAccent;
import com.vocabapp.domain.model.Word;
import com.vocabapp.presentation.MainActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AudioService extends Service {

    private static final String CHANNEL_ID = "vocab_playback_channel";
    private static final int NOTIFICATION_ID = 1001;

    public static final String ACTION_START = "com.vocabapp.audio.ACTION_START";
    public static final String ACTION_PAUSE = "com.vocabapp.audio.ACTION_PAUSE";
    public static final String ACTION_RESUME = "com.vocabapp.audio.ACTION_RESUME";
    public static final String ACTION_STOP = "com.vocabapp.audio.ACTION_STOP";
    public static final String ACTION_NEXT = "com.vocabapp.audio.ACTION_NEXT";
    public static final String ACTION_PREV = "com.vocabapp.audio.ACTION_PREV";

    public static final String EXTRA_VOCAB_BOOK_ID = "vocab_book_id";
    public static final String EXTRA_PLAYBACK_MODE = "playback_mode";

    public class LocalBinder extends Binder {
        public AudioService getService() { return AudioService.this; }
    }

    private final IBinder binder = new LocalBinder();

    @Inject TtsManager ttsManager;
    @Inject AudioSessionManager audioSessionManager;
    @Inject WordRepository wordRepository;
    @Inject UserPreferencesManager prefsManager;

    private List<Word> words = new ArrayList<>();
    private int currentIndex = 0;
    private PlaybackMode playbackMode = PlaybackMode.STUDY_MODE;
    private boolean isPlaying = false;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        setupTtsListener();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;
        String action = intent.getAction();
        if (action == null) return START_NOT_STICKY;

        switch (action) {
            case ACTION_START:
                long vocabBookId = intent.getLongExtra(EXTRA_VOCAB_BOOK_ID, -1);
                String modeStr = intent.getStringExtra(EXTRA_PLAYBACK_MODE);
                if (modeStr != null) playbackMode = PlaybackMode.valueOf(modeStr);
                startPlayback(vocabBookId);
                break;
            case ACTION_PAUSE:
                pausePlayback();
                break;
            case ACTION_RESUME:
                resumePlayback();
                break;
            case ACTION_STOP:
                stopPlayback();
                break;
            case ACTION_NEXT:
                nextWord();
                break;
            case ACTION_PREV:
                prevWord();
                break;
        }
        return START_STICKY;
    }

    private void startPlayback(long vocabBookId) {
        wordRepository.getWordIdsAsync(vocabBookId, null, wordIds -> {
            // Load words synchronously on disk thread via callback
        });
        startForeground(NOTIFICATION_ID, buildNotification("正在播放单词..."));
        isPlaying = true;
        audioSessionManager.requestFocus(new AudioSessionManager.AudioFocusCallback() {
            @Override public void onFocusGained() { if (isPlaying) speakCurrentWord(); }
            @Override public void onFocusLost() { pausePlayback(); }
            @Override public void onFocusLostTransient() { ttsManager.stop(); }
        });
        speakCurrentWord();
    }

    public void setWords(List<Word> wordList) {
        this.words = wordList;
        this.currentIndex = 0;
    }

    private void speakCurrentWord() {
        if (words.isEmpty() || currentIndex >= words.size()) return;
        Word word = words.get(currentIndex);
        PronunciationAccent accent = prefsManager.getAccent();
        Locale chineseLocale = Locale.SIMPLIFIED_CHINESE;
        Locale englishLocale = accent == PronunciationAccent.BRITISH ? Locale.UK : Locale.US;

        switch (playbackMode) {
            case CHINESE_RECALL_ENGLISH:
                ttsManager.speakChinese(word.chineseDefinition, "chinese_" + currentIndex);
                break;
            case ENGLISH_RECALL_CHINESE:
                ttsManager.speakEnglish(word.english, accent, "english_" + currentIndex);
                break;
            case STUDY_MODE:
            default:
                ttsManager.speakEnglish(word.english, accent, "english_" + currentIndex);
                break;
        }
        updateNotification(word.english + " - " + word.chineseDefinition);
    }

    private void setupTtsListener() {
        ttsManager.setProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {}

            @Override
            public void onDone(String utteranceId) {
                if (!isPlaying) return;
                int intervalMs = prefsManager.getAutoAdvanceSeconds() * 1000;
                handler.postDelayed(() -> {
                    if (isPlaying) {
                        currentIndex++;
                        if (currentIndex < words.size()) {
                            speakCurrentWord();
                        } else {
                            currentIndex = 0;
                            stopPlayback();
                        }
                    }
                }, intervalMs);
            }

            @Override
            public void onError(String utteranceId) {
                if (isPlaying) nextWord();
            }
        });
    }

    private void pausePlayback() {
        isPlaying = false;
        ttsManager.stop();
        updateNotification("已暂停");
    }

    private void resumePlayback() {
        isPlaying = true;
        speakCurrentWord();
    }

    private void stopPlayback() {
        isPlaying = false;
        ttsManager.stop();
        audioSessionManager.abandonFocus();
        stopForeground(true);
        stopSelf();
    }

    private void nextWord() {
        ttsManager.stop();
        handler.removeCallbacksAndMessages(null);
        if (!words.isEmpty()) {
            currentIndex = (currentIndex + 1) % words.size();
            if (isPlaying) speakCurrentWord();
        }
    }

    private void prevWord() {
        ttsManager.stop();
        handler.removeCallbacksAndMessages(null);
        if (!words.isEmpty()) {
            currentIndex = (currentIndex - 1 + words.size()) % words.size();
            if (isPlaying) speakCurrentWord();
        }
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, "单词播放", NotificationManager.IMPORTANCE_LOW);
        channel.setDescription("后台播放单词");
        NotificationManager nm = getSystemService(NotificationManager.class);
        nm.createNotificationChannel(channel);
    }

    private Notification buildNotification(String text) {
        Intent mainIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("单词本")
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentIntent(contentIntent)
                .setOngoing(true)
                .build();
    }

    private void updateNotification(String text) {
        NotificationManager nm = getSystemService(NotificationManager.class);
        nm.notify(NOTIFICATION_ID, buildNotification(text));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isPlaying = false;
        ttsManager.stop();
        audioSessionManager.abandonFocus();
        handler.removeCallbacksAndMessages(null);
    }
}
