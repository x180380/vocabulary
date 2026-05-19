package com.vocabapp.data.local.database.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.vocabapp.data.local.database.entities.VocabBookEntity;
import com.vocabapp.data.local.database.entities.VocabBookWithCount;

import java.util.List;

@Dao
public interface VocabBookDao {

    @Query("SELECT v.*, COUNT(bw.id) AS word_count FROM vocab_books v LEFT JOIN book_words bw ON v.book_id = bw.book_id WHERE v.is_mine = 1 GROUP BY v.book_id ORDER BY v.book_id ASC")
    LiveData<List<VocabBookWithCount>> getMyVocabBooksWithCount();

    @Query("SELECT v.*, COUNT(bw.id) AS word_count FROM vocab_books v LEFT JOIN book_words bw ON v.book_id = bw.book_id GROUP BY v.book_id ORDER BY v.book_id ASC")
    LiveData<List<VocabBookWithCount>> getAllLibraryBooksWithCount();

    @Query("UPDATE vocab_books SET is_mine = 1 WHERE book_id = :id")
    void markAsMine(long id);

    @Query("UPDATE vocab_books SET is_mine = 0 WHERE book_id = :id")
    void markAsNotMine(long id);

    @Query("SELECT * FROM vocab_books WHERE book_id = :id")
    VocabBookEntity getVocabBookByIdSync(long id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertVocabBook(VocabBookEntity book);

    @Query("SELECT COUNT(*) FROM vocab_books")
    int getCount();

    @Query("DELETE FROM vocab_books WHERE book_id = :id")
    void deleteById(long id);
}
