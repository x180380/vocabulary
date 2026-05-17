package com.vocabapp.presentation.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.vocabapp.R;
import com.vocabapp.databinding.FragmentSettingsBinding;
import com.vocabapp.domain.enums.PronunciationAccent;
import com.vocabapp.domain.enums.VisibilityMode;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private SettingsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        binding.btnBack.setOnClickListener(v ->
                Navigation.findNavController(requireView()).navigateUp());

        setupClickListeners();
        observeData();
    }

    private void setupClickListeners() {
        binding.rowDisplayMode.setOnClickListener(v -> showVisibilityModeDialog());
        binding.rowAccent.setOnClickListener(v -> showAccentDialog());
        binding.rowAutoAdvance.setOnClickListener(v -> showAutoAdvanceDialog());
        binding.rowGroupSize.setOnClickListener(v -> showGroupSizeDialog());
    }

    private void observeData() {
        viewModel.visibilityMode.observe(getViewLifecycleOwner(), mode -> {
            String label;
            switch (mode) {
                case HIDE_ENGLISH: label = getString(R.string.hide_word); break;
                case HIDE_CHINESE: label = getString(R.string.hide_chinese); break;
                default: label = getString(R.string.show_both); break;
            }
            binding.tvDisplayModeValue.setText(label);
        });

        viewModel.accent.observe(getViewLifecycleOwner(), accent -> {
            binding.tvAccentValue.setText(accent == PronunciationAccent.AMERICAN
                    ? getString(R.string.accent_american) : getString(R.string.accent_british));
        });

        viewModel.autoAdvanceSeconds.observe(getViewLifecycleOwner(), seconds ->
                binding.tvAutoAdvanceValue.setText(getString(R.string.seconds_format, seconds)));

        viewModel.groupSize.observe(getViewLifecycleOwner(), size ->
                binding.tvGroupSizeValue.setText(getString(R.string.group_size_format, size)));
    }

    private void showVisibilityModeDialog() {
        String[] options = {
                getString(R.string.show_both),
                getString(R.string.hide_word),
                getString(R.string.hide_chinese)
        };
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.setting_default_display))
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: viewModel.setVisibilityMode(VisibilityMode.SHOW_BOTH); break;
                        case 1: viewModel.setVisibilityMode(VisibilityMode.HIDE_ENGLISH); break;
                        case 2: viewModel.setVisibilityMode(VisibilityMode.HIDE_CHINESE); break;
                    }
                })
                .show();
    }

    private void showAccentDialog() {
        String[] options = {getString(R.string.accent_american), getString(R.string.accent_british)};
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.setting_pronunciation))
                .setItems(options, (dialog, which) ->
                        viewModel.setAccent(which == 0 ? PronunciationAccent.AMERICAN : PronunciationAccent.BRITISH))
                .show();
    }

    private void showAutoAdvanceDialog() {
        int[] options = {0, 1, 3, 5, 7, 10, 15};
        String[] labels = {"0秒", "1秒", "3秒", "5秒", "7秒", "10秒", "15秒"};
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.setting_auto_advance))
                .setItems(labels, (dialog, which) -> viewModel.setAutoAdvanceSeconds(options[which]))
                .show();
    }

    private void showGroupSizeDialog() {
        int[] options = {20, 50, 100, 200};
        String[] labels = {"20个", "50个", "100个", "200个"};
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.setting_group_size))
                .setItems(labels, (dialog, which) -> viewModel.setGroupSize(options[which]))
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
