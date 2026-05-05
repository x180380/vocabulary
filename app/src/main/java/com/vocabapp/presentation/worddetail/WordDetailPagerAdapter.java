package com.vocabapp.presentation.worddetail;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vocabapp.databinding.PageWordDetailBinding;
import com.vocabapp.domain.enums.VisibilityMode;
import com.vocabapp.domain.model.Definition;
import com.vocabapp.domain.model.Example;
import com.vocabapp.domain.model.Word;

import java.util.ArrayList;
import java.util.List;

public class WordDetailPagerAdapter extends RecyclerView.Adapter<WordDetailPagerAdapter.PageViewHolder> {

    public interface OnPlayClickListener {
        void onPlayWord(Word word);
        void onPlayExample(Word word, int exampleIndex);
        void onRevealWord();
        void onToggleBookmark(Word word);
    }

    private List<Word> words = new ArrayList<>();
    private VisibilityMode visibilityMode = VisibilityMode.HIDE_ENGLISH;
    private boolean isRevealed = false;
    private OnPlayClickListener playClickListener;

    public void setWords(List<Word> words) {
        this.words = words != null ? words : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setVisibilityMode(VisibilityMode mode) {
        this.visibilityMode = mode;
        this.isRevealed = false;
        notifyDataSetChanged();
    }

    public void setRevealed(boolean revealed) {
        this.isRevealed = revealed;
        notifyDataSetChanged();
    }

    public void setPlayClickListener(OnPlayClickListener listener) {
        this.playClickListener = listener;
    }

    @NonNull
    @Override
    public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        PageWordDetailBinding binding = PageWordDetailBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new PageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
        holder.bind(words.get(position));
    }

    @Override
    public int getItemCount() {
        return words.size();
    }

    class PageViewHolder extends RecyclerView.ViewHolder {
        private final PageWordDetailBinding binding;

        PageViewHolder(PageWordDetailBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Word word) {
            boolean showEnglish = visibilityMode != VisibilityMode.HIDE_ENGLISH || isRevealed;
            if (showEnglish) {
                binding.tvEnglish.setText(word.english);
                binding.tvTapToReveal.setVisibility(View.GONE);
                binding.tvEnglish.setVisibility(View.VISIBLE);
            } else {
                binding.tvEnglish.setVisibility(View.GONE);
                binding.tvTapToReveal.setVisibility(View.VISIBLE);
                binding.tvTapToReveal.setOnClickListener(v -> {
                    if (playClickListener != null) playClickListener.onRevealWord();
                });
            }

            if (word.phonetics != null) {
                binding.tvPhoneticBritish.setText("英 " + word.phonetics.british);
                binding.tvPhoneticAmerican.setText("美 " + word.phonetics.american);
            }

            boolean showChinese = visibilityMode != VisibilityMode.HIDE_CHINESE;
            binding.tvDefinitions.setVisibility(showChinese ? View.VISIBLE : View.GONE);

            if (showChinese && word.definitions != null && !word.definitions.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (Definition def : word.definitions) {
                    sb.append(def.partOfSpeech).append(" ").append(def.meaning).append("\n");
                }
                binding.tvDefinitions.setText(sb.toString().trim());
            }

            binding.llExamples.removeAllViews();
            if (word.examples != null && !word.examples.isEmpty()) {
                int maxExamples = Math.min(word.examples.size(), 2);
                for (int i = 0; i < maxExamples; i++) {
                    Example example = word.examples.get(i);
                    View exView = LayoutInflater.from(binding.getRoot().getContext())
                            .inflate(android.R.layout.simple_list_item_2, binding.llExamples, false);
                    android.widget.TextView text1 = exView.findViewById(android.R.id.text1);
                    android.widget.TextView text2 = exView.findViewById(android.R.id.text2);
                    text1.setText(maskWord(example.english, word.english, showEnglish));
                    text2.setText(showChinese ? example.chinese : "");
                    text1.setTextSize(13);
                    text2.setTextSize(12);
                    binding.llExamples.addView(exView);
                }
            }

            binding.btnPlayWord.setOnClickListener(v -> {
                if (playClickListener != null) playClickListener.onPlayWord(word);
            });

            binding.btnBookmark.setImageResource(word.isBookmarked
                    ? android.R.drawable.btn_star_big_on
                    : android.R.drawable.btn_star_big_off);
            binding.btnBookmark.setOnClickListener(v -> {
                if (playClickListener != null) playClickListener.onToggleBookmark(word);
            });
        }

        private String maskWord(String sentence, String word, boolean revealed) {
            if (revealed || sentence == null || word == null) return sentence;
            return sentence.replaceAll("(?i)" + java.util.regex.Pattern.quote(word), "***");
        }
    }
}
