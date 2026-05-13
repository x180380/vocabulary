package com.vocabapp.data.local.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

public class VocabBookWithCount {

    @Embedded
    public VocabBookEntity book;

    @ColumnInfo(name = "word_count")
    public int wordCount;
}
