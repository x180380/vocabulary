package com.vocabapp.domain.model;

public class VocabBook {
    public final long id;
    public final String bookName;
    public final String assetFile;
    public final int wordCount;
    public final int colorIndex;

    public VocabBook(long id, String bookName, String assetFile, int wordCount, int colorIndex) {
        this.id = id;
        this.bookName = bookName;
        this.assetFile = assetFile;
        this.wordCount = wordCount;
        this.colorIndex = colorIndex;
    }
}
