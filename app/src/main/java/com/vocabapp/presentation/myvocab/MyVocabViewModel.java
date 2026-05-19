package com.vocabapp.presentation.myvocab;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.vocabapp.data.repository.VocabBookRepository;
import com.vocabapp.domain.model.VocabBook;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MyVocabViewModel extends ViewModel {

    private final VocabBookRepository repository;
    public final LiveData<List<VocabBook>> myBooks;
    public final MutableLiveData<Boolean> isEditMode = new MutableLiveData<>(false);

    @Inject
    public MyVocabViewModel(VocabBookRepository repository) {
        this.repository = repository;
        this.myBooks = repository.getMyVocabBooks();
    }

    public void toggleEditMode() {
        Boolean current = isEditMode.getValue();
        isEditMode.setValue(current == null ? true : !current);
    }

    public void removeFromMyVocab(long bookId) {
        repository.removeFromMyVocab(bookId);
    }
}
