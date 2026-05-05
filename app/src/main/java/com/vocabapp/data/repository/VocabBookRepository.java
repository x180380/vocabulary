package com.vocabapp.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vocabapp.data.local.database.daos.VocabBookDao;
import com.vocabapp.data.local.database.entities.VocabBookEntity;
import com.vocabapp.domain.model.VocabBook;
import com.vocabapp.presentation.common.AppExecutors;

import java.lang.reflect.Type;
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

    public LiveData<List<VocabBook>> getFavoriteVocabBooks() {
        return Transformations.map(dao.getFavoriteVocabBooks(), this::toModels);
    }

    public LiveData<List<VocabBook>> getAllVocabBooks() {
        return Transformations.map(dao.getAllVocabBooks(), this::toModels);
    }

    public LiveData<VocabBook> getVocabBookById(long id) {
        return Transformations.map(dao.getVocabBookById(id), this::toModel);
    }

    public void createVocabBook(String title, String description, int colorIndex, boolean isFavorite) {
        executors.diskIO().execute(() -> {
            VocabBookEntity entity = new VocabBookEntity(title, description, colorIndex, isFavorite);
            dao.insertVocabBook(entity);
        });
    }

    public void setFavorite(long id, boolean isFavorite) {
        executors.diskIO().execute(() -> dao.setFavorite(id, isFavorite));
    }

    public void deleteVocabBook(long id) {
        executors.diskIO().execute(() -> {
            VocabBookEntity entity = dao.getVocabBookByIdSync(id);
            if (entity != null) dao.deleteVocabBook(entity);
        });
    }

    private List<VocabBook> toModels(List<VocabBookEntity> entities) {
        if (entities == null) return new ArrayList<>();
        List<VocabBook> result = new ArrayList<>();
        for (VocabBookEntity e : entities) result.add(toModel(e));
        return result;
    }

    private VocabBook toModel(VocabBookEntity e) {
        if (e == null) return null;
        return new VocabBook(e.id, e.title, e.description, e.colorIndex,
                e.isFavorite, e.createdAt, e.wordCount);
    }
}
