package com.vocabapp.di;

import android.content.Context;

import com.google.gson.Gson;
import com.vocabapp.data.local.database.daos.BookWordDao;
import com.vocabapp.data.local.database.daos.VocabBookDao;
import com.vocabapp.data.local.database.daos.WordDefinitionDao;
import com.vocabapp.data.local.preferences.UserPreferencesManager;
import com.vocabapp.data.local.seed.DatabaseSeeder;
import com.vocabapp.presentation.common.AppExecutors;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    @Provides
    @Singleton
    public Gson provideGson() {
        return new Gson();
    }

    @Provides
    @Singleton
    public AppExecutors provideAppExecutors() {
        return new AppExecutors();
    }

    @Provides
    @Singleton
    public UserPreferencesManager provideUserPreferencesManager(@ApplicationContext Context context) {
        return new UserPreferencesManager(context);
    }

    @Provides
    @Singleton
    public DatabaseSeeder provideDatabaseSeeder(@ApplicationContext Context context,
                                                VocabBookDao vocabBookDao,
                                                WordDefinitionDao wordDefinitionDao,
                                                BookWordDao bookWordDao) {
        return new DatabaseSeeder(context, vocabBookDao, wordDefinitionDao, bookWordDao);
    }
}
