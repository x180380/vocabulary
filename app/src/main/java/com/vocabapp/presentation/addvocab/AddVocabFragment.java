package com.vocabapp.presentation.addvocab;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.vocabapp.databinding.FragmentAddVocabBinding;
import com.vocabapp.presentation.common.adapters.LibraryVocabAdapter;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AddVocabFragment extends Fragment {

    private FragmentAddVocabBinding binding;
    private AddVocabViewModel viewModel;
    private LibraryVocabAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAddVocabBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AddVocabViewModel.class);

        adapter = new LibraryVocabAdapter(book -> viewModel.addToMyVocab(book.id));
        binding.rvLibrary.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvLibrary.setAdapter(adapter);

        binding.btnBack.setOnClickListener(v ->
                Navigation.findNavController(requireView()).navigateUp());

        viewModel.availableBooks.observe(getViewLifecycleOwner(), books -> {
            adapter.submitList(books);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
