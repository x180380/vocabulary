package com.vocabapp.data.local.database.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.vocabapp.data.local.database.entities.BookmarkEntity;

import java.util.List;

@Dao
public interface BookmarkDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(BookmarkEntity bookmark);

    @Query("DELETE FROM bookmarks WHERE word_id = :wordId")
    void delete(long wordId);

    @Query("SELECT COUNT(*) > 0 FROM bookmarks WHERE word_id = :wordId")
    LiveData<Boolean> isBookmarked(long wordId);

    @Query("SELECT word_id FROM bookmarks")
    LiveData<List<Long>> getAllBookmarkedWordIds();
}
