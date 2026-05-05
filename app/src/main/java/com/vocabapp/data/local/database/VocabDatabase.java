package com.vocabapp.data.local.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.vocabapp.data.local.database.daos.TagDao;
import com.vocabapp.data.local.database.daos.VocabBookDao;
import com.vocabapp.data.local.database.daos.WordDao;
import com.vocabapp.data.local.database.entities.TagEntity;
import com.vocabapp.data.local.database.entities.VocabBookEntity;
import com.vocabapp.data.local.database.entities.WordEntity;
import com.vocabapp.data.local.database.entities.WordTagCrossRef;

@Database(
    entities = {
        VocabBookEntity.class,
        WordEntity.class,
        TagEntity.class,
        WordTagCrossRef.class
    },
    version = 1,
    exportSchema = false
)
public abstract class VocabDatabase extends RoomDatabase {

    public abstract VocabBookDao vocabBookDao();
    public abstract WordDao wordDao();
    public abstract TagDao tagDao();
}
