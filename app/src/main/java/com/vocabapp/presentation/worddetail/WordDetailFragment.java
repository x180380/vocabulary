package com.vocabapp.presentation.worddetail;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.UtteranceProgressListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.vocabapp.R;
import com.vocabapp.audio.TtsManager;
import com.vocabapp.data.local.preferences.UserPreferencesManager;
import com.vocabapp.databinding.BottomSheetVisibilityModeBinding;
import com.vocabapp.databinding.FragmentWordDetailBinding;
import com.vocabapp.domain.enums.PlaybackMode;
import com.vocabapp.domain.enums.PronunciationAccent;
import com.vocabapp.domain.enums.VisibilityMode;
import com.vocabapp.domain.model.Definition;
import com.vocabapp.domain.model.Word;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class WordDetailFragment extends Fragment {

    private FragmentWordDetailBinding binding;
    private WordDetailViewModel viewModel;
    private WordDetailPagerAdapter pagerAdapter;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable autoAdvanceRunnable;
    private Runnable playAudioRunnable;
    private Runnable enableOverlayRunnable;
    private Runnable autoAdvanceAfterTtsRunnable;
    private boolean initialScrollDone = false;
    private boolean tapOverlayEnabled = false;
    private boolean readyToAdvance = false;

    @Inject TtsManager ttsManager;
    @Inject UserPreferencesManager prefsManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentWordDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(WordDetailViewModel.class);

        long vocabBookId = getArguments() != null ? getArguments().getLong("vocabBookId", -1) : -1;
        long startWordId = getArguments() != null ? getArguments().getLong("startWordId", -1) : -1;
        int groupCount = getArguments() != null ? getArguments().getInt("groupCount", -1) : -1;
        long initialWordId = getArguments() != null ? getArguments().getLong("initialWordId", -1) : -1;
        viewModel.init(vocabBookId, startWordId, groupCount, initialWordId);

        String playbackModeStr = getArguments() != null ? getArguments().getString("playbackMode", "") : "";
        if (!playbackModeStr.isEmpty()) {
            try {
                viewModel.setPlaybackMode(PlaybackMode.valueOf(playbackModeStr));
            } catch (IllegalArgumentException ignored) {}
        }

        setupPager();
        setupClickListeners();
        observeData();
    }

    private void setupPager() {
        pagerAdapter = new WordDetailPagerAdapter();
        pagerAdapter.setPlayClickListener(new WordDetailPagerAdapter.OnPlayClickListener() {
            @Override
            public void onPlayWord(Word word) {
                ttsManager.speakEnglish(word.english, prefsManager.getAccent(), "word_" + word.id);
            }

            @Override
            public void onPlayBritish(Word word) {
                ttsManager.speakEnglish(word.english, PronunciationAccent.BRITISH, "phonetic_gb_" + word.id);
            }

            @Override
            public void onPlayAmerican(Word word) {
                ttsManager.speakEnglish(word.english, PronunciationAccent.AMERICAN, "phonetic_us_" + word.id);
            }

            @Override
            public void onPlayExample(Word word, int exampleIndex) {
                if (word.examples != null && exampleIndex < word.examples.size()) {
                    ttsManager.speakEnglish(word.examples.get(exampleIndex).english,
                            prefsManager.getAccent(), "ex_" + word.id + "_" + exampleIndex);
                }
            }

            @Override
            public void onRevealWord() {
                viewModel.revealEnglish();
            }

            @Override
            public void onToggleBookmark(Word word) {
                // bookmark feature removed
            }
        });

        binding.viewPager.setAdapter(pagerAdapter);
        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                pagerAdapter.stopExampleAnimation();
                viewModel.onPageChanged(position);
                pagerAdapter.setCurrentPosition(position);

                PlaybackMode mode = viewModel.playbackMode.getValue();
                if (mode != null) {
                    cancelPending();
                    disableTapOverlay();
                    playAudioRunnable = () -> playPageAudio(position);
                    handler.postDelayed(playAudioRunnable, 300);
                    if (mode != PlaybackMode.STUDY_MODE) {
                        enableOverlayRunnable = WordDetailFragment.this::enableTapOverlay;
                        handler.postDelayed(enableOverlayRunnable, 900);
                    }
                }
            }
        });
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v ->
                Navigation.findNavController(requireView()).navigateUp());
        binding.btnSettings.setOnClickListener(v ->
                Navigation.findNavController(requireView()).navigate(R.id.action_wordDetail_to_settings));
        binding.btnHideToggle.setOnClickListener(v -> showVisibilityModeSheet());
        binding.btnAutoPlay.setOnClickListener(v -> {
            if (viewModel.playbackMode.getValue() != null) {
                stopPlayingMode();
                return;
            }
            PlaybackMode lastMode = viewModel.getLastPlaybackMode();
            if (lastMode != null) {
                resumePlayingMode(lastMode);
                return;
            }
            long vocabBookId = getArguments() != null ? getArguments().getLong("vocabBookId", -1) : -1;
            int groupCount = getArguments() != null ? getArguments().getInt("groupCount", -1) : -1;
            List<Word> words = viewModel.allWords.getValue();
            Integer idx = viewModel.currentIndex.getValue();
            long currentWordId = (words != null && idx != null && idx < words.size())
                    ? words.get(idx).id : -1;
            Bundle args = new Bundle();
            args.putLong("vocabBookId", vocabBookId);
            if (groupCount > 0) {
                long groupStartWordId = getArguments() != null ? getArguments().getLong("startWordId", -1) : -1;
                args.putLong("startWordId", groupStartWordId);
                args.putInt("groupCount", groupCount);
                args.putLong("initialWordId", currentWordId);
            } else {
                args.putLong("startWordId", currentWordId);
            }
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_wordDetail_to_playbackMode, args);
        });
        binding.tapOverlay.setOnClickListener(v -> onTapOverlay());
        binding.btnMute.setOnClickListener(v -> {
            boolean nowMuted = !ttsManager.isMuted();
            ttsManager.setMuted(nowMuted);
            binding.btnMute.setImageResource(nowMuted ? R.drawable.ic_volume_mute : R.drawable.ic_volume_up);
        });
    }

    private void observeData() {
        setupTtsProgressListener();
        viewModel.allWords.observe(getViewLifecycleOwner(), words -> {
            if (words == null || words.isEmpty()) return;
            pagerAdapter.setWords(words);

            if (!initialScrollDone) {
                initialScrollDone = true;
                int startIdx = viewModel.findStartIndex(words);
                viewModel.currentIndex.setValue(startIdx);
                pagerAdapter.setCurrentPosition(startIdx);
                binding.viewPager.setCurrentItem(startIdx, false);

                PlaybackMode mode = viewModel.playbackMode.getValue();
                if (mode != null) {
                    playAudioRunnable = () -> playPageAudio(startIdx);
                    handler.postDelayed(playAudioRunnable, 500);
                    if (mode != PlaybackMode.STUDY_MODE) {
                        enableOverlayRunnable = this::enableTapOverlay;
                        handler.postDelayed(enableOverlayRunnable, 1100);
                    }
                }
            }
        });

        viewModel.visibilityMode.observe(getViewLifecycleOwner(), mode -> {
            pagerAdapter.setVisibilityMode(mode);
            updateHideToggleText(mode);
        });

        viewModel.isEnglishRevealed.observe(getViewLifecycleOwner(), revealed ->
                pagerAdapter.setRevealed(Boolean.TRUE.equals(revealed)));

        viewModel.isAutoAdvance.observe(getViewLifecycleOwner(), autoAdvance -> {
            binding.btnAutoPlay.setColorFilter(Boolean.TRUE.equals(autoAdvance)
                    ? getResources().getColor(R.color.color_primary, null) : 0);
            if (Boolean.TRUE.equals(autoAdvance)) scheduleAutoAdvance();
            else cancelPending();
        });


        viewModel.playbackMode.observe(getViewLifecycleOwner(), mode -> {
            if (mode == null) {
                binding.btnHideToggle.setVisibility(View.VISIBLE);
                binding.btnAutoPlay.setImageResource(R.drawable.ic_play);
                binding.btnMute.setVisibility(View.VISIBLE);
                viewModel.setVisibilityMode(VisibilityMode.SHOW_BOTH);
                return;
            }
            pagerAdapter.setPlaybackMode(mode);
            switch (mode) {
                case CHINESE_RECALL_ENGLISH:
                    viewModel.setVisibilityMode(VisibilityMode.HIDE_ENGLISH);
                    break;
                case ENGLISH_RECALL_CHINESE:
                    viewModel.setVisibilityMode(VisibilityMode.HIDE_CHINESE);
                    break;
                case STUDY_MODE:
                    viewModel.setVisibilityMode(VisibilityMode.SHOW_BOTH);
                    break;
                case MEMORIZE_EXAMPLE:
                    viewModel.setVisibilityMode(VisibilityMode.HIDE_ENGLISH);
                    break;
            }
            binding.btnHideToggle.setVisibility(View.GONE);
            binding.btnAutoPlay.setImageResource(R.drawable.ic_stop);
            binding.btnAutoPlay.setVisibility(View.VISIBLE);
            binding.btnMute.setVisibility(View.GONE);
            setupTtsProgressListener();
        });
    }

    // ── Playback mode audio ──────────────────────────────────────────────────

    private void stopPlayingMode() {
        ttsManager.stop();
        cancelPending();
        disableTapOverlay();
        viewModel.setPlaybackMode(null);
    }

    private void resumePlayingMode(PlaybackMode mode) {
        viewModel.setPlaybackMode(mode);
        Integer idx = viewModel.currentIndex.getValue();
        if (idx != null) {
            playAudioRunnable = () -> playPageAudio(idx);
            handler.postDelayed(playAudioRunnable, 300);
            if (mode != PlaybackMode.STUDY_MODE) {
                enableOverlayRunnable = this::enableTapOverlay;
                handler.postDelayed(enableOverlayRunnable, 900);
            }
        }
    }

    private void setupTtsProgressListener() {
        ttsManager.setProgressListener(new UtteranceProgressListener() {
            @Override public void onStart(String utteranceId) {}

            @Override
            public void onDone(String utteranceId) {
                handler.post(() -> onTtsDone(utteranceId));
            }

            @Override public void onError(String utteranceId) {}
        });
    }

    private void playPageAudio(int position) {
        List<Word> words = viewModel.allWords.getValue();
        if (words == null || position >= words.size()) return;
        Word word = words.get(position);
        PlaybackMode mode = viewModel.playbackMode.getValue();
        if (mode == null) return;

        switch (mode) {
            case CHINESE_RECALL_ENGLISH:
                ttsManager.speakChinese(getChineseText(word), "page_cn_" + word.id);
                break;
            case ENGLISH_RECALL_CHINESE:
                ttsManager.speakEnglish(word.english, prefsManager.getAccent(), "page_en_" + word.id);
                break;
            case STUDY_MODE:
                ttsManager.speakEnglish(word.english, prefsManager.getAccent(), "study_en_" + word.id);
                break;
            case MEMORIZE_EXAMPLE:
                String exChinese = (word.examples != null && !word.examples.isEmpty()
                        && word.examples.get(0).chinese != null)
                        ? word.examples.get(0).chinese : getChineseText(word);
                ttsManager.speakChinese(exChinese, "page_cn_" + word.id);
                break;
        }
    }

    private void onTtsDone(String utteranceId) {
        if (binding == null) return;
        if (utteranceId.startsWith("ex_")) {
            pagerAdapter.stopExampleAnimation();
            return;
        }
        PlaybackMode mode = viewModel.playbackMode.getValue();
        if (mode == null) return;
        List<Word> words = viewModel.allWords.getValue();
        Integer currentIdx = viewModel.currentIndex.getValue();
        if (words == null || currentIdx == null) return;

        long pauseMs = prefsManager.getAutoAdvanceSeconds() * 1000L;

        if (mode == PlaybackMode.STUDY_MODE && utteranceId.startsWith("study_en_")) {
            long wordId = parseWordId(utteranceId);
            if (currentIdx < words.size() && words.get(currentIdx).id == wordId) {
                Word word = words.get(currentIdx);
                ttsManager.speakChinese(getChineseText(word), "study_cn_" + word.id);
            }

        } else if (mode == PlaybackMode.STUDY_MODE && utteranceId.startsWith("study_cn_")) {
            long wordId = parseWordId(utteranceId);
            if (currentIdx < words.size() && words.get(currentIdx).id == wordId) {
                readyToAdvance = true;
                enableTapOverlay();
                scheduleAutoAdvanceAfterTts(pauseMs);
            }

        } else if (mode == PlaybackMode.CHINESE_RECALL_ENGLISH && utteranceId.startsWith("tap_en_")) {
            readyToAdvance = true;
            enableTapOverlay();
            scheduleAutoAdvanceAfterTts(pauseMs);

        } else if (mode == PlaybackMode.ENGLISH_RECALL_CHINESE && utteranceId.startsWith("tap_cn_")) {
            readyToAdvance = true;
            enableTapOverlay();
            scheduleAutoAdvanceAfterTts(pauseMs);

        } else if (mode == PlaybackMode.MEMORIZE_EXAMPLE && utteranceId.startsWith("tap_ex_")) {
            readyToAdvance = true;
            enableTapOverlay();
            scheduleAutoAdvanceAfterTts(pauseMs);
        }
    }

    private void onTapOverlay() {
        if (!tapOverlayEnabled) return;
        PlaybackMode mode = viewModel.playbackMode.getValue();
        if (mode == null) return;

        if (readyToAdvance) {
            disableTapOverlay();
            cancelPending();
            advanceToNextCard();
            return;
        }

        disableTapOverlay();
        cancelPending();

        List<Word> words = viewModel.allWords.getValue();
        Integer idx = viewModel.currentIndex.getValue();
        if (words == null || idx == null || idx >= words.size()) return;
        Word word = words.get(idx);

        if (mode == PlaybackMode.CHINESE_RECALL_ENGLISH) {
            viewModel.revealEnglish();
            ttsManager.speakEnglish(word.english, prefsManager.getAccent(), "tap_en_" + word.id);
        } else if (mode == PlaybackMode.ENGLISH_RECALL_CHINESE) {
            ttsManager.speakChinese(getChineseText(word), "tap_cn_" + word.id);
        } else if (mode == PlaybackMode.MEMORIZE_EXAMPLE) {
            viewModel.revealEnglish();
            if (word.examples != null && !word.examples.isEmpty()) {
                ttsManager.speakEnglish(word.examples.get(0).english, prefsManager.getAccent(), "tap_ex_" + word.id);
            } else {
                long pauseMs = prefsManager.getAutoAdvanceSeconds() * 1000L;
                readyToAdvance = true;
                enableTapOverlay();
                scheduleAutoAdvanceAfterTts(pauseMs);
            }
        }
    }

    private void advanceToNextCard() {
        if (binding == null) return;
        int total = viewModel.getWordCount();
        Integer current = viewModel.currentIndex.getValue();
        if (current == null || total == 0) return;
        int next = WordDetailViewModel.computeNextIndex(current, total);
        if (next == current) {
            // single-word list: ViewPager won't fire onPageSelected, replay manually
            PlaybackMode mode = viewModel.playbackMode.getValue();
            if (mode != null) {
                cancelPending();
                disableTapOverlay();
                playAudioRunnable = () -> playPageAudio(next);
                handler.postDelayed(playAudioRunnable, 300);
                if (mode != PlaybackMode.STUDY_MODE) {
                    enableOverlayRunnable = this::enableTapOverlay;
                    handler.postDelayed(enableOverlayRunnable, 900);
                }
            }
        } else {
            binding.viewPager.setCurrentItem(next, true);
        }
    }

    private void enableTapOverlay() {
        if (binding == null) return;
        tapOverlayEnabled = true;
        binding.tapOverlay.setVisibility(View.VISIBLE);
    }

    private void disableTapOverlay() {
        tapOverlayEnabled = false;
        readyToAdvance = false;
        if (binding != null) binding.tapOverlay.setVisibility(View.GONE);
    }

    // ── Normal (non-playback) auto-advance ───────────────────────────────────

    private void scheduleAutoAdvance() {
        cancelPending();
        int delay = viewModel.getAutoAdvanceSeconds() * 1000;
        autoAdvanceRunnable = () -> {
            int total = viewModel.getWordCount();
            if (total == 0) return;
            Integer current = viewModel.currentIndex.getValue();
            int next = WordDetailViewModel.computeNextIndex(current != null ? current : 0, total);
            binding.viewPager.setCurrentItem(next, true);
            scheduleAutoAdvance();
        };
        handler.postDelayed(autoAdvanceRunnable, delay);
    }

    private void scheduleAutoAdvanceAfterTts(long delayMs) {
        autoAdvanceAfterTtsRunnable = this::advanceToNextCard;
        handler.postDelayed(autoAdvanceAfterTtsRunnable, delayMs);
    }

    private void cancelPending() {
        if (autoAdvanceRunnable != null) {
            handler.removeCallbacks(autoAdvanceRunnable);
            autoAdvanceRunnable = null;
        }
        if (playAudioRunnable != null) {
            handler.removeCallbacks(playAudioRunnable);
            playAudioRunnable = null;
        }
        if (enableOverlayRunnable != null) {
            handler.removeCallbacks(enableOverlayRunnable);
            enableOverlayRunnable = null;
        }
        if (autoAdvanceAfterTtsRunnable != null) {
            handler.removeCallbacks(autoAdvanceAfterTtsRunnable);
            autoAdvanceAfterTtsRunnable = null;
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String getChineseText(Word word) {
        if (word.definitions == null || word.definitions.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (Definition def : word.definitions) {
            if (def.partOfSpeech != null && !def.partOfSpeech.isEmpty())
                sb.append(def.partOfSpeech).append(" ");
            sb.append(def.meaning).append("。");
        }
        return sb.toString().trim();
    }

    private long parseWordId(String utteranceId) {
        try {
            String[] parts = utteranceId.split("_");
            return Long.parseLong(parts[parts.length - 1]);
        } catch (Exception e) {
            return -1;
        }
    }

    private void showVisibilityModeSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        BottomSheetVisibilityModeBinding sheet =
                BottomSheetVisibilityModeBinding.inflate(getLayoutInflater());
        dialog.setContentView(sheet.getRoot());
        sheet.optHideChinese.setOnClickListener(v -> {
            viewModel.setVisibilityMode(VisibilityMode.HIDE_CHINESE);
            dialog.dismiss();
        });
        sheet.optHideEnglish.setOnClickListener(v -> {
            viewModel.setVisibilityMode(VisibilityMode.HIDE_ENGLISH);
            dialog.dismiss();
        });
        sheet.optShowBoth.setOnClickListener(v -> {
            viewModel.setVisibilityMode(VisibilityMode.SHOW_BOTH);
            dialog.dismiss();
        });
        sheet.optCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void updateHideToggleText(VisibilityMode mode) {
        if (mode == null) return;
        switch (mode) {
            case HIDE_ENGLISH: binding.btnHideToggle.setText(R.string.hide_word); break;
            case HIDE_CHINESE: binding.btnHideToggle.setText(R.string.hide_definition); break;
            case SHOW_BOTH: binding.btnHideToggle.setText(R.string.show_both); break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cancelPending();
        ttsManager.stop();
        binding = null;
    }
}
