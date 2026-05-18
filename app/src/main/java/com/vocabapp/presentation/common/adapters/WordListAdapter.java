package com.vocabapp.presentation.common.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.vocabapp.databinding.ItemWordBinding;
import com.vocabapp.domain.enums.VisibilityMode;
import com.vocabapp.domain.model.Word;

import java.util.HashSet;
import java.util.Set;

public class WordListAdapter extends ListAdapter<Word, WordListAdapter.ViewHolder> {

    public interface OnWordClickListener {
        void onWordClick(Word word);
    }

    public interface OnWordSelectListener {
        void onWordSelect(long wordId);
    }

    private final OnWordClickListener onWordClick;
    private final OnWordSelectListener onWordSelect;
    private boolean isBatchMode = false;
    private Set<Long> selectedIds = new HashSet<>();
    private VisibilityMode visibilityMode = VisibilityMode.SHOW_BOTH;

    public WordListAdapter(OnWordClickListener onWordClick, OnWordSelectListener onWordSelect) {
        super(DIFF_CALLBACK);
        this.onWordClick = onWordClick;
        this.onWordSelect = onWordSelect;
    }

    private static final DiffUtil.ItemCallback<Word> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Word>() {
                @Override
                public boolean areItemsTheSame(@NonNull Word oldItem, @NonNull Word newItem) {
                    return oldItem.id == newItem.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull Word oldItem, @NonNull Word newItem) {
                    return oldItem.english.equals(newItem.english)
                            && oldItem.isBookmarked == newItem.isBookmarked;
                }
            };

    public void setBatchMode(boolean batchMode) {
        this.isBatchMode = batchMode;
        notifyDataSetChanged();
    }

    public void setSelectedIds(Set<Long> ids) {
        this.selectedIds = ids != null ? ids : new HashSet<>();
        notifyDataSetChanged();
    }

    public void setVisibilityMode(VisibilityMode mode) {
        this.visibilityMode = mode;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWordBinding binding = ItemWordBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemWordBinding binding;

        ViewHolder(ItemWordBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Word word) {
            // English word — hide if mode is HIDE_ENGLISH
            if (visibilityMode == VisibilityMode.HIDE_ENGLISH) {
                binding.tvEnglish.setText("***");
            } else {
                binding.tvEnglish.setText(word.english);
            }

            // Chinese definition — hide if mode is HIDE_CHINESE
            if (visibilityMode == VisibilityMode.HIDE_CHINESE) {
                binding.tvChinese.setVisibility(View.INVISIBLE);
            } else {
                binding.tvChinese.setVisibility(View.VISIBLE);
                binding.tvChinese.setText(word.chineseDefinition);
            }

            // Batch mode checkbox
            if (isBatchMode) {
                binding.checkbox.setVisibility(View.VISIBLE);
                binding.checkbox.setChecked(selectedIds.contains(word.id));
                binding.checkbox.setOnClickListener(v -> onWordSelect.onWordSelect(word.id));
            } else {
                binding.checkbox.setVisibility(View.GONE);
            }

            binding.ivBookmark.setVisibility(word.isBookmarked ? View.VISIBLE : View.GONE);

            binding.getRoot().setOnClickListener(v -> {
                if (isBatchMode) onWordSelect.onWordSelect(word.id);
                else onWordClick.onWordClick(word);
            });
        }
    }
}
