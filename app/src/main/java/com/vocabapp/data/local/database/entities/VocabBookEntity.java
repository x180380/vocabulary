package com.vocabapp.data.local.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "vocab_books")
public class VocabBookEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "color_index")
    public int colorIndex;

    @ColumnInfo(name = "is_favorite")
    public boolean isFavorite;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "word_count")
    public int wordCount;

    public VocabBookEntity() {}

    public VocabBookEntity(String title, String description, int colorIndex, boolean isFavorite) {
        this.title = title;
        this.description = description;
        this.colorIndex = colorIndex;
        this.isFavorite = isFavorite;
        this.createdAt = System.currentTimeMillis();
        this.wordCount = 0;
    }
}
