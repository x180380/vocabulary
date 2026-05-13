package com.vocabapp.data.local.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "book_words",
    foreignKeys = @ForeignKey(
        entity = VocabBookEntity.class,
        parentColumns = "book_id",
        childColumns = "book_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {
        @Index(value = {"book_id", "word"}, unique = true),
        @Index("word")
    }
)
public class BookWordEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "book_id")
    public long bookId;

    @NonNull
    @ColumnInfo(name = "word")
    public String word = "";

    public BookWordEntity() {}

    public BookWordEntity(long bookId, @NonNull String word) {
        this.bookId = bookId;
        this.word = word;
    }
}
