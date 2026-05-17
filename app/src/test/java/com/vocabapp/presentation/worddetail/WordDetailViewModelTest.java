package com.vocabapp.presentation.worddetail;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class WordDetailViewModelTest {

    @Test
    public void computeNextIndex_advancesNormally() {
        assertEquals(1, WordDetailViewModel.computeNextIndex(0, 3));
        assertEquals(2, WordDetailViewModel.computeNextIndex(1, 3));
    }

    @Test
    public void computeNextIndex_wrapsAroundAtLastCard() {
        assertEquals(0, WordDetailViewModel.computeNextIndex(2, 3));
    }

    @Test
    public void computeNextIndex_singleWord_staysAtZero() {
        assertEquals(0, WordDetailViewModel.computeNextIndex(0, 1));
    }

    @Test
    public void computeNextIndex_emptyList_returnsZero() {
        assertEquals(0, WordDetailViewModel.computeNextIndex(0, 0));
    }

    @Test
    public void computeNextIndex_twoWords_loopsCorrectly() {
        assertEquals(1, WordDetailViewModel.computeNextIndex(0, 2));
        assertEquals(0, WordDetailViewModel.computeNextIndex(1, 2));
    }
}
