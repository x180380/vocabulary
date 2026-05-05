package com.vocabapp.data.local.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "words",
    foreignKeys = @ForeignKey(
        entity = VocabBookEntity.class,
        parentColumns = "id",
        childColumns = "vocab_book_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("vocab_book_id")}
)
public class WordEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "vocab_book_id")
    public long vocabBookId;

    @ColumnInfo(name = "english")
    public String english;

    @ColumnInfo(name = "chinese_definition")
    public String chineseDefinition;

    // JSON array of Definition objects: [{"partOfSpeech":"n.","meaning":"..."}]
    @ColumnInfo(name = "definitions")
    public String definitions;

    // JSON: {"british":"/...","american":"/..."}
    @ColumnInfo(name = "phonetics")
    public String phonetics;

    // JSON array of Example objects: [{"english":"...","chinese":"..."}]
    @ColumnInfo(name = "examples")
    public String examples;

    @ColumnInfo(name = "is_bookmarked")
    public boolean isBookmarked;

    @ColumnInfo(name = "added_at")
    public long addedAt;

    @ColumnInfo(name = "last_reviewed_at")
    public long lastReviewedAt;

    public WordEntity() {}
}
