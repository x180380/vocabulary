package com.vocabapp.data.local.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "word_definitions")
public class WordDefinitionEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "word")
    public String word = "";

    @ColumnInfo(name = "phonetics_british")
    public String phoneticsBritish;

    @ColumnInfo(name = "phonetics_american")
    public String phoneticsAmerican;

    // JSON array: [{"partOfSpeech":"n.","meaning":"..."}]
    @ColumnInfo(name = "definitions")
    public String definitions;

    @ColumnInfo(name = "example_english")
    public String exampleEnglish;

    @ColumnInfo(name = "example_chinese")
    public String exampleChinese;

    public WordDefinitionEntity() {}
}
