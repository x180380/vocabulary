package com.vocabapp.presentation.worddetail;

import androidx.lifecycle.LiveData;
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

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class WordDetailViewModel extends ViewModel {

    private final WordRepository wordRepository;
    private final UserPreferencesManager prefsManager;

    // All words for the current vocab (drives ViewPager2 count)
    public final MutableLiveData<Long> vocabBookIdLive = new MutableLiveData<>();
    public final LiveData<List<Word>> allWords;

    public final MutableLiveData<Integer> currentIndex = new MutableLiveData<>(0);
    public final MutableLiveData<VisibilityMode> visibilityMode;
    public final MutableLiveData<Boolean> isBookmarked = new MutableLiveData<>(false);
    public final MutableLiveData<Boolean> isAutoAdvance = new MutableLiveData<>(false);
    public final MutableLiveData<Boolean> isEnglishRevealed = new MutableLiveData<>(false);
    public final MutableLiveData<PlaybackMode> playbackMode = new MutableLiveData<>(null);

    private long startWordId = -1;

    @Inject
    public WordDetailViewModel(WordRepository wordRepository, UserPreferencesManager prefsManager) {
        this.wordRepository = wordRepository;
        this.prefsManager = prefsManager;
        this.visibilityMode = new MutableLiveData<>(prefsManager.getVisibilityMode());

        allWords = Transformations.switchMap(vocabBookIdLive,
                id -> id != null && id >= 0
                        ? wordRepository.getWordsByVocab(id, SortOrder.BY_TIME_DESC)
                        : new MutableLiveData<>(null));
    }

    public void init(long vocabBookId, long startWordId) {
        this.startWordId = startWordId;
        vocabBookIdLive.setValue(vocabBookId);
    }

    /** Called once words load — finds start word position */
    public int findStartIndex(List<Word> words) {
        if (words == null || startWordId < 0) return 0;
        for (int i = 0; i < words.size(); i++) {
            if (words.get(i).id == startWordId) return i;
        }
        return 0;
    }

    public void onPageChanged(int newIndex) {
        currentIndex.setValue(newIndex);
        isEnglishRevealed.setValue(false);
        // Sync bookmark state with new word
        List<Word> words = allWords.getValue();
        if (words != null && newIndex < words.size()) {
            isBookmarked.setValue(words.get(newIndex).isBookmarked);
        }
    }

    public void toggleBookmark() {
        List<Word> words = allWords.getValue();
        Integer idx = currentIndex.getValue();
        if (words == null || idx == null || idx >= words.size()) return;
        Word word = words.get(idx);
        boolean newState = !word.isBookmarked;
        wordRepository.setBookmark(word.id, newState);
        isBookmarked.setValue(newState);
    }

    public void revealEnglish() {
        isEnglishRevealed.setValue(true);
    }

    public void setPlaybackMode(PlaybackMode mode) {
        playbackMode.setValue(mode);
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
}
