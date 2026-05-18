package com.vocabapp.data.repository;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Transformations;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vocabapp.data.local.database.daos.BookmarkDao;
import com.vocabapp.data.local.database.daos.BookWordDao;
import com.vocabapp.data.local.database.entities.BookmarkEntity;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class WordRepository {

    private final BookWordDao bookWordDao;
    private final BookmarkDao bookmarkDao;
    private final AppExecutors executors;
    private final Gson gson;

    @Inject
    public WordRepository(BookWordDao bookWordDao, BookmarkDao bookmarkDao,
                          AppExecutors executors, Gson gson) {
        this.bookWordDao = bookWordDao;
        this.bookmarkDao = bookmarkDao;
        this.executors = executors;
        this.gson = gson;
    }

    public LiveData<List<Word>> getWordsByVocab(long vocabBookId, SortOrder sortOrder) {
        LiveData<List<BookWordWithDefinition>> wordsSource;
        if (sortOrder == SortOrder.RANDOM) {
            wordsSource = bookWordDao.getWordsByBookRandom(vocabBookId);
        } else if (sortOrder == SortOrder.BY_TIME_DESC) {
            wordsSource = bookWordDao.getWordsByBookOriginal(vocabBookId);
        } else {
            wordsSource = bookWordDao.getWordsByBook(vocabBookId);
        }
        LiveData<List<Long>> bookmarkSource = bookmarkDao.getAllBookmarkedWordIds();

        MediatorLiveData<List<Word>> result = new MediatorLiveData<>();
        result.addSource(wordsSource, items -> {
            List<Long> bookmarked = bookmarkSource.getValue();
            result.setValue(toWordsWithBookmarks(items, bookmarked));
        });
        result.addSource(bookmarkSource, bookmarked -> {
            List<BookWordWithDefinition> items = wordsSource.getValue();
            result.setValue(toWordsWithBookmarks(items, bookmarked));
        });
        return result;
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

    public LiveData<Boolean> isBookmarked(long wordId) {
        return bookmarkDao.isBookmarked(wordId);
    }

    public void toggleBookmark(long wordId, boolean currentlyBookmarked) {
        executors.diskIO().execute(() -> {
            if (currentlyBookmarked) {
                bookmarkDao.delete(wordId);
            } else {
                bookmarkDao.insert(new BookmarkEntity(wordId));
            }
        });
    }

    public interface WordIdsCallback {
        void onResult(List<Long> wordIds);
    }

    private List<Word> toWordsWithBookmarks(List<BookWordWithDefinition> items, List<Long> bookmarkedIds) {
        if (items == null) return new ArrayList<>();
        Set<Long> bookmarkedSet = bookmarkedIds != null ? new HashSet<>(bookmarkedIds) : new HashSet<>();
        List<Word> result = new ArrayList<>();
        for (BookWordWithDefinition bwd : items) {
            if (bwd == null || bwd.bookWord == null) continue;
            Word word = toWord(bwd, bookmarkedSet.contains(bwd.bookWord.id));
            if (word != null) result.add(word);
        }
        return result;
    }

    private List<Word> toWords(List<BookWordWithDefinition> items) {
        return toWordsWithBookmarks(items, null);
    }

    private Word toWord(BookWordWithDefinition bwd, boolean isBookmarked) {
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
                examples,
                isBookmarked
        );
    }

    private Word toWord(BookWordWithDefinition bwd) {
        return toWord(bwd, false);
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
