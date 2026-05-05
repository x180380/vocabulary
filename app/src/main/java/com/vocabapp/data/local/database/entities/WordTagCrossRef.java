package com.vocabapp.data.local.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
    tableName = "word_tag_cross_ref",
    primaryKeys = {"word_id", "tag_id"},
    foreignKeys = {
        @ForeignKey(entity = WordEntity.class, parentColumns = "id", childColumns = "word_id", onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = TagEntity.class, parentColumns = "id", childColumns = "tag_id", onDelete = ForeignKey.CASCADE)
    },
    indices = {@Index("tag_id")}
)
public class WordTagCrossRef {

    @ColumnInfo(name = "word_id")
    public long wordId;

    @ColumnInfo(name = "tag_id")
    public long tagId;

    public WordTagCrossRef(long wordId, long tagId) {
        this.wordId = wordId;
        this.tagId = tagId;
    }
}
