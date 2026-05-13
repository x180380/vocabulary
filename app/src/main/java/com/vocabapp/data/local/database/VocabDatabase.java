package com.vocabapp.data.local.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.vocabapp.data.local.database.daos.BookWordDao;
import com.vocabapp.data.local.database.daos.VocabBookDao;
import com.vocabapp.data.local.database.daos.WordDefinitionDao;
import com.vocabapp.data.local.database.entities.BookWordEntity;
import com.vocabapp.data.local.database.entities.VocabBookEntity;
import com.vocabapp.data.local.database.entities.WordDefinitionEntity;

@Database(
    entities = {
        VocabBookEntity.class,
        WordDefinitionEntity.class,
        BookWordEntity.class
    },
    version = 2,
    exportSchema = false
)
public abstract class VocabDatabase extends RoomDatabase {

    public abstract VocabBookDao vocabBookDao();
    public abstract WordDefinitionDao wordDefinitionDao();
    public abstract BookWordDao bookWordDao();
}
