package com.vocabapp.data.repository;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vocabapp.data.local.database.daos.BookWordDao;
import com.vocabapp.data.local.database.entities.BookWordWithDefinition;
import com.vocabapp.data.local.database.entities.WordDefinitionEntity;
import com.vocabapp.domain.enums.SortOrder;
import com.vocabapp.domain.model.Definition;
import com.vocabapp.domain.model.Example;
import com.vocabapp.domain.model.Phonetics;
import com.vocabapp.domain.model.Word;
import com.vocabapp.presentation.common.AppExecutors;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class WordRepository {

    private final BookWordDao bookWordDao;
    private final AppExecutors executors;
    private final Gson gson;

    @Inject
    public WordRepository(BookWordDao bookWordDao, AppExecutors executors, Gson gson) {
        this.bookWordDao = bookWordDao;
        this.executors = executors;
        this.gson = gson;
    }

    public LiveData<List<Word>> getWordsByVocab(long vocabBookId, SortOrder sortOrder) {
        return Transformations.map(bookWordDao.getWordsByBook(vocabBookId), this::toWords);
    }

    public LiveData<Word> getWordById(long wordId) {
        return Transformations.map(bookWordDao.getById(wordId), bwd -> toWord(bwd));
    }

    public void deleteWords(List<Long> wordIds) {
        executors.diskIO().execute(() -> {
            if (wordIds == null || wordIds.isEmpty()) return;
            bookWordDao.deleteByIds(wordIds);
        });
    }

    public void moveWords(List<Long> wordIds, long targetVocabBookId) {
        executors.diskIO().execute(() -> {
            if (wordIds == null || wordIds.isEmpty()) return;
            bookWordDao.moveToBook(wordIds, targetVocabBookId);
        });
    }

    public void getWordIdsAsync(long vocabBookId, @Nullable SortOrder sortOrder, WordIdsCallback callback) {
        executors.diskIO().execute(() -> {
            List<Long> ids = bookWordDao.getWordIdsByBook(vocabBookId);
            executors.mainThread().execute(() -> callback.onResult(ids));
        });
    }

    public interface WordIdsCallback {
        void onResult(List<Long> wordIds);
    }

    private List<Word> toWords(List<BookWordWithDefinition> items) {
        if (items == null) return new ArrayList<>();
        List<Word> result = new ArrayList<>();
        for (BookWordWithDefinition bwd : items) {
            Word word = toWord(bwd);
            if (word != null) result.add(word);
        }
        return result;
    }

    private Word toWord(BookWordWithDefinition bwd) {
        if (bwd == null || bwd.bookWord == null) return null;
        WordDefinitionEntity def = (bwd.definitions != null && !bwd.definitions.isEmpty())
                ? bwd.definitions.get(0) : null;

        List<Definition> definitions = def != null ? parseDefinitions(def.definitions) : new ArrayList<>();
        String chineseDefinition = definitions.isEmpty() ? "" : definitions.get(0).meaning;
        Phonetics phonetics = new Phonetics(
                def != null && def.phoneticsBritish != null ? def.phoneticsBritish : "",
                def != null && def.phoneticsAmerican != null ? def.phoneticsAmerican : ""
        );
        List<Example> examples = def != null
                ? buildExamples(def.exampleEnglish, def.exampleChinese)
                : new ArrayList<>();

        return new Word(
                bwd.bookWord.id,
                bwd.bookWord.bookId,
                bwd.bookWord.word,
                chineseDefinition,
                definitions,
                phonetics,
                examples
        );
    }

    private List<Definition> parseDefinitions(String json) {
        if (json == null || json.isEmpty()) return new ArrayList<>();
        Type t = new TypeToken<List<DefinitionDto>>() {}.getType();
        List<DefinitionDto> dtos = gson.fromJson(json, t);
        List<Definition> result = new ArrayList<>();
        if (dtos != null) {
            for (DefinitionDto d : dtos) result.add(new Definition(d.partOfSpeech, d.meaning));
        }
        return result;
    }

    private List<Example> buildExamples(String englishEx, String chineseEx) {
        if ((englishEx == null || englishEx.isEmpty()) && (chineseEx == null || chineseEx.isEmpty())) {
            return new ArrayList<>();
        }
        List<Example> result = new ArrayList<>();
        result.add(new Example(englishEx, chineseEx));
        return result;
    }

    private static class DefinitionDto {
        String partOfSpeech;
        String meaning;
    }
}
