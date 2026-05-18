package com.vocabapp.domain.model;

import java.util.List;

public class Word {
    public final long id;
    public final long vocabBookId;
    public final String english;
    public final String chineseDefinition;
    public final List<Definition> definitions;
    public final Phonetics phonetics;
    public final List<Example> examples;
    public final boolean isBookmarked;

    public Word(long id, long vocabBookId, String english, String chineseDefinition,
                List<Definition> definitions, Phonetics phonetics, List<Example> examples) {
        this(id, vocabBookId, english, chineseDefinition, definitions, phonetics, examples, false);
    }

    public Word(long id, long vocabBookId, String english, String chineseDefinition,
                List<Definition> definitions, Phonetics phonetics, List<Example> examples,
                boolean isBookmarked) {
        this.id = id;
        this.vocabBookId = vocabBookId;
        this.english = english;
        this.chineseDefinition = chineseDefinition;
        this.definitions = definitions;
        this.phonetics = phonetics;
        this.examples = examples;
        this.isBookmarked = isBookmarked;
    }
}
