package com.mowtiie.centsation.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mowtiie.centsation.data.transaction.Transaction;
import com.mowtiie.centsation.data.transaction.TransactionRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HistoryViewModel extends AndroidViewModel {

    private final TransactionRepository transactionRepository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<List<Transaction>> transactionsLiveData = new MutableLiveData<>();

    private String savingID;

    public HistoryViewModel(@NonNull Application application) {
        super(application);
        transactionRepository = new TransactionRepository(application);
    }

    public LiveData<List<Transaction>> getTransactions() {
        return transactionsLiveData;
    }

    public void setSavingID(String savingID) {
        this.savingID = savingID;
    }

    public void loadTransactions() {
        if (savingID == null) return;
        final String id = savingID;
        executor.execute(() -> transactionsLiveData.postValue(transactionRepository.get(id)));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}