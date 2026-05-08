package com.vocabapp.presentation.worddetail;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.Gravity;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.vocabapp.R;
import com.vocabapp.databinding.PageWordDetailBinding;
import com.vocabapp.domain.enums.VisibilityMode;
import com.vocabapp.domain.model.Definition;
import com.vocabapp.domain.model.Example;
import com.vocabapp.domain.model.Word;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class WordDetailPagerAdapter extends RecyclerView.Adapter<WordDetailPagerAdapter.PageViewHolder> {

    public interface OnPlayClickListener {
        void onPlayWord(Word word);
        void onPlayBritish(Word word);
        void onPlayAmerican(Word word);
        void onPlayExample(Word word, int exampleIndex);
        void onRevealWord();
        void onToggleBookmark(Word word);
    }

    private List<Word> words = new ArrayList<>();
    private VisibilityMode visibilityMode = VisibilityMode.HIDE_ENGLISH;
    private boolean isRevealed = false;
    private OnPlayClickListener playClickListener;
    private int currentPosition = 0;
    private boolean currentBookmarked = false;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private ObjectAnimator exampleAnimX, exampleAnimY;
    private View exampleAnimBtn;

    public void stopExampleAnimation() {
        if (exampleAnimX != null) { exampleAnimX.cancel(); exampleAnimX = null; }
        if (exampleAnimY != null) { exampleAnimY.cancel(); exampleAnimY = null; }
        if (exampleAnimBtn != null) {
            exampleAnimBtn.setScaleX(1f);
            exampleAnimBtn.setScaleY(1f);
            exampleAnimBtn = null;
        }
    }

    private void startExampleAnimation(View btn) {
        stopExampleAnimation();
        exampleAnimBtn = btn;
        exampleAnimX = ObjectAnimator.ofFloat(btn, "scaleX", 1f, 0.75f);
        exampleAnimX.setDuration(350);
        exampleAnimX.setRepeatCount(ValueAnimator.INFINITE);
        exampleAnimX.setRepeatMode(ValueAnimator.REVERSE);
        exampleAnimY = ObjectAnimator.ofFloat(btn, "scaleY", 1f, 0.75f);
        exampleAnimY.setDuration(350);
        exampleAnimY.setRepeatCount(ValueAnimator.INFINITE);
        exampleAnimY.setRepeatMode(ValueAnimator.REVERSE);
        exampleAnimX.start();
        exampleAnimY.start();
    }

    private void postNotify(Runnable r) {
        // Post to avoid IllegalStateException when called during RecyclerView layout
        mainHandler.post(r);
    }

    public void setWords(List<Word> words) {
        this.words = words != null ? words : new ArrayList<>();
        postNotify(this::notifyDataSetChanged);
    }

    public void setVisibilityMode(VisibilityMode mode) {
        this.visibilityMode = mode;
        this.isRevealed = false;
        postNotify(this::notifyDataSetChanged);
    }

    public void setRevealed(boolean revealed) {
        this.isRevealed = revealed;
        postNotify(this::notifyDataSetChanged);
    }

    public void setPlayClickListener(OnPlayClickListener listener) {
        this.playClickListener = listener;
    }

    public void setCurrentPosition(int position) {
        this.currentPosition = position;
    }

    public void setCurrentBookmarked(boolean bookmarked) {
        this.currentBookmarked = bookmarked;
        int pos = currentPosition;
        postNotify(() -> notifyItemChanged(pos));
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
        boolean bookmarked = (position == currentPosition) ? currentBookmarked : words.get(position).isBookmarked;
        holder.bind(words.get(position), position, words.size(), bookmarked);
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

        void bind(Word word, int position, int total, boolean bookmarked) {
            binding.tvProgress.setText(
                    binding.getRoot().getContext().getString(R.string.word_progress_format, position + 1, total));

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
            int phoneticVisibility = showEnglish ? View.VISIBLE : View.GONE;
            binding.llPhoneticBritish.setVisibility(phoneticVisibility);
            binding.llPhoneticAmerican.setVisibility(phoneticVisibility);
            binding.btnPlayBritish.setOnClickListener(v -> {
                if (playClickListener != null) playClickListener.onPlayBritish(word);
            });
            binding.btnPlayAmerican.setOnClickListener(v -> {
                if (playClickListener != null) playClickListener.onPlayAmerican(word);
            });

            // Bookmark button
            int bookmarkIcon = bookmarked
                    ? android.R.drawable.btn_star_big_on
                    : android.R.drawable.btn_star_big_off;
            binding.btnBookmark.setIcon(ContextCompat.getDrawable(binding.getRoot().getContext(), bookmarkIcon));
            binding.btnBookmark.setOnClickListener(v -> {
                if (playClickListener != null) playClickListener.onToggleBookmark(word);
            });

            // Definitions
            boolean showChinese = visibilityMode != VisibilityMode.HIDE_CHINESE;
            binding.llDefinitions.removeAllViews();
            if (showChinese && word.definitions != null && !word.definitions.isEmpty()) {
                for (Definition def : word.definitions) {
                    binding.llDefinitions.addView(buildDefinitionRow(def));
                }
            }

            // Examples
            binding.llExamples.removeAllViews();
            boolean hasExamples = word.examples != null && !word.examples.isEmpty();
            binding.llExamplesHeader.setVisibility(hasExamples ? View.VISIBLE : View.GONE);
            if (hasExamples) {
                int maxExamples = Math.min(word.examples.size(), 2);
                for (int i = 0; i < maxExamples; i++) {
                    binding.llExamples.addView(buildExampleRow(word, i, showEnglish, showChinese));
                }
            }
        }

        private View buildDefinitionRow(Definition def) {
            android.content.Context ctx = binding.getRoot().getContext();
            float density = ctx.getResources().getDisplayMetrics().density;

            LinearLayout row = new LinearLayout(ctx);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            rowParams.bottomMargin = (int) (4 * density);
            row.setLayoutParams(rowParams);

            TextView pos = new TextView(ctx);
            pos.setText(def.partOfSpeech != null ? def.partOfSpeech : "");
            pos.setTextColor(ContextCompat.getColor(ctx, R.color.color_red_accent));
            pos.setTextSize(14f);
            pos.setTypeface(null, Typeface.BOLD);
            LinearLayout.LayoutParams posParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            posParams.setMarginEnd((int) (6 * density));
            pos.setLayoutParams(posParams);

            TextView meaning = new TextView(ctx);
            meaning.setText(def.meaning);
            meaning.setTextColor(ContextCompat.getColor(ctx, R.color.color_on_surface));
            meaning.setTextSize(15f);
            meaning.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            row.addView(pos);
            row.addView(meaning);
            return row;
        }

        private View buildExampleRow(Word word, int index, boolean showEnglish, boolean showChinese) {
            View row = LayoutInflater.from(binding.getRoot().getContext())
                    .inflate(R.layout.item_example, binding.llExamples, false);
            Example example = word.examples.get(index);

            TextView tvEn = row.findViewById(R.id.tvExampleEnglish);
            TextView tvCn = row.findViewById(R.id.tvExampleChinese);
            ImageButton btnPlay = row.findViewById(R.id.btnPlayExample);

            CharSequence englishText = buildMaskedEnglish(example.english, word.english, showEnglish,
                    binding.getRoot().getContext());
            tvEn.setText(englishText);

            tvCn.setText(showChinese ? example.chinese : "");
            tvCn.setVisibility(showChinese && example.chinese != null ? View.VISIBLE : View.GONE);

            int exIdx = index;
            btnPlay.setOnClickListener(v -> {
                startExampleAnimation(v);
                if (playClickListener != null) playClickListener.onPlayExample(word, exIdx);
            });

            return row;
        }

        private CharSequence buildMaskedEnglish(String sentence, String word, boolean revealed, android.content.Context ctx) {
            if (sentence == null) return "";
            if (revealed || word == null) return sentence;

            String masked = sentence.replaceAll("(?i)" + Pattern.quote(word), "***");
            SpannableString span = new SpannableString(masked);
            int start = 0;
            while (true) {
                int idx = masked.indexOf("***", start);
                if (idx < 0) break;
                span.setSpan(new ForegroundColorSpan(
                        ContextCompat.getColor(ctx, R.color.color_red_accent)),
                        idx, idx + 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                start = idx + 3;
            }
            return span;
        }
    }
}
