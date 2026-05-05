package com.vocabapp.data.local.database.entities;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import java.util.List;

public class WordWithTags {

    @Embedded
    public WordEntity word;

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = @Junction(
            value = WordTagCrossRef.class,
            parentColumn = "word_id",
            entityColumn = "tag_id"
        )
    )
    public List<TagEntity> tags;
}
