package com.vocabapp.data.local.database.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.vocabapp.data.local.database.entities.BookWordEntity;
import com.vocabapp.data.local.database.entities.BookWordWithDefinition;

import java.util.List;

@Dao
public interface BookWordDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<BookWordEntity> bookWords);

    @Transaction
    @Query("SELECT * FROM book_words WHERE book_id = :bookId ORDER BY word ASC")
    LiveData<List<BookWordWithDefinition>> getWordsByBook(long bookId);

    @Transaction
    @Query("SELECT * FROM book_words WHERE id = :id")
    LiveData<BookWordWithDefinition> getById(long id);

    @Query("SELECT id FROM book_words WHERE book_id = :bookId ORDER BY word ASC")
    List<Long> getWordIdsByBook(long bookId);

    @Query("DELETE FROM book_words WHERE id IN (:ids)")
    void deleteByIds(List<Long> ids);

    @Query("UPDATE book_words SET book_id = :targetBookId WHERE id IN (:ids)")
    void moveToBook(List<Long> ids, long targetBookId);
}
