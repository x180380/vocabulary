package com.vocabapp.data.local.database.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.vocabapp.data.local.database.entities.VocabBookEntity;

import java.util.List;

@Dao
public interface VocabBookDao {

    @Query("SELECT * FROM vocab_books WHERE is_favorite = 1 ORDER BY created_at DESC")
    LiveData<List<VocabBookEntity>> getFavoriteVocabBooks();

    @Query("SELECT * FROM vocab_books ORDER BY created_at DESC")
    LiveData<List<VocabBookEntity>> getAllVocabBooks();

    @Query("SELECT * FROM vocab_books WHERE id = :id")
    LiveData<VocabBookEntity> getVocabBookById(long id);

    @Query("SELECT * FROM vocab_books WHERE id = :id")
    VocabBookEntity getVocabBookByIdSync(long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertVocabBook(VocabBookEntity book);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertVocabBooks(List<VocabBookEntity> books);

    @Update
    void updateVocabBook(VocabBookEntity book);

    @Delete
    void deleteVocabBook(VocabBookEntity book);

    @Query("UPDATE vocab_books SET is_favorite = :isFavorite WHERE id = :id")
    void setFavorite(long id, boolean isFavorite);

    @Query("UPDATE vocab_books SET word_count = (SELECT COUNT(*) FROM words WHERE vocab_book_id = :id) WHERE id = :id")
    void refreshWordCount(long id);

    @Query("SELECT COUNT(*) FROM vocab_books")
    int getCount();
}
