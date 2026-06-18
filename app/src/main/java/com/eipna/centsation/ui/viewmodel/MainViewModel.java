package com.eipna.centsation.ui.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.eipna.centsation.data.saving.Saving;
import com.eipna.centsation.data.saving.SavingRepository;
import com.eipna.centsation.data.saving.SavingSort;
import com.eipna.centsation.data.transaction.TransactionType;
import com.eipna.centsation.util.PreferenceUtil;

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private final SavingRepository savingRepository;
    private final PreferenceUtil preferences;

    private final MutableLiveData<List<Saving>> savingsLiveData = new MutableLiveData<>(new ArrayList<>());

    private String sortCriteria;
    private boolean isSortAscending;

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
        List<Saving> list = savingRepository.getSavings(Saving.NOT_ARCHIVE);
        sortInPlace(list);
        savingsLiveData.setValue(list);
    }

    public void setSortCriteria(String criteria) {
        this.sortCriteria = criteria;
        preferences.setSortCriteria(criteria);
        applySort();
    }

    public void setSortAscending(boolean ascending) {
        this.isSortAscending = ascending;
        preferences.setSortOrder(ascending);
        applySort();
    }

    private void applySort() {
        List<Saving> current = savingsLiveData.getValue();
        if (current == null) return;
        List<Saving> sorted = new ArrayList<>(current);
        sortInPlace(sorted);
        savingsLiveData.setValue(sorted);
    }

    private void sortInPlace(List<Saving> list) {
        Comparator<Saving> comparator = comparatorFor(sortCriteria);
        if (comparator == null) return;
        if (!isSortAscending) comparator = comparator.reversed();
        list.sort(comparator);
    }

    private Comparator<Saving> comparatorFor(String criteria) {
        if (SavingSort.NAME.SORT.equals(criteria)) {
            return Saving.SORT_NAME;
        } else if (SavingSort.VALUE.SORT.equals(criteria)) {
            return Saving.SORT_VALUE;
        } else if (SavingSort.GOAL.SORT.equals(criteria)) {
            return Saving.SORT_GOAL;
        } else if (SavingSort.DEADLINE.SORT.equals(criteria)) {
            return Saving.SORT_DEADLINE;
        }
        return null;
    }

    public void deleteSaving(String savingID) {
        savingRepository.delete(savingID);
        loadSavings();
    }

    public void archiveSaving(Saving saving) {
        saving.setIsArchived(Saving.IS_ARCHIVE);
        savingRepository.edit(saving);
        loadSavings();
    }

    public void deposit(Saving saving, double amount) {
        saving.setCurrentSaving(saving.getCurrentSaving() + amount);
        savingRepository.makeTransaction(saving, amount, TransactionType.DEPOSIT);
        loadSavings();
    }

    public boolean withdraw(Saving saving, double amount) {
        double newBalance = saving.getCurrentSaving() - amount;
        if (newBalance < 0) return false;
        saving.setCurrentSaving(newBalance);
        savingRepository.makeTransaction(saving, amount, TransactionType.WITHDRAW);
        loadSavings();
        return true;
    }
}

