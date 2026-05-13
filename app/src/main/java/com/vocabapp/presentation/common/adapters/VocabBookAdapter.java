package com.vocabapp.presentation.common.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.vocabapp.R;
import com.vocabapp.databinding.ItemVocabBookBinding;
import com.vocabapp.domain.model.VocabBook;
import com.vocabapp.presentation.common.ColorUtils;

public class VocabBookAdapter extends ListAdapter<VocabBook, VocabBookAdapter.ViewHolder> {

    public interface OnBookClickListener {
        void onBookClick(VocabBook book);
    }

    private final OnBookClickListener onBookClick;
    private final OnBookClickListener onPlayClick;

    public VocabBookAdapter(OnBookClickListener onBookClick, OnBookClickListener onPlayClick) {
        super(DIFF_CALLBACK);
        this.onBookClick = onBookClick;
        this.onPlayClick = onPlayClick;
    }

    private static final DiffUtil.ItemCallback<VocabBook> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<VocabBook>() {
                @Override
                public boolean areItemsTheSame(@NonNull VocabBook oldItem, @NonNull VocabBook newItem) {
                    return oldItem.id == newItem.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull VocabBook oldItem, @NonNull VocabBook newItem) {
                    return oldItem.bookName.equals(newItem.bookName)
                            && oldItem.wordCount == newItem.wordCount
                            && oldItem.colorIndex == newItem.colorIndex;
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemVocabBookBinding binding = ItemVocabBookBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemVocabBookBinding binding;

        ViewHolder(ItemVocabBookBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(VocabBook book) {
            binding.tvTitle.setText(book.bookName);
            binding.tvWordCount.setText(binding.getRoot().getContext()
                    .getString(R.string.word_count_format, book.wordCount));
            binding.cardRoot.setCardBackgroundColor(
                    ColorUtils.getCardColor(binding.getRoot().getContext(), book.colorIndex));
            binding.cardRoot.setOnClickListener(v -> onBookClick.onBookClick(book));
            binding.btnPlay.setOnClickListener(v -> onPlayClick.onBookClick(book));
        }
    }
}
