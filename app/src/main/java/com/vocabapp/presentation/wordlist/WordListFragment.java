package com.vocabapp.presentation.wordlist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.vocabapp.R;
import com.vocabapp.data.local.preferences.UserPreferencesManager;
import com.vocabapp.databinding.BottomSheetMoveToBinding;
import com.vocabapp.databinding.FragmentWordListBinding;
import com.vocabapp.domain.enums.SortOrder;
import com.vocabapp.domain.enums.VisibilityMode;
import com.vocabapp.domain.model.VocabBook;
import com.vocabapp.domain.model.Word;
import com.vocabapp.presentation.common.adapters.WordListAdapter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class WordListFragment extends Fragment {

    private FragmentWordListBinding binding;
    private WordListViewModel viewModel;
    private WordListAdapter adapter;
    private long startWordId = -1;
    private int groupCount = -1;

    @Inject UserPreferencesManager prefsManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentWordListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(WordListViewModel.class);

        long vocabBookId = getArguments() != null ? getArguments().getLong("vocabBookId", -1) : -1;
        startWordId = getArguments() != null ? getArguments().getLong("startWordId", -1) : -1;
        groupCount = getArguments() != null ? getArguments().getInt("groupCount", -1) : -1;
        viewModel.setVocabBookId(vocabBookId);

        setupRecyclerView();
        setupClickListeners();
        observeData();
    }

    private void setupRecyclerView() {
        adapter = new WordListAdapter(
                this::navigateToWordDetail,
                wordId -> viewModel.toggleWordSelection(wordId)
        );
        binding.rvWords.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvWords.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v ->
                Navigation.findNavController(requireView()).navigateUp());
        binding.btnSort.setOnClickListener(v -> showSortMenu());
        binding.btnEdit.setOnClickListener(v -> viewModel.toggleBatchMode());
        binding.btnListenWords.setOnClickListener(v -> navigateToPlaybackMode());
        binding.btnHideDefinition.setOnClickListener(v -> cycleVisibilityMode());
        binding.btnCardStudy.setOnClickListener(v -> {
            List<Word> words = getDisplayWords(viewModel.words.getValue());
            if (words != null && !words.isEmpty()) navigateToWordDetail(words.get(0));
        });
        binding.btnBatchDelete.setOnClickListener(v -> viewModel.deleteSelected());
        binding.btnBatchMoveTo.setOnClickListener(v -> showMoveToDialog());
        binding.btnSelectAll.setOnClickListener(v -> viewModel.selectAll());
    }

    private void observeData() {
        viewModel.words.observe(getViewLifecycleOwner(), words -> {
            List<Word> display = getDisplayWords(words);
            if (display == null || display.isEmpty()) {
                binding.rvWords.setVisibility(View.GONE);
                binding.tvEmpty.setVisibility(View.VISIBLE);
            } else {
                binding.rvWords.setVisibility(View.VISIBLE);
                binding.tvEmpty.setVisibility(View.GONE);
                adapter.submitList(display);
            }
        });

        viewModel.isBatchMode.observe(getViewLifecycleOwner(), isBatchMode -> {
            adapter.setBatchMode(isBatchMode);
            binding.bottomBarNormal.setVisibility(!isBatchMode ? View.VISIBLE : View.GONE);
            binding.bottomBarBatch.setVisibility(isBatchMode ? View.VISIBLE : View.GONE);
            binding.btnEdit.setImageResource(isBatchMode ? R.drawable.ic_close : R.drawable.ic_edit);
            binding.tvSelectedCount.setVisibility(isBatchMode ? View.VISIBLE : View.GONE);
            binding.btnSelectAll.setVisibility(isBatchMode ? View.VISIBLE : View.GONE);
        });

        viewModel.selectedWordIds.observe(getViewLifecycleOwner(), ids -> {
            adapter.setSelectedIds(ids);
            int count = ids != null ? ids.size() : 0;
            binding.tvSelectedCount.setText(getString(R.string.selected_count, count));
        });

        viewModel.visibilityMode.observe(getViewLifecycleOwner(), mode -> {
            adapter.setVisibilityMode(mode);
            updateHideButton(mode);
        });
    }

    private List<Word> getDisplayWords(List<Word> allWords) {
        if (allWords == null) return null;
        if (groupCount > 0 && startWordId != -1) {
            int startIdx = 0;
            for (int i = 0; i < allWords.size(); i++) {
                if (allWords.get(i).id == startWordId) { startIdx = i; break; }
            }
            int endIdx = Math.min(startIdx + groupCount, allWords.size());
            return new ArrayList<>(allWords.subList(startIdx, endIdx));
        }
        return allWords;
    }

    private void showSortMenu() {
        String[] options = {
                getString(R.string.sort_time_desc),
                getString(R.string.sort_time_asc),
                getString(R.string.sort_alpha)
        };
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("排序")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: viewModel.setSortOrder(SortOrder.BY_TIME_DESC); break;
                        case 1: viewModel.setSortOrder(SortOrder.BY_TIME_ASC); break;
                        case 2: viewModel.setSortOrder(SortOrder.BY_ALPHABET_ASC); break;
                    }
                })
                .show();
    }

    private void cycleVisibilityMode() {
        VisibilityMode current = viewModel.visibilityMode.getValue();
        if (current == null) current = VisibilityMode.SHOW_BOTH;
        switch (current) {
            case SHOW_BOTH: viewModel.setVisibilityMode(VisibilityMode.HIDE_CHINESE); break;
            case HIDE_CHINESE: viewModel.setVisibilityMode(VisibilityMode.HIDE_ENGLISH); break;
            case HIDE_ENGLISH: viewModel.setVisibilityMode(VisibilityMode.SHOW_BOTH); break;
        }
    }

    private void updateHideButton(VisibilityMode mode) {
        switch (mode) {
            case SHOW_BOTH: binding.btnHideDefinition.setText(R.string.show_both); break;
            case HIDE_CHINESE: binding.btnHideDefinition.setText(R.string.hide_definition); break;
            case HIDE_ENGLISH: binding.btnHideDefinition.setText(R.string.hide_word); break;
        }
    }

    private void showMoveToDialog() {
        List<VocabBook> books = viewModel.allVocabBooks.getValue();
        if (books == null || books.isEmpty()) return;
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        BottomSheetMoveToBinding dialogBinding = BottomSheetMoveToBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());
        String[] bookTitles = books.stream().map(b -> b.bookName).toArray(String[]::new);
        ArrayAdapter<String> bookAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, bookTitles);
        dialogBinding.lvVocabBooks.setAdapter(bookAdapter);
        dialogBinding.lvVocabBooks.setOnItemClickListener((parent, v, pos, id) -> {
            viewModel.moveSelectedTo(books.get(pos).id);
            dialog.dismiss();
        });
        dialog.show();
    }

    private void navigateToWordDetail(Word word) {
        Bundle args = new Bundle();
        Long bookId = viewModel.vocabBookId.getValue();
        args.putLong("vocabBookId", bookId != null ? bookId : -1);
        if (groupCount > 0) {
            args.putLong("startWordId", startWordId);
            args.putInt("groupCount", groupCount);
            args.putLong("initialWordId", word.id);
        } else {
            args.putLong("startWordId", word.id);
        }
        Navigation.findNavController(requireView())
                .navigate(R.id.action_wordList_to_wordDetail, args);
    }

    private void navigateToPlaybackMode() {
        Long bookId = viewModel.vocabBookId.getValue();
        if (bookId == null) return;
        Bundle args = new Bundle();
        args.putLong("vocabBookId", bookId);
        if (groupCount > 0) {
            args.putLong("startWordId", startWordId);
            args.putInt("groupCount", groupCount);
        } else {
            LinearLayoutManager lm = (LinearLayoutManager) binding.rvWords.getLayoutManager();
            List<Word> allWords = viewModel.words.getValue();
            long firstWordId = -1;
            if (lm != null && allWords != null) {
                int pos = lm.findFirstVisibleItemPosition();
                List<Word> display = getDisplayWords(allWords);
                if (display != null && pos >= 0 && pos < display.size()) {
                    firstWordId = display.get(pos).id;
                }
            }
            args.putLong("startWordId", firstWordId);
        }
        Navigation.findNavController(requireView())
                .navigate(R.id.action_wordList_to_playbackMode, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
