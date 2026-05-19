package com.vocabapp.presentation.common.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.vocabapp.R;
import com.vocabapp.databinding.ItemLibraryBookBinding;
import com.vocabapp.domain.model.VocabBook;

public class LibraryVocabAdapter extends ListAdapter<VocabBook, LibraryVocabAdapter.ViewHolder> {

    public interface OnAddListener {
        void onAdd(VocabBook book);
    }

    private final OnAddListener onAdd;

    public LibraryVocabAdapter(OnAddListener onAdd) {
        super(DIFF_CALLBACK);
        this.onAdd = onAdd;
    }

    private static final DiffUtil.ItemCallback<VocabBook> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<VocabBook>() {
                @Override
                public boolean areItemsTheSame(@NonNull VocabBook a, @NonNull VocabBook b) {
                    return a.id == b.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull VocabBook a, @NonNull VocabBook b) {
                    return a.bookName.equals(b.bookName) && a.wordCount == b.wordCount
                            && a.isMine == b.isMine;
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLibraryBookBinding binding = ItemLibraryBookBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemLibraryBookBinding binding;

        ViewHolder(ItemLibraryBookBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(VocabBook book) {
            binding.tvBookName.setText(book.bookName);
            binding.tvWordCount.setText(binding.getRoot().getContext()
                    .getString(R.string.word_count_format, book.wordCount));
            if (book.isMine) {
                binding.btnAdd.setText("已添加");
                binding.btnAdd.setEnabled(false);
                binding.btnAdd.setAlpha(0.4f);
                binding.getRoot().setOnClickListener(null);
                binding.getRoot().setClickable(false);
            } else {
                binding.btnAdd.setText("添加");
                binding.btnAdd.setEnabled(true);
                binding.btnAdd.setAlpha(1f);
                binding.btnAdd.setOnClickListener(v -> onAdd.onAdd(book));
                binding.getRoot().setOnClickListener(v -> onAdd.onAdd(book));
                binding.getRoot().setClickable(true);
            }
        }
    }
}
