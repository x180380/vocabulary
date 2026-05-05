package com.vocabapp.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.vocabapp.data.local.database.daos.TagDao;
import com.vocabapp.data.local.database.entities.TagEntity;
import com.vocabapp.data.local.database.entities.WordTagCrossRef;
import com.vocabapp.domain.model.Tag;
import com.vocabapp.presentation.common.AppExecutors;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TagRepository {

    private final TagDao tagDao;
    private final AppExecutors executors;

    @Inject
    public TagRepository(TagDao tagDao, AppExecutors executors) {
        this.tagDao = tagDao;
        this.executors = executors;
    }

    public LiveData<List<Tag>> getAllTags() {
        return Transformations.map(tagDao.getAllTags(), entities -> {
            List<Tag> result = new ArrayList<>();
            if (entities != null) {
                for (TagEntity e : entities) result.add(new Tag(e.id, e.name, e.colorHex));
            }
            return result;
        });
    }

    public void addTagToWords(String tagName, String colorHex, List<Long> wordIds) {
        executors.diskIO().execute(() -> {
            TagEntity existing = tagDao.getTagByName(tagName);
            long tagId;
            if (existing != null) {
                tagId = existing.id;
            } else {
                TagEntity newTag = new TagEntity(tagName, colorHex);
                tagId = tagDao.insertTag(newTag);
            }
            List<WordTagCrossRef> refs = new ArrayList<>();
            for (Long wordId : wordIds) refs.add(new WordTagCrossRef(wordId, tagId));
            tagDao.insertWordTagCrossRefs(refs);
        });
    }

    public void removeTagsFromWords(List<Long> wordIds) {
        executors.diskIO().execute(() -> tagDao.removeAllTagsFromWords(wordIds));
    }
}
