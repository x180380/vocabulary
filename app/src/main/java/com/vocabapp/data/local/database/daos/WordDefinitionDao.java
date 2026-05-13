package com.vocabapp.data.local.database.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;

import com.vocabapp.data.local.database.entities.WordDefinitionEntity;

import java.util.List;

@Dao
public interface WordDefinitionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<WordDefinitionEntity> words);
}
