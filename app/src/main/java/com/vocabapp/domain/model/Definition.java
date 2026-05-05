package com.vocabapp.domain.model;

public class Definition {
    public final String partOfSpeech;
    public final String meaning;

    public Definition(String partOfSpeech, String meaning) {
        this.partOfSpeech = partOfSpeech;
        this.meaning = meaning;
    }
}
