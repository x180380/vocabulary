package com.vocabapp.presentation.vocablist;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.vocabapp.data.repository.VocabBookRepository;
import com.vocabapp.domain.model.VocabBook;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class VocabListViewModel extends ViewModel {

    private final VocabBookRepository repository;
    public final LiveData<List<VocabBook>> favoriteBooks;
    public final MutableLiveData<Boolean> isEditMode = new MutableLiveData<>(false);

    @Inject
    public VocabListViewModel(VocabBookRepository repository) {
        this.repository = repository;
        this.favoriteBooks = repository.getFavoriteVocabBooks();
    }

    public void toggleFavorite(long vocabBookId, boolean currentFavorite) {
        repository.setFavorite(vocabBookId, !currentFavorite);
    }

    public void toggleEditMode() {
        Boolean current = isEditMode.getValue();
        isEditMode.setValue(current == null ? true : !current);
    }

    public void createVocabBook(String title, int colorIndex) {
        repository.createVocabBook(title, "", colorIndex, true);
    }

    public void deleteVocabBook(long id) {
        repository.deleteVocabBook(id);
    }
}
