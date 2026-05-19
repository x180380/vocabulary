package com.vocabapp.presentation.myvocab;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import com.vocabapp.R;
import com.vocabapp.databinding.FragmentMyVocabBinding;
import com.vocabapp.domain.model.VocabBook;
import com.vocabapp.presentation.common.adapters.VocabBookAdapter;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MyVocabFragment extends Fragment {

    private FragmentMyVocabBinding binding;
    private MyVocabViewModel viewModel;
    private VocabBookAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMyVocabBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MyVocabViewModel.class);

        setupRecyclerView();
        setupClickListeners();
        observeData();
    }

    private void setupRecyclerView() {
        adapter = new VocabBookAdapter(
                book -> navigateToWordList(book),
                book -> navigateToPlaybackMode(book.id)
        );
        adapter.setOnDeleteClick(book -> viewModel.removeFromMyVocab(book.id));
        adapter.setOnAddClick(this::navigateToAddVocab);
        binding.rvVocabBooks.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        binding.rvVocabBooks.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.fabAdd.setVisibility(View.GONE);
        binding.btnSettings.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_myVocab_to_settings));
        binding.btnEdit.setOnClickListener(v -> viewModel.toggleEditMode());
    }

    private void observeData() {
        viewModel.myBooks.observe(getViewLifecycleOwner(), books -> {
            binding.tvEmpty.setVisibility(View.GONE);
            binding.rvVocabBooks.setVisibility(View.VISIBLE);
            adapter.submitList(books);
        });

        viewModel.isEditMode.observe(getViewLifecycleOwner(), isEdit -> {
            adapter.setEditMode(Boolean.TRUE.equals(isEdit));
            binding.btnEdit.setText(Boolean.TRUE.equals(isEdit) ? "完成" : "编辑");
        });
    }

    private void navigateToWordList(VocabBook book) {
        Bundle args = new Bundle();
        args.putLong("vocabBookId", book.id);
        args.putString("vocabBookTitle", book.bookName);
        Navigation.findNavController(requireView())
                .navigate(R.id.action_myVocab_to_wordGroupList, args);
    }

    private void navigateToAddVocab() {
        Navigation.findNavController(requireView())
                .navigate(R.id.action_myVocab_to_addVocab);
    }

    private void navigateToPlaybackMode(long vocabBookId) {
        Bundle args = new Bundle();
        args.putLong("vocabBookId", vocabBookId);
        Navigation.findNavController(requireView())
                .navigate(R.id.action_myVocab_to_playbackMode, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
