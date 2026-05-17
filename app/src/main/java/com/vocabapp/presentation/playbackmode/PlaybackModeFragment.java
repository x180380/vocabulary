package com.vocabapp.presentation.playbackmode;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.vocabapp.R;
import com.vocabapp.databinding.FragmentPlaybackModeBinding;
import com.vocabapp.domain.enums.PlaybackMode;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PlaybackModeFragment extends BottomSheetDialogFragment {

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

        binding.btnModeChineseRecall.setOnClickListener(v ->
                navigateToWordDetail(PlaybackMode.CHINESE_RECALL_ENGLISH));
        binding.btnModeEnglishRecall.setOnClickListener(v ->
                navigateToWordDetail(PlaybackMode.ENGLISH_RECALL_CHINESE));
        binding.btnModeStudy.setOnClickListener(v ->
                navigateToWordDetail(PlaybackMode.STUDY_MODE));
        binding.btnModeMemorizeExample.setOnClickListener(v ->
                navigateToWordDetail(PlaybackMode.MEMORIZE_EXAMPLE));
    }

    private void navigateToWordDetail(PlaybackMode mode) {
        long vocabBookId = getArguments() != null ? getArguments().getLong("vocabBookId", -1) : -1;
        long startWordId = getArguments() != null ? getArguments().getLong("startWordId", -1) : -1;
        int groupCount = getArguments() != null ? getArguments().getInt("groupCount", -1) : -1;
        long initialWordId = getArguments() != null ? getArguments().getLong("initialWordId", -1) : -1;
        Bundle args = new Bundle();
        args.putLong("vocabBookId", vocabBookId);
        args.putLong("startWordId", startWordId);
        args.putInt("groupCount", groupCount);
        args.putLong("initialWordId", initialWordId);
        args.putString("playbackMode", mode.name());
        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                .navigate(R.id.action_playbackMode_to_wordDetail, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
