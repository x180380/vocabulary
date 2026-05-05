package com.vocabapp.presentation.vocablist;

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

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.vocabapp.R;
import com.vocabapp.databinding.BottomSheetCreateVocabBinding;
import com.vocabapp.databinding.FragmentVocabListBinding;
import com.vocabapp.domain.model.VocabBook;
import com.vocabapp.presentation.common.adapters.VocabBookAdapter;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class VocabListFragment extends Fragment {

    private FragmentVocabListBinding binding;
    private VocabListViewModel viewModel;
    private VocabBookAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentVocabListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(VocabListViewModel.class);

        setupRecyclerView();
        setupClickListeners();
        observeData();
    }

    private void setupRecyclerView() {
        adapter = new VocabBookAdapter(
                book -> navigateToWordList(book),
                book -> {
                    navigateToPlaybackMode(book.id);
                }
        );
        binding.rvVocabBooks.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        binding.rvVocabBooks.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.fabAdd.setOnClickListener(v -> showCreateVocabDialog());
        binding.btnSettings.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_vocabList_to_settings));
    }

    private void observeData() {
        viewModel.favoriteBooks.observe(getViewLifecycleOwner(), books -> {
            if (books == null || books.isEmpty()) {
                binding.rvVocabBooks.setVisibility(View.GONE);
                binding.tvEmpty.setVisibility(View.VISIBLE);
            } else {
                binding.rvVocabBooks.setVisibility(View.VISIBLE);
                binding.tvEmpty.setVisibility(View.GONE);
                adapter.submitList(books);
            }
        });
    }

    private void navigateToWordList(VocabBook book) {
        Bundle args = new Bundle();
        args.putLong("vocabBookId", book.id);
        args.putString("vocabBookTitle", book.title);
        Navigation.findNavController(requireView())
                .navigate(R.id.action_vocabList_to_wordList, args);
    }

    private void navigateToPlaybackMode(long vocabBookId) {
        Bundle args = new Bundle();
        args.putLong("vocabBookId", vocabBookId);
        Navigation.findNavController(requireView())
                .navigate(R.id.action_vocabList_to_playbackMode, args);
    }

    private void showCreateVocabDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        BottomSheetCreateVocabBinding dialogBinding =
                BottomSheetCreateVocabBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());

        dialogBinding.btnCreate.setOnClickListener(v -> {
            String title = dialogBinding.etVocabName.getText().toString().trim();
            if (!title.isEmpty()) {
                int colorIndex = getSelectedColorIndex(dialogBinding);
                viewModel.createVocabBook(title, colorIndex);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private int getSelectedColorIndex(BottomSheetCreateVocabBinding binding) {
        if (binding.colorChip1.isChecked()) return 1;
        if (binding.colorChip2.isChecked()) return 2;
        if (binding.colorChip3.isChecked()) return 3;
        if (binding.colorChip4.isChecked()) return 4;
        if (binding.colorChip5.isChecked()) return 5;
        if (binding.colorChip6.isChecked()) return 6;
        if (binding.colorChip7.isChecked()) return 7;
        return 0; // colorChip0 = index 0
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
