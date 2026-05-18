package com.vocabapp.data.local.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "bookmarks")
public class BookmarkEntity {

    @PrimaryKey
    @ColumnInfo(name = "word_id")
    public long wordId;

    public BookmarkEntity() {}

    public BookmarkEntity(long wordId) {
        this.wordId = wordId;
    }
}
