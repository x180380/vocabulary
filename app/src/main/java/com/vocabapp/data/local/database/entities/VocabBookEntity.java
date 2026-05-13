package com.vocabapp.data.local.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "vocab_books")
public class VocabBookEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "book_id")
    public long bookId;

    @ColumnInfo(name = "book_name")
    public String bookName;

    @ColumnInfo(name = "asset_file")
    public String assetFile;

    public VocabBookEntity() {}

    public VocabBookEntity(String bookName, String assetFile) {
        this.bookName = bookName;
        this.assetFile = assetFile;
    }
}
