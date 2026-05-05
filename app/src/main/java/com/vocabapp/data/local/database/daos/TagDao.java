package com.vocabapp.data.local.database.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.vocabapp.data.local.database.entities.TagEntity;
import com.vocabapp.data.local.database.entities.WordTagCrossRef;

import java.util.List;

@Dao
public interface TagDao {

    @Query("SELECT * FROM tags ORDER BY name ASC")
    LiveData<List<TagEntity>> getAllTags();

    @Query("SELECT * FROM tags ORDER BY name ASC")
    List<TagEntity> getAllTagsSync();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertTag(TagEntity tag);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertWordTagCrossRefs(List<WordTagCrossRef> crossRefs);

    @Query("DELETE FROM word_tag_cross_ref WHERE word_id IN (:wordIds)")
    void removeAllTagsFromWords(List<Long> wordIds);

    @Query("DELETE FROM word_tag_cross_ref WHERE word_id = :wordId AND tag_id = :tagId")
    void removeTagFromWord(long wordId, long tagId);

    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    TagEntity getTagByName(String name);
}
