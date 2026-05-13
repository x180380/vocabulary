package com.vocabapp.presentation.common.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vocabapp.databinding.ItemWordGroupBinding;
import com.vocabapp.domain.model.Word;

import java.util.ArrayList;
import java.util.List;

public class WordGroupAdapter extends RecyclerView.Adapter<WordGroupAdapter.ViewHolder> {

    public interface OnGroupClickListener {
        void onGroupClick(int groupIndex);
    }

    public interface OnGroupPlayListener {
        void onGroupPlay(int groupIndex);
    }

    private List<Word> allWords = new ArrayList<>();
    private int groupSize = 100;
    private OnGroupClickListener clickListener;
    private OnGroupPlayListener playListener;

    public void setWords(List<Word> words, int groupSize) {
        this.allWords = words != null ? words : new ArrayList<>();
        this.groupSize = groupSize > 0 ? groupSize : 100;
        notifyDataSetChanged();
    }

    public void setOnGroupClickListener(OnGroupClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnGroupPlayListener(OnGroupPlayListener listener) {
        this.playListener = listener;
    }

    public Word getFirstWordOfGroup(int groupIndex) {
        int startIndex = groupIndex * groupSize;
        if (startIndex >= 0 && startIndex < allWords.size()) return allWords.get(startIndex);
        return null;
    }

    @Override
    public int getItemCount() {
        if (allWords.isEmpty()) return 0;
        return (allWords.size() + groupSize - 1) / groupSize;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWordGroupBinding binding = ItemWordGroupBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int startIndex = position * groupSize;
        int endIndex = Math.min(startIndex + groupSize, allWords.size());
        holder.bind(position + 1, startIndex + 1, endIndex, position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemWordGroupBinding binding;

        ViewHolder(ItemWordGroupBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(int groupNumber, int startWord, int endWord, int groupIndex) {
            binding.tvGroupTitle.setText("第" + groupNumber + "组");
            binding.tvGroupRange.setText("第" + startWord + "-" + endWord + "个单词");
            binding.getRoot().setOnClickListener(v -> {
                if (clickListener != null) clickListener.onGroupClick(groupIndex);
            });
            binding.btnPlayGroup.setOnClickListener(v -> {
                if (playListener != null) playListener.onGroupPlay(groupIndex);
            });
        }
    }
}
