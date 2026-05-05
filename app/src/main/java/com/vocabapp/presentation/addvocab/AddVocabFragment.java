package com.vocabapp.presentation.addvocab;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.vocabapp.databinding.BottomSheetCreateVocabBinding;
import com.vocabapp.databinding.FragmentAddVocabBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AddVocabFragment extends Fragment {

    private FragmentAddVocabBinding binding;
    private AddVocabViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAddVocabBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AddVocabViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(requireView()).navigateUp());

        binding.btnNewVocabBook.setOnClickListener(v -> showCreateDialog());
    }

    private void showCreateDialog() {
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
                Navigation.findNavController(requireView()).navigateUp();
            }
        });
        dialog.show();
    }

    private int getSelectedColorIndex(BottomSheetCreateVocabBinding b) {
        if (b.colorChip1.isChecked()) return 1;
        if (b.colorChip2.isChecked()) return 2;
        if (b.colorChip3.isChecked()) return 3;
        if (b.colorChip4.isChecked()) return 4;
        if (b.colorChip5.isChecked()) return 5;
        if (b.colorChip6.isChecked()) return 6;
        if (b.colorChip7.isChecked()) return 7;
        return 0;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
