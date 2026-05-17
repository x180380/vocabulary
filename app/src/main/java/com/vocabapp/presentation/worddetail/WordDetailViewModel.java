package com.vocabapp.presentation.worddetail;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.vocabapp.data.local.preferences.UserPreferencesManager;
import com.vocabapp.data.repository.WordRepository;
import com.vocabapp.domain.enums.PlaybackMode;
import com.vocabapp.domain.enums.PronunciationAccent;
import com.vocabapp.domain.enums.SortOrder;
import com.vocabapp.domain.enums.VisibilityMode;
import com.vocabapp.domain.model.Word;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class WordDetailViewModel extends ViewModel {

    private final WordRepository wordRepository;
    private final UserPreferencesManager prefsManager;

    public final MutableLiveData<Long> vocabBookIdLive = new MutableLiveData<>();
    public final MediatorLiveData<List<Word>> allWords = new MediatorLiveData<>();

    public final MutableLiveData<Integer> currentIndex = new MutableLiveData<>(0);
    public final MutableLiveData<VisibilityMode> visibilityMode;
    public final MutableLiveData<Boolean> isAutoAdvance = new MutableLiveData<>(false);
    public final MutableLiveData<Boolean> isEnglishRevealed = new MutableLiveData<>(false);
    public final MutableLiveData<PlaybackMode> playbackMode = new MutableLiveData<>(null);

    private long startWordId = -1;
    private int groupCount = -1;
    private long initialWordId = -1;

    @Inject
    public WordDetailViewModel(WordRepository wordRepository, UserPreferencesManager prefsManager) {
        this.wordRepository = wordRepository;
        this.prefsManager = prefsManager;
        this.visibilityMode = new MutableLiveData<>(prefsManager.getVisibilityMode());

        LiveData<List<Word>> rawWords = Transformations.switchMap(vocabBookIdLive,
                id -> id != null && id >= 0
                        ? wordRepository.getWordsByVocab(id, SortOrder.BY_ALPHABET_ASC)
                        : new MutableLiveData<>(null));

        allWords.addSource(rawWords, words -> allWords.setValue(applyGroupFilter(words)));
    }

    public void init(long vocabBookId, long startWordId) {
        init(vocabBookId, startWordId, -1);
    }

    public void init(long vocabBookId, long startWordId, int groupCount) {
        init(vocabBookId, startWordId, groupCount, -1);
    }

    public void init(long vocabBookId, long startWordId, int groupCount, long initialWordId) {
        this.startWordId = startWordId;
        this.groupCount = groupCount;
        this.initialWordId = initialWordId;
        vocabBookIdLive.setValue(vocabBookId);
    }

    private List<Word> applyGroupFilter(List<Word> words) {
        if (words == null || groupCount <= 0 || startWordId < 0) return words;
        int startIdx = 0;
        for (int i = 0; i < words.size(); i++) {
            if (words.get(i).id == startWordId) { startIdx = i; break; }
        }
        return new ArrayList<>(words.subList(startIdx, Math.min(startIdx + groupCount, words.size())));
    }

    public int findStartIndex(List<Word> words) {
        if (words == null) return 0;
        long lookupId = (groupCount > 0 && initialWordId >= 0) ? initialWordId : startWordId;
        if (lookupId < 0) return 0;
        for (int i = 0; i < words.size(); i++) {
            if (words.get(i).id == lookupId) return i;
        }
        return 0;
    }

    public void onPageChanged(int newIndex) {
        currentIndex.setValue(newIndex);
        isEnglishRevealed.setValue(false);
    }

    public void revealEnglish() {
        isEnglishRevealed.setValue(true);
    }

    private PlaybackMode lastPlaybackMode = null;

    public void setPlaybackMode(PlaybackMode mode) {
        if (mode != null) lastPlaybackMode = mode;
        playbackMode.setValue(mode);
    }

    public PlaybackMode getLastPlaybackMode() {
        return lastPlaybackMode;
    }

    public void setVisibilityMode(VisibilityMode mode) {
        visibilityMode.setValue(mode);
    }

    public void toggleAutoAdvance() {
        Boolean current = isAutoAdvance.getValue();
        isAutoAdvance.setValue(current == null ? true : !current);
    }

    public PronunciationAccent getAccent() {
        return prefsManager.getAccent();
    }

    public int getAutoAdvanceSeconds() {
        return prefsManager.getAutoAdvanceSeconds();
    }

    public int getWordCount() {
        List<Word> words = allWords.getValue();
        return words != null ? words.size() : 0;
    }

    static int computeNextIndex(int current, int total) {
        if (total == 0) return 0;
        return (current + 1) % total;
    }
}
