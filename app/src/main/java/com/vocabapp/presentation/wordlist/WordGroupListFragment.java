package com.vocabapp.presentation.wordlist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.vocabapp.R;
import com.vocabapp.data.local.preferences.UserPreferencesManager;
import com.vocabapp.databinding.FragmentWordGroupListBinding;
import com.vocabapp.domain.model.Word;
import com.vocabapp.presentation.common.adapters.WordGroupAdapter;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class WordGroupListFragment extends Fragment {

    private FragmentWordGroupListBinding binding;
    private WordListViewModel viewModel;
    private WordGroupAdapter groupAdapter;
    private long vocabBookId;

    @Inject UserPreferencesManager prefsManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentWordGroupListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(WordListViewModel.class);

        vocabBookId = getArguments() != null ? getArguments().getLong("vocabBookId", -1) : -1;
        viewModel.setVocabBookId(vocabBookId);

        setupRecyclerView();
        setupClickListeners();
        observeData();
    }

    private void setupRecyclerView() {
        groupAdapter = new WordGroupAdapter();
        groupAdapter.setOnGroupClickListener(this::navigateToGroupWordList);
        groupAdapter.setOnGroupPlayListener(this::navigateToPlaybackModeForGroup);
        binding.rvGroups.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvGroups.setAdapter(groupAdapter);
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v ->
                Navigation.findNavController(requireView()).navigateUp());
        binding.btnListView.setOnClickListener(v -> navigateToAllWordList());
    }

    private void observeData() {
        viewModel.words.observe(getViewLifecycleOwner(), words -> {
            if (words == null || words.isEmpty()) {
                binding.rvGroups.setVisibility(View.GONE);
                binding.tvEmpty.setVisibility(View.VISIBLE);
            } else {
                binding.rvGroups.setVisibility(View.VISIBLE);
                binding.tvEmpty.setVisibility(View.GONE);
                groupAdapter.setWords(words, prefsManager.getGroupSize());
            }
        });
    }

    private void navigateToGroupWordList(int groupIndex) {
        List<Word> words = viewModel.words.getValue();
        if (words == null) return;
        int groupSize = prefsManager.getGroupSize();
        int startIdx = groupIndex * groupSize;
        if (startIdx >= words.size()) return;
        Bundle args = new Bundle();
        args.putLong("vocabBookId", vocabBookId);
        args.putLong("startWordId", words.get(startIdx).id);
        args.putInt("groupCount", groupSize);
        Navigation.findNavController(requireView())
                .navigate(R.id.action_wordGroupList_to_wordList, args);
    }

    private void navigateToAllWordList() {
        Bundle args = new Bundle();
        args.putLong("vocabBookId", vocabBookId);
        Navigation.findNavController(requireView())
                .navigate(R.id.action_wordGroupList_to_wordList, args);
    }

    private void navigateToPlaybackModeForGroup(int groupIndex) {
        List<Word> words = viewModel.words.getValue();
        if (words == null) return;
        int groupSize = prefsManager.getGroupSize();
        int startIdx = groupIndex * groupSize;
        if (startIdx >= words.size()) return;
        Bundle args = new Bundle();
        args.putLong("vocabBookId", vocabBookId);
        args.putLong("startWordId", words.get(startIdx).id);
        args.putInt("groupCount", groupSize);
        Navigation.findNavController(requireView())
                .navigate(R.id.action_wordGroupList_to_playbackMode, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
