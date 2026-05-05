package com.vocabapp.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vocabapp.data.local.database.daos.VocabBookDao;
import com.vocabapp.data.local.database.daos.WordDao;
import com.vocabapp.data.local.database.entities.TagEntity;
import com.vocabapp.data.local.database.entities.WordEntity;
import com.vocabapp.data.local.database.entities.WordWithTags;
import com.vocabapp.domain.enums.SortOrder;
import com.vocabapp.domain.model.Definition;
import com.vocabapp.domain.model.Example;
import com.vocabapp.domain.model.Phonetics;
import com.vocabapp.domain.model.Tag;
import com.vocabapp.domain.model.Word;
import com.vocabapp.presentation.common.AppExecutors;

import androidx.annotation.Nullable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class WordRepository {

    private final WordDao wordDao;
    private final VocabBookDao vocabBookDao;
    private final AppExecutors executors;
    private final Gson gson;

    @Inject
    public WordRepository(WordDao wordDao, VocabBookDao vocabBookDao,
                          AppExecutors executors, Gson gson) {
        this.wordDao = wordDao;
        this.vocabBookDao = vocabBookDao;
        this.executors = executors;
        this.gson = gson;
    }

    public LiveData<List<Word>> getWordsByVocab(long vocabBookId, SortOrder sortOrder) {
        LiveData<List<WordWithTags>> source;
        switch (sortOrder) {
            case BY_TIME_ASC:
                source = wordDao.getWordsByVocabTimeAsc(vocabBookId);
                break;
            case BY_ALPHABET_ASC:
                source = wordDao.getWordsByVocabAlpha(vocabBookId);
                break;
            default:
                source = wordDao.getWordsByVocabTimeDesc(vocabBookId);
        }
        return Transformations.map(source, this::toWords);
    }

    public LiveData<List<Word>> getWordsByTag(long vocabBookId, List<Long> tagIds) {
        return Transformations.map(wordDao.getWordsByTag(vocabBookId, tagIds), this::toWords);
    }

    public LiveData<Word> getWordById(long wordId) {
        return Transformations.map(wordDao.getWordWithTagsById(wordId), this::toWord);
    }

    public void deleteWords(List<Long> wordIds) {
        executors.diskIO().execute(() -> {
            if (wordIds == null || wordIds.isEmpty()) return;
            // Get unique vocabBookIds affected before deleting
            List<Long> vocabBookIds = new ArrayList<>();
            for (Long wordId : wordIds) {
                WordEntity entity = wordDao.getWordByIdSync(wordId);
                if (entity != null && !vocabBookIds.contains(entity.vocabBookId)) {
                    vocabBookIds.add(entity.vocabBookId);
                }
            }
            wordDao.deleteWordsByIds(wordIds);
            for (Long bookId : vocabBookIds) {
                vocabBookDao.refreshWordCount(bookId);
            }
        });
    }

    public void moveWords(List<Long> wordIds, long targetVocabBookId) {
        executors.diskIO().execute(() -> {
            if (wordIds == null || wordIds.isEmpty()) return;
            List<Long> sourceBookIds = new ArrayList<>();
            for (Long wordId : wordIds) {
                WordEntity entity = wordDao.getWordByIdSync(wordId);
                if (entity != null && !sourceBookIds.contains(entity.vocabBookId)) {
                    sourceBookIds.add(entity.vocabBookId);
                }
            }
            wordDao.moveWordsToVocab(wordIds, targetVocabBookId);
            for (Long bookId : sourceBookIds) vocabBookDao.refreshWordCount(bookId);
            vocabBookDao.refreshWordCount(targetVocabBookId);
        });
    }

    public void setBookmark(long wordId, boolean isBookmarked) {
        executors.diskIO().execute(() -> wordDao.setBookmark(wordId, isBookmarked));
    }

    public void getWordIdsAsync(long vocabBookId, @Nullable SortOrder sortOrder, WordIdsCallback callback) {
        executors.diskIO().execute(() -> {
            List<Long> ids = sortOrder == SortOrder.BY_ALPHABET_ASC
                    ? wordDao.getWordIdsByVocabAlpha(vocabBookId)
                    : wordDao.getWordIdsByVocab(vocabBookId);
            executors.mainThread().execute(() -> callback.onResult(ids));
        });
    }

    public interface WordIdsCallback {
        void onResult(List<Long> wordIds);
    }

    private List<Word> toWords(List<WordWithTags> entities) {
        if (entities == null) return new ArrayList<>();
        List<Word> result = new ArrayList<>();
        for (WordWithTags wt : entities) result.add(toWord(wt));
        return result;
    }

    private Word toWord(WordWithTags wt) {
        if (wt == null) return null;
        WordEntity e = wt.word;
        return new Word(
                e.id, e.vocabBookId, e.english, e.chineseDefinition,
                parseDefinitions(e.definitions),
                parsePhonetics(e.phonetics),
                parseExamples(e.examples),
                e.isBookmarked, e.addedAt,
                toTags(wt.tags)
        );
    }

    private List<Definition> parseDefinitions(String json) {
        if (json == null || json.isEmpty()) return new ArrayList<>();
        Type t = new TypeToken<List<DefinitionDto>>() {}.getType();
        List<DefinitionDto> dtos = gson.fromJson(json, t);
        List<Definition> result = new ArrayList<>();
        if (dtos != null) for (DefinitionDto d : dtos) result.add(new Definition(d.partOfSpeech, d.meaning));
        return result;
    }

    private Phonetics parsePhonetics(String json) {
        if (json == null || json.isEmpty()) return new Phonetics("", "");
        PhoneticsDto dto = gson.fromJson(json, PhoneticsDto.class);
        return new Phonetics(dto.british != null ? dto.british : "", dto.american != null ? dto.american : "");
    }

    private List<Example> parseExamples(String json) {
        if (json == null || json.isEmpty()) return new ArrayList<>();
        Type t = new TypeToken<List<ExampleDto>>() {}.getType();
        List<ExampleDto> dtos = gson.fromJson(json, t);
        List<Example> result = new ArrayList<>();
        if (dtos != null) for (ExampleDto d : dtos) result.add(new Example(d.english, d.chinese));
        return result;
    }

    private List<Tag> toTags(List<TagEntity> entities) {
        List<Tag> result = new ArrayList<>();
        if (entities != null) for (TagEntity e : entities) result.add(new Tag(e.id, e.name, e.colorHex));
        return result;
    }

    private static class DefinitionDto { String partOfSpeech; String meaning; }
    private static class PhoneticsDto { String british; String american; }
    private static class ExampleDto { String english; String chinese; }
}
