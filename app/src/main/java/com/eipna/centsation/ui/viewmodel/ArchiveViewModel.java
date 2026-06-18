package com.eipna.centsation.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.eipna.centsation.data.saving.Saving;
import com.eipna.centsation.data.saving.SavingRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ArchiveViewModel extends AndroidViewModel {

    private final SavingRepository savingRepository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<List<Saving>> savingsLiveData = new MutableLiveData<>();

    public ArchiveViewModel(@NonNull Application application) {
        super(application);
        savingRepository = new SavingRepository(application);
    }

    public LiveData<List<Saving>> getSavings() {
        return savingsLiveData;
    }

    public void loadSavings() {
        executor.execute(this::loadSavingsBlocking);
    }

    private void loadSavingsBlocking() {
        savingsLiveData.postValue(savingRepository.getSavings(Saving.IS_ARCHIVE));
    }

    public void deleteSaving(String savingID) {
        executor.execute(() -> {
            savingRepository.delete(savingID);
            loadSavingsBlocking();
        });
    }

    public void unarchiveSaving(Saving saving) {
        Saving unarchived = copyOf(saving);
        unarchived.setIsArchived(Saving.NOT_ARCHIVE);
        executor.execute(() -> {
            savingRepository.edit(unarchived);
            loadSavingsBlocking();
        });
    }

    private Saving copyOf(Saving source) {
        Saving copy = new Saving();
        copy.setID(source.getID());
        copy.setName(source.getName());
        copy.setCurrentSaving(source.getCurrentSaving());
        copy.setGoal(source.getGoal());
        copy.setNotes(source.getNotes());
        copy.setIsArchived(source.getIsArchived());
        copy.setDeadline(source.getDeadline());
        return copy;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}