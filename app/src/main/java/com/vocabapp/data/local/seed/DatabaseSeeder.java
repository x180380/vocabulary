package com.vocabapp.data.local.seed;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vocabapp.data.local.database.daos.VocabBookDao;
import com.vocabapp.data.local.database.daos.WordDao;
import com.vocabapp.data.local.database.entities.VocabBookEntity;
import com.vocabapp.data.local.database.entities.WordEntity;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.qualifiers.ApplicationContext;

public class DatabaseSeeder {

    private static final String PREF_NAME = "vocab_app_prefs";
    private static final String KEY_DB_SEEDED = "db_seeded";

    private final Context context;
    private final VocabBookDao vocabBookDao;
    private final WordDao wordDao;
    private final Gson gson;

    public DatabaseSeeder(@ApplicationContext Context context, VocabBookDao vocabBookDao, WordDao wordDao) {
        this.context = context.getApplicationContext();
        this.vocabBookDao = vocabBookDao;
        this.wordDao = wordDao;
        this.gson = new Gson();
    }

    public void seedIfNeeded() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (prefs.getBoolean(KEY_DB_SEEDED, false)) {
            return;
        }

        seedVocabBook("中考必备词汇", "中考核心词汇", 1, "seed/zhongkao_words.json");
        seedVocabBook("高考核心词汇", "高考必备词汇", 3, "seed/gaokao_words.json");
        seedVocabBook("四级词汇", "CET-4 核心词汇", 5, "seed/cet4_words.json");

        prefs.edit().putBoolean(KEY_DB_SEEDED, true).apply();
    }

    private void seedVocabBook(String title, String description, int colorIndex, String assetPath) {
        try {
            String json = readAsset(assetPath);
            Type listType = new TypeToken<List<SeedWord>>() {}.getType();
            List<SeedWord> seedWords = gson.fromJson(json, listType);

            VocabBookEntity book = new VocabBookEntity(title, description, colorIndex, true);
            long bookId = vocabBookDao.insertVocabBook(book);

            List<WordEntity> entities = new ArrayList<>();
            long now = System.currentTimeMillis();
            for (int i = 0; i < seedWords.size(); i++) {
                SeedWord sw = seedWords.get(i);
                WordEntity entity = new WordEntity();
                entity.vocabBookId = bookId;
                entity.english = sw.english;
                entity.chineseDefinition = sw.chineseDefinition;
                entity.definitions = gson.toJson(sw.definitions);
                entity.phonetics = gson.toJson(sw.phonetics);
                entity.examples = gson.toJson(sw.examples);
                entity.isBookmarked = false;
                entity.addedAt = now - (long) i * 1000;
                entity.lastReviewedAt = 0;
                entities.add(entity);
            }
            wordDao.insertWords(entities);
            vocabBookDao.refreshWordCount(bookId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readAsset(String path) throws IOException {
        InputStream is = context.getAssets().open(path);
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();
        return new String(buffer, StandardCharsets.UTF_8);
    }

    // Inner DTOs matching the JSON structure
    private static class SeedWord {
        String english;
        String chineseDefinition;
        List<SeedDefinition> definitions;
        SeedPhonetics phonetics;
        List<SeedExample> examples;
    }

    private static class SeedDefinition {
        String partOfSpeech;
        String meaning;
    }

    private static class SeedPhonetics {
        String british;
        String american;
    }

    private static class SeedExample {
        String english;
        String chinese;
    }
}
