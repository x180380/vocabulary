package com.vocabapp.presentation.settings;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.vocabapp.data.local.preferences.UserPreferencesManager;
import com.vocabapp.domain.enums.PronunciationAccent;
import com.vocabapp.domain.enums.VisibilityMode;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SettingsViewModel extends ViewModel {

    private final UserPreferencesManager prefsManager;

    public final MutableLiveData<VisibilityMode> visibilityMode;
    public final MutableLiveData<PronunciationAccent> accent;
    public final MutableLiveData<Integer> autoAdvanceSeconds;
    public final MutableLiveData<Integer> groupSize;

    @Inject
    public SettingsViewModel(UserPreferencesManager prefsManager) {
        this.prefsManager = prefsManager;
        this.visibilityMode = new MutableLiveData<>(prefsManager.getVisibilityMode());
        this.accent = new MutableLiveData<>(prefsManager.getAccent());
        this.autoAdvanceSeconds = new MutableLiveData<>(prefsManager.getAutoAdvanceSeconds());
        this.groupSize = new MutableLiveData<>(prefsManager.getGroupSize());
    }

    public void setVisibilityMode(VisibilityMode mode) {
        visibilityMode.setValue(mode);
        prefsManager.setVisibilityMode(mode);
    }

    public void setAccent(PronunciationAccent newAccent) {
        accent.setValue(newAccent);
        prefsManager.setAccent(newAccent);
    }

    public void setAutoAdvanceSeconds(int seconds) {
        autoAdvanceSeconds.setValue(seconds);
        prefsManager.setAutoAdvanceSeconds(seconds);
    }

    public void setGroupSize(int size) {
        groupSize.setValue(size);
        prefsManager.setGroupSize(size);
    }
}
