package com.vocabapp.presentation.playbackmode;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.vocabapp.R;
import com.vocabapp.databinding.FragmentPlaybackModeBinding;
import com.vocabapp.domain.enums.PlaybackMode;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PlaybackModeFragment extends Fragment {

    private FragmentPlaybackModeBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPlaybackModeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(requireView()).navigateUp());

        binding.btnModeChineseRecall.setOnClickListener(v ->
                navigateToWordDetail(PlaybackMode.CHINESE_RECALL_ENGLISH));
        binding.btnModeEnglishRecall.setOnClickListener(v ->
                navigateToWordDetail(PlaybackMode.ENGLISH_RECALL_CHINESE));
        binding.btnModeStudy.setOnClickListener(v ->
                navigateToWordDetail(PlaybackMode.STUDY_MODE));
    }

    private void navigateToWordDetail(PlaybackMode mode) {
        long vocabBookId = getArguments() != null ? getArguments().getLong("vocabBookId", -1) : -1;
        Bundle args = new Bundle();
        args.putLong("vocabBookId", vocabBookId);
        args.putLong("startWordId", -1);
        args.putString("playbackMode", mode.name());
        Navigation.findNavController(requireView())
                .navigate(R.id.action_playbackMode_to_wordDetail, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
