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

    @Query("SELECT v.*, COUNT(bw.id) AS word_count FROM vocab_books v LEFT JOIN book_words bw ON v.book_id = bw.book_id GROUP BY v.book_id ORDER BY v.book_id ASC")
    LiveData<List<VocabBookWithCount>> getAllVocabBooksWithCount();

    @Query("SELECT * FROM vocab_books WHERE book_id = :id")
    VocabBookEntity getVocabBookByIdSync(long id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertVocabBook(VocabBookEntity book);

    @Query("SELECT COUNT(*) FROM vocab_books")
    int getCount();

    @Query("DELETE FROM vocab_books WHERE book_id = :id")
    void deleteById(long id);
}
