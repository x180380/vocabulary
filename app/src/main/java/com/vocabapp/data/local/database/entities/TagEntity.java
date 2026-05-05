package com.vocabapp.data.local.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tags")
public class TagEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "color_hex")
    public String colorHex;

    public TagEntity() {}

    public TagEntity(String name, String colorHex) {
        this.name = name;
        this.colorHex = colorHex;
    }
}
