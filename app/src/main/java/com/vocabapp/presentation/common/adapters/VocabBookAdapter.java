package com.vocabapp.presentation.common.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import com.vocabapp.R;
import com.vocabapp.databinding.ItemAddVocabBookBinding;
import com.vocabapp.databinding.ItemVocabBookBinding;
import com.vocabapp.domain.model.VocabBook;

import java.util.List;

public class VocabBookAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ADD = 0;
    private static final int TYPE_BOOK = 1;
    private static final int HEADER_COUNT = 1;

    public interface OnBookClickListener {
        void onBookClick(VocabBook book);
    }

    private final OnBookClickListener onBookClick;
    private final OnBookClickListener onPlayClick;
    private final AsyncListDiffer<VocabBook> differ;
    private OnBookClickListener onDeleteClick;
    private Runnable onAddClick;
    private boolean isEditMode = false;

    public VocabBookAdapter(OnBookClickListener onBookClick, OnBookClickListener onPlayClick) {
        this.onBookClick = onBookClick;
        this.onPlayClick = onPlayClick;
        this.differ = new AsyncListDiffer<>(
                new OffsetListUpdateCallback(this, HEADER_COUNT),
                new AsyncDifferConfig.Builder<>(DIFF_CALLBACK).build());
    }

    public void submitList(List<VocabBook> list) {
        differ.submitList(list);
    }

    private VocabBook getItem(int listPosition) {
        return differ.getCurrentList().get(listPosition);
    }

    public void setOnDeleteClick(OnBookClickListener listener) {
        this.onDeleteClick = listener;
    }

    public void setOnAddClick(Runnable listener) {
        this.onAddClick = listener;
    }

    public void setEditMode(boolean editMode) {
        this.isEditMode = editMode;
        notifyDataSetChanged();
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

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? TYPE_ADD : TYPE_BOOK;
    }

    @Override
    public int getItemCount() {
        return differ.getCurrentList().size() + HEADER_COUNT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ADD) {
            ItemAddVocabBookBinding binding = ItemAddVocabBookBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new AddViewHolder(binding);
        }
        ItemVocabBookBinding binding = ItemVocabBookBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new BookViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof AddViewHolder) {
            ((AddViewHolder) holder).bind();
        } else {
            ((BookViewHolder) holder).bind(getItem(position - 1));
        }
    }

    class AddViewHolder extends RecyclerView.ViewHolder {
        private final ItemAddVocabBookBinding binding;

        AddViewHolder(ItemAddVocabBookBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind() {
            binding.clAddCard.setOnClickListener(v -> {
                if (!isEditMode && onAddClick != null) onAddClick.run();
            });
        }
    }

    private static class OffsetListUpdateCallback implements ListUpdateCallback {
        private final RecyclerView.Adapter<?> adapter;
        private final int offset;

        OffsetListUpdateCallback(RecyclerView.Adapter<?> adapter, int offset) {
            this.adapter = adapter;
            this.offset = offset;
        }

        @Override
        public void onInserted(int position, int count) {
            adapter.notifyItemRangeInserted(position + offset, count);
        }

        @Override
        public void onRemoved(int position, int count) {
            adapter.notifyItemRangeRemoved(position + offset, count);
        }

        @Override
        public void onMoved(int fromPosition, int toPosition) {
            adapter.notifyItemMoved(fromPosition + offset, toPosition + offset);
        }

        @Override
        public void onChanged(int position, int count, @Nullable Object payload) {
            adapter.notifyItemRangeChanged(position + offset, count, payload);
        }
    }

    class BookViewHolder extends RecyclerView.ViewHolder {
        private final ItemVocabBookBinding binding;

        BookViewHolder(ItemVocabBookBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(VocabBook book) {
            binding.tvTitle.setText(book.bookName);
            binding.tvWordCount.setText(binding.getRoot().getContext()
                    .getString(R.string.word_count_format, book.wordCount));
            binding.cardRoot.setCardBackgroundColor(
                    binding.getRoot().getContext().getColor(R.color.card_color_5));
            binding.cardRoot.setOnClickListener(v -> {
                if (!isEditMode) onBookClick.onBookClick(book);
            });
            binding.btnPlay.setVisibility(isEditMode ? View.GONE : View.VISIBLE);
            binding.btnPlay.setOnClickListener(v -> onPlayClick.onBookClick(book));
            int deleteVis = isEditMode ? View.VISIBLE : View.GONE;
            binding.deleteOverlay.setVisibility(deleteVis);
            binding.btnDelete.setVisibility(deleteVis);
            View.OnClickListener deleteListener = v -> {
                if (onDeleteClick != null) onDeleteClick.onBookClick(book);
            };
            binding.deleteOverlay.setOnClickListener(deleteListener);
            binding.btnDelete.setOnClickListener(deleteListener);
        }
    }
}
