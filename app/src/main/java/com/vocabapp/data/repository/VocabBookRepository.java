package com.vocabapp.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.vocabapp.data.local.database.daos.VocabBookDao;
import com.vocabapp.data.local.database.entities.VocabBookWithCount;
import com.vocabapp.domain.model.VocabBook;
import com.vocabapp.presentation.common.AppExecutors;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class VocabBookRepository {

    private final VocabBookDao dao;
    private final AppExecutors executors;

    @Inject
    public VocabBookRepository(VocabBookDao dao, AppExecutors executors) {
        this.dao = dao;
        this.executors = executors;
    }

    public LiveData<List<VocabBook>> getAllLibraryBooks() {
        return Transformations.map(dao.getAllLibraryBooksWithCount(), this::toModels);
    }

    public void createCustomVocab(String name) {
        executors.diskIO().execute(() -> {
            com.vocabapp.data.local.database.entities.VocabBookEntity entity =
                    new com.vocabapp.data.local.database.entities.VocabBookEntity();
            entity.bookName = name;
            entity.assetFile = null;
            entity.isMine = true;
            dao.insertVocabBook(entity);
        });
    }

    public void addToMyVocab(long bookId) {
        executors.diskIO().execute(() -> dao.markAsMine(bookId));
    }

    public void removeFromMyVocab(long bookId) {
        executors.diskIO().execute(() -> dao.markAsNotMine(bookId));
    }

    public void deleteVocabBook(long bookId) {
        executors.diskIO().execute(() -> dao.deleteById(bookId));
    }

    public LiveData<List<VocabBook>> getMyVocabBooks() {
        return Transformations.map(dao.getMyVocabBooksWithCount(), this::toModels);
    }

    private List<VocabBook> toModels(List<VocabBookWithCount> entities) {
        if (entities == null) return new ArrayList<>();
        List<VocabBook> result = new ArrayList<>();
        for (VocabBookWithCount e : entities) {
            VocabBook book = toModel(e);
            if (book != null) result.add(book);
        }
        return result;
    }

    private VocabBook toModel(VocabBookWithCount e) {
        if (e == null || e.book == null) return null;
        return new VocabBook(
                e.book.bookId,
                e.book.bookName,
                e.book.assetFile,
                e.wordCount,
                colorIndexFor(e.book.assetFile),
                e.book.isMine
        );
    }

    private int colorIndexFor(String assetFile) {
        if (assetFile == null) return 0;
        if (assetFile.contains("zhongkao")) return 1;
        if (assetFile.contains("gaokao")) return 3;
        if (assetFile.contains("cet4")) return 5;
        return 0;
    }
}
