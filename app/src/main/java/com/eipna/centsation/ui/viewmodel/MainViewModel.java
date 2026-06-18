package com.eipna.centsation.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.eipna.centsation.data.saving.Saving;
import com.eipna.centsation.data.saving.SavingRepository;
import com.eipna.centsation.data.saving.SavingSort;
import com.eipna.centsation.data.transaction.TransactionType;
import com.eipna.centsation.util.AlarmUtil;
import com.eipna.centsation.util.PreferenceUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainViewModel extends AndroidViewModel {

    private final SavingRepository savingRepository;
    private final PreferenceUtil preferences;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<List<Saving>> savingsLiveData = new MutableLiveData<>();

    private volatile String sortCriteria;
    private volatile boolean isSortAscending;

    public MainViewModel(@NonNull Application application) {
        super(application);
        savingRepository = new SavingRepository(application);
        preferences = new PreferenceUtil(application);
        sortCriteria = preferences.getSortCriteria();
        isSortAscending = preferences.getSortOrder();
    }

    public LiveData<List<Saving>> getSavings() {
        return savingsLiveData;
    }

    public String getSortCriteria() {
        return sortCriteria;
    }

    public boolean isSortAscending() {
        return isSortAscending;
    }

    public void loadSavings() {
        executor.execute(this::loadSavingsBlocking);
    }

    private void loadSavingsBlocking() {
        List<Saving> list = savingRepository.getSavings(Saving.NOT_ARCHIVE);
        sortInPlace(list);
        savingsLiveData.postValue(list);
    }

    public void setSortCriteria(String criteria) {
        this.sortCriteria = criteria;
        executor.execute(() -> {
            preferences.setSortCriteria(criteria);
            resortCurrentList();
        });
    }

    public void setSortAscending(boolean ascending) {
        this.isSortAscending = ascending;
        executor.execute(() -> {
            preferences.setSortOrder(ascending);
            resortCurrentList();
        });
    }

    private void resortCurrentList() {
        List<Saving> current = savingsLiveData.getValue();
        if (current == null) return;
        List<Saving> sorted = new ArrayList<>(current);
        sortInPlace(sorted);
        savingsLiveData.postValue(sorted);
    }

    private void sortInPlace(List<Saving> list) {
        Comparator<Saving> comparator = comparatorFor(sortCriteria);
        if (comparator == null) return;
        if (!isSortAscending) comparator = comparator.reversed();
        list.sort(comparator);
    }

    private Comparator<Saving> comparatorFor(String criteria) {
        if (SavingSort.NAME.SORT.equals(criteria)) return Saving.SORT_NAME;
        if (SavingSort.VALUE.SORT.equals(criteria)) return Saving.SORT_VALUE;
        if (SavingSort.GOAL.SORT.equals(criteria)) return Saving.SORT_GOAL;
        if (SavingSort.DEADLINE.SORT.equals(criteria)) return Saving.SORT_DEADLINE;
        return null;
    }

    public void deleteSaving(Saving saving) {
        executor.execute(() -> {
            AlarmUtil.cancel(getApplication(), saving);
            savingRepository.delete(saving.getID());
            loadSavingsBlocking();
        });
    }

    public void archiveSaving(Saving saving) {
        Saving archived = new Saving(saving);
        archived.setIsArchived(Saving.IS_ARCHIVE);
        executor.execute(() -> {
            savingRepository.edit(archived);
            loadSavingsBlocking();
        });
    }

    public void deposit(Saving saving, double amount) {
        Saving updated = new Saving(saving);
        updated.setCurrentSaving(saving.getCurrentSaving() + amount);
        executor.execute(() -> {
            savingRepository.makeTransaction(updated, amount, TransactionType.DEPOSIT);
            loadSavingsBlocking();
        });
    }

    public boolean withdraw(Saving saving, double amount) {
        double newBalance = saving.getCurrentSaving() - amount;
        if (newBalance < 0) return false;
        Saving updated = new Saving(saving);
        updated.setCurrentSaving(newBalance);
        executor.execute(() -> {
            savingRepository.makeTransaction(updated, amount, TransactionType.WITHDRAW);
            loadSavingsBlocking();
        });
        return true;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}