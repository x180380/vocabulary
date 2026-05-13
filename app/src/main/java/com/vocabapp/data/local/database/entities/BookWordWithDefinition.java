package com.vocabapp.data.local.database.entities;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class BookWordWithDefinition {

    @Embedded
    public BookWordEntity bookWord;

    @Relation(
        parentColumn = "word",
        entityColumn = "word"
    )
    public List<WordDefinitionEntity> definitions;
}
