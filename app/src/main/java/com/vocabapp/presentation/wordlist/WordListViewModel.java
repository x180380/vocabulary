package com.vocabapp.presentation.wordlist;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.vocabapp.data.repository.VocabBookRepository;
import com.vocabapp.data.repository.WordRepository;
import com.vocabapp.domain.enums.SortOrder;
import com.vocabapp.domain.enums.VisibilityMode;
import com.vocabapp.domain.model.VocabBook;
import com.vocabapp.domain.model.Word;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class WordListViewModel extends ViewModel {

    private final WordRepository wordRepository;
    private final VocabBookRepository vocabBookRepository;

    public final MutableLiveData<Long> vocabBookId = new MutableLiveData<>();
    public final MutableLiveData<SortOrder> sortOrder = new MutableLiveData<>(SortOrder.BY_ALPHABET_ASC);
    public final MutableLiveData<Boolean> isBatchMode = new MutableLiveData<>(false);
    public final MutableLiveData<Set<Long>> selectedWordIds = new MutableLiveData<>(new HashSet<>());
    public final MutableLiveData<VisibilityMode> visibilityMode = new MutableLiveData<>(VisibilityMode.SHOW_BOTH);
    public final MutableLiveData<Boolean> showBookmarkedOnly = new MutableLiveData<>(false);

    public final LiveData<List<VocabBook>> allVocabBooks;
    public final LiveData<List<Word>> words;
    public final LiveData<List<Word>> filteredWords;

    @Inject
    public WordListViewModel(WordRepository wordRepository, VocabBookRepository vocabBookRepository) {
        this.wordRepository = wordRepository;
        this.vocabBookRepository = vocabBookRepository;

        allVocabBooks = vocabBookRepository.getAllVocabBooks();

        MediatorLiveData<List<Word>> mediator = new MediatorLiveData<>();
        mediator.addSource(vocabBookId, id -> reloadWords(mediator));
        mediator.addSource(sortOrder, order -> reloadWords(mediator));
        words = mediator;

        MediatorLiveData<List<Word>> filterMediator = new MediatorLiveData<>();
        filterMediator.addSource(words, list -> filterMediator.setValue(applyFilter(list)));
        filterMediator.addSource(showBookmarkedOnly, flag -> filterMediator.setValue(applyFilter(words.getValue())));
        filteredWords = filterMediator;
    }

    private LiveData<List<Word>> currentWordSource;

    private void reloadWords(MediatorLiveData<List<Word>> mediator) {
        Long id = vocabBookId.getValue();
        SortOrder order = sortOrder.getValue();
        if (id == null) return;
        if (currentWordSource != null) mediator.removeSource(currentWordSource);
        currentWordSource = wordRepository.getWordsByVocab(id, order != null ? order : SortOrder.BY_ALPHABET_ASC);
        mediator.addSource(currentWordSource, mediator::setValue);
    }

    public void setVocabBookId(long id) {
        vocabBookId.setValue(id);
    }

    public void setSortOrder(SortOrder order) {
        sortOrder.setValue(order);
    }

    public void toggleBatchMode() {
        Boolean current = isBatchMode.getValue();
        isBatchMode.setValue(current == null ? true : !current);
        if (Boolean.FALSE.equals(isBatchMode.getValue())) {
            selectedWordIds.setValue(new HashSet<>());
        }
    }

    public void toggleWordSelection(long wordId) {
        Set<Long> current = new HashSet<>(selectedWordIds.getValue() != null
                ? selectedWordIds.getValue() : new HashSet<>());
        if (current.contains(wordId)) current.remove(wordId);
        else current.add(wordId);
        selectedWordIds.setValue(current);
    }

    public void selectAll() {
        List<Word> currentWords = filteredWords.getValue();
        if (currentWords == null) return;
        Set<Long> all = new HashSet<>();
        for (Word w : currentWords) all.add(w.id);
        selectedWordIds.setValue(all);
    }

    public void clearSelection() {
        selectedWordIds.setValue(new HashSet<>());
    }

    public void deleteSelected() {
        Set<Long> ids = selectedWordIds.getValue();
        if (ids == null || ids.isEmpty()) return;
        wordRepository.deleteWords(new ArrayList<>(ids));
        clearSelection();
        isBatchMode.setValue(false);
    }

    public void moveSelectedTo(long targetVocabBookId) {
        Set<Long> ids = selectedWordIds.getValue();
        if (ids == null || ids.isEmpty()) return;
        wordRepository.moveWords(new ArrayList<>(ids), targetVocabBookId);
        clearSelection();
        isBatchMode.setValue(false);
    }

    public void setVisibilityMode(VisibilityMode mode) {
        visibilityMode.setValue(mode);
    }

    public void toggleBookmarkFilter() {
        Boolean current = showBookmarkedOnly.getValue();
        showBookmarkedOnly.setValue(current == null || !current);
    }

    private List<Word> applyFilter(List<Word> list) {
        if (list == null) return new ArrayList<>();
        Boolean bookmarkedOnly = showBookmarkedOnly.getValue();
        if (Boolean.TRUE.equals(bookmarkedOnly)) {
            List<Word> filtered = new ArrayList<>();
            for (Word w : list) {
                if (w.isBookmarked) filtered.add(w);
            }
            return filtered;
        }
        return list;
    }
}
