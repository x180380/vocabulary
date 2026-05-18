package com.vocabapp.presentation.addvocab;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.vocabapp.data.repository.VocabBookRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AddVocabViewModel extends ViewModel {

    private final VocabBookRepository repository;
    public final MutableLiveData<Boolean> createSuccess = new MutableLiveData<>();

    @Inject
    public AddVocabViewModel(VocabBookRepository repository) {
        this.repository = repository;
    }

    public void createVocabBook(String name) {
        String trimmed = name == null ? "" : name.trim();
        if (trimmed.isEmpty()) return;
        repository.createVocabBook(trimmed, () -> createSuccess.setValue(true));
    }
}
