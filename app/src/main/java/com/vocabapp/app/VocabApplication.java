package com.vocabapp.app;

import android.app.Application;

import com.vocabapp.data.local.seed.DatabaseSeeder;
import com.vocabapp.presentation.common.AppExecutors;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class VocabApplication extends Application {

    @Inject
    DatabaseSeeder databaseSeeder;

    @Inject
    AppExecutors appExecutors;

    @Override
    public void onCreate() {
        super.onCreate();
        appExecutors.diskIO().execute(() -> databaseSeeder.seedIfNeeded());
    }
}
