package com.vocabapp.presentation.playbackmode;

import android.content.Context;

import androidx.lifecycle.ViewModel;

import com.vocabapp.audio.AudioService;
import com.vocabapp.audio.AudioServiceConnection;
import com.vocabapp.data.repository.WordRepository;
import com.vocabapp.domain.enums.PlaybackMode;
import com.vocabapp.domain.enums.SortOrder;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;

@HiltViewModel
public class PlaybackModeViewModel extends ViewModel {

    private final WordRepository wordRepository;
    private final Context context;
    private long vocabBookId = -1;

    @Inject
    public PlaybackModeViewModel(WordRepository wordRepository, @ApplicationContext Context context) {
        this.wordRepository = wordRepository;
        this.context = context;
    }

    public void setVocabBookId(long id) {
        this.vocabBookId = id;
    }

    public void startPlayback(PlaybackMode mode) {
        wordRepository.getWordIdsAsync(vocabBookId, SortOrder.BY_TIME_DESC, wordIds -> {
            if (wordIds == null || wordIds.isEmpty()) return;
            android.content.Intent intent = new android.content.Intent(context, AudioService.class);
            intent.setAction(AudioService.ACTION_START);
            intent.putExtra(AudioService.EXTRA_VOCAB_BOOK_ID, vocabBookId);
            intent.putExtra(AudioService.EXTRA_PLAYBACK_MODE, mode.name());
            context.startForegroundService(intent);
        });
    }
}
