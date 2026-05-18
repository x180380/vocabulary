package com.vocabapp.data.local.seed;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.vocabapp.data.local.database.daos.BookWordDao;
import com.vocabapp.data.local.database.daos.VocabBookDao;
import com.vocabapp.data.local.database.daos.WordDefinitionDao;
import com.vocabapp.data.local.database.entities.BookWordEntity;
import com.vocabapp.data.local.database.entities.VocabBookEntity;
import com.vocabapp.data.local.database.entities.WordDefinitionEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dagger.hilt.android.qualifiers.ApplicationContext;

public class DatabaseSeeder {

    private static final String PREF_NAME = "vocab_app_prefs";
    private static final String KEY_DB_SEEDED = "seed_v4";
    private static final int BATCH_SIZE = 500;
    private static final Pattern POS_PATTERN = Pattern.compile("^([a-z]{1,7})\\.");

    private final Context context;
    private final VocabBookDao vocabBookDao;
    private final WordDefinitionDao wordDefinitionDao;
    private final BookWordDao bookWordDao;
    private final Gson gson;

    public DatabaseSeeder(@ApplicationContext Context context,
                          VocabBookDao vocabBookDao,
                          WordDefinitionDao wordDefinitionDao,
                          BookWordDao bookWordDao) {
        this.context = context.getApplicationContext();
        this.vocabBookDao = vocabBookDao;
        this.wordDefinitionDao = wordDefinitionDao;
        this.bookWordDao = bookWordDao;
        this.gson = new Gson();
    }

    public void seedIfNeeded() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (prefs.getBoolean(KEY_DB_SEEDED, false)) {
            return;
        }

        Set<String> zhongkaoWords = readWordList("seed/zhongkao_words.txt");
        Set<String> gaokaoWords = readWordList("seed/gaokao_words.txt");
        Set<String> cet4Words = readWordList("seed/cet4_words.txt");

        Set<String> allNeeded = new HashSet<>();
        allNeeded.addAll(zhongkaoWords);
        allNeeded.addAll(gaokaoWords);
        allNeeded.addAll(cet4Words);

        seedWordDefinitions(allNeeded);

        long zhongkaoId = vocabBookDao.insertVocabBook(
                new VocabBookEntity("中考核心词汇", "seed/zhongkao_words.txt"));
        long gaokaoId = vocabBookDao.insertVocabBook(
                new VocabBookEntity("高考必备词汇", "seed/gaokao_words.txt"));
        long cet4Id = vocabBookDao.insertVocabBook(
                new VocabBookEntity("CET-4 核心词汇", "seed/cet4_words.txt"));

        if (zhongkaoId > 0) seedBookWords(zhongkaoId, zhongkaoWords);
        if (gaokaoId > 0) seedBookWords(gaokaoId, gaokaoWords);
        if (cet4Id > 0) seedBookWords(cet4Id, cet4Words);

        prefs.edit().putBoolean(KEY_DB_SEEDED, true).apply();
    }

    private Set<String> readWordList(String assetPath) {
        Set<String> words = new HashSet<>();
        try (InputStream is = context.getAssets().open(assetPath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim().toLowerCase();
                if (!line.isEmpty()) words.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return words;
    }

    private void seedWordDefinitions(Set<String> neededWords) {
        List<WordDefinitionEntity> batch = new ArrayList<>(BATCH_SIZE);
        try (InputStream is = context.getAssets().open("seed/vocabulary.csv");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            reader.readLine(); // skip header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = parseCsvRow(line, 6);
                if (fields[0].isEmpty()) continue;
                String word = fields[0].trim().toLowerCase();
                if (!neededWords.contains(word)) continue;

                WordDefinitionEntity entity = new WordDefinitionEntity();
                entity.word = word;
                entity.phoneticsBritish = fields[1].isEmpty() ? null : fields[1];
                entity.phoneticsAmerican = fields[2].isEmpty() ? null : fields[2];
                entity.definitions = parseDefinitionsToJson(fields[3]);
                entity.exampleEnglish = fields[4].isEmpty() ? null : fields[4];
                entity.exampleChinese = fields[5].isEmpty() ? null : fields[5];
                batch.add(entity);

                if (batch.size() >= BATCH_SIZE) {
                    wordDefinitionDao.insertAll(batch);
                    batch.clear();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!batch.isEmpty()) wordDefinitionDao.insertAll(batch);
    }

    private void seedBookWords(long bookId, Set<String> words) {
        List<BookWordEntity> batch = new ArrayList<>(BATCH_SIZE);
        for (String word : words) {
            batch.add(new BookWordEntity(bookId, word));
            if (batch.size() >= BATCH_SIZE) {
                bookWordDao.insertAll(batch);
                batch.clear();
            }
        }
        if (!batch.isEmpty()) bookWordDao.insertAll(batch);
    }

    // Parses a single CSV row respecting double-quoted fields.
    private String[] parseCsvRow(String line, int numFields) {
        String[] fields = new String[numFields];
        Arrays.fill(fields, "");
        int fieldIdx = 0;
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < line.length() && fieldIdx < numFields; i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    fields[fieldIdx++] = current.toString();
                    current = new StringBuilder();
                } else {
                    current.append(c);
                }
            }
        }
        if (fieldIdx < numFields) {
            fields[fieldIdx] = current.toString();
        }
        return fields;
    }

    // Splits on literal \n (backslash + n) and extracts part-of-speech from each segment.
    private String parseDefinitionsToJson(String rawDefs) {
        if (rawDefs == null || rawDefs.isEmpty()) return "[]";
        String[] parts = rawDefs.split("\\\\n");
        List<DefinitionDto> defs = new ArrayList<>();
        for (String part : parts) {
            part = part.trim();
            if (part.isEmpty()) continue;
            Matcher m = POS_PATTERN.matcher(part);
            String pos = "";
            String meaning;
            if (m.find()) {
                pos = m.group(0);
                meaning = part.substring(m.end()).trim();
            } else {
                meaning = part;
            }
            if (!meaning.isEmpty()) {
                defs.add(new DefinitionDto(pos, meaning));
            }
        }
        return gson.toJson(defs);
    }

    private static class DefinitionDto {
        final String partOfSpeech;
        final String meaning;

        DefinitionDto(String partOfSpeech, String meaning) {
            this.partOfSpeech = partOfSpeech;
            this.meaning = meaning;
        }
    }
}
