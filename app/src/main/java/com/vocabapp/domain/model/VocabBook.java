package com.vocabapp.domain.model;

public class VocabBook {
    public final long id;
    public final String title;
    public final String description;
    public final int colorIndex;
    public final boolean isFavorite;
    public final long createdAt;
    public final int wordCount;

    public VocabBook(long id, String title, String description, int colorIndex,
                     boolean isFavorite, long createdAt, int wordCount) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.colorIndex = colorIndex;
        this.isFavorite = isFavorite;
        this.createdAt = createdAt;
        this.wordCount = wordCount;
    }
}
