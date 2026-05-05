package com.vocabapp.data.local.database.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.vocabapp.data.local.database.entities.WordEntity;
import com.vocabapp.data.local.database.entities.WordWithTags;

import java.util.List;

@Dao
public interface WordDao {

    @Transaction
    @Query("SELECT * FROM words WHERE vocab_book_id = :vocabBookId ORDER BY added_at DESC")
    LiveData<List<WordWithTags>> getWordsByVocabTimeDesc(long vocabBookId);

    @Transaction
    @Query("SELECT * FROM words WHERE vocab_book_id = :vocabBookId ORDER BY added_at ASC")
    LiveData<List<WordWithTags>> getWordsByVocabTimeAsc(long vocabBookId);

    @Transaction
    @Query("SELECT * FROM words WHERE vocab_book_id = :vocabBookId ORDER BY english ASC")
    LiveData<List<WordWithTags>> getWordsByVocabAlpha(long vocabBookId);

    @Transaction
    @Query("SELECT w.* FROM words w INNER JOIN word_tag_cross_ref wt ON w.id = wt.word_id WHERE w.vocab_book_id = :vocabBookId AND wt.tag_id IN (:tagIds) GROUP BY w.id ORDER BY w.added_at DESC")
    LiveData<List<WordWithTags>> getWordsByTag(long vocabBookId, List<Long> tagIds);

    @Query("SELECT id FROM words WHERE vocab_book_id = :vocabBookId ORDER BY added_at DESC")
    List<Long> getWordIdsByVocab(long vocabBookId);

    @Query("SELECT id FROM words WHERE vocab_book_id = :vocabBookId ORDER BY english ASC")
    List<Long> getWordIdsByVocabAlpha(long vocabBookId);

    @Transaction
    @Query("SELECT * FROM words WHERE id = :wordId")
    LiveData<WordWithTags> getWordWithTagsById(long wordId);

    @Query("SELECT * FROM words WHERE id = :wordId")
    WordEntity getWordByIdSync(long wordId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertWord(WordEntity word);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertWords(List<WordEntity> words);

    @Query("DELETE FROM words WHERE id IN (:wordIds)")
    void deleteWordsByIds(List<Long> wordIds);

    @Query("UPDATE words SET vocab_book_id = :targetVocabBookId WHERE id IN (:wordIds)")
    void moveWordsToVocab(List<Long> wordIds, long targetVocabBookId);

    @Query("UPDATE words SET is_bookmarked = :isBookmarked WHERE id = :wordId")
    void setBookmark(long wordId, boolean isBookmarked);

    @Query("SELECT COUNT(*) FROM words WHERE vocab_book_id = :vocabBookId")
    int getWordCount(long vocabBookId);
}
