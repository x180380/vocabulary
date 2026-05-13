package com.vocabapp.di;

import android.content.Context;

import androidx.room.Room;

import com.vocabapp.data.local.database.VocabDatabase;
import com.vocabapp.data.local.database.daos.BookWordDao;
import com.vocabapp.data.local.database.daos.VocabBookDao;
import com.vocabapp.data.local.database.daos.WordDefinitionDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    @Provides
    @Singleton
    public VocabDatabase provideDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(context, VocabDatabase.class, "vocab_database")
                .fallbackToDestructiveMigration()
                .build();
    }

    @Provides
    @Singleton
    public VocabBookDao provideVocabBookDao(VocabDatabase db) {
        return db.vocabBookDao();
    }

    @Provides
    @Singleton
    public WordDefinitionDao provideWordDefinitionDao(VocabDatabase db) {
        return db.wordDefinitionDao();
    }

    @Provides
    @Singleton
    public BookWordDao provideBookWordDao(VocabDatabase db) {
        return db.bookWordDao();
    }
}
