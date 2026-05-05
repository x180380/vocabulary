package com.vocabapp.presentation.addvocab;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.vocabapp.data.repository.VocabBookRepository;
import com.vocabapp.domain.model.VocabBook;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AddVocabViewModel extends ViewModel {

    private final VocabBookRepository repository;
    public final LiveData<List<VocabBook>> allVocabBooks;

    @Inject
    public AddVocabViewModel(VocabBookRepository repository) {
        this.repository = repository;
        this.allVocabBooks = repository.getAllVocabBooks();
    }

    public void createVocabBook(String title, int colorIndex) {
        repository.createVocabBook(title, "", colorIndex, true);
    }
}
