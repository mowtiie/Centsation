package com.eipna.centsation.ui.activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.eipna.centsation.data.Database;
import com.eipna.centsation.data.transaction.Transaction;
import com.eipna.centsation.databinding.ActivityHistoryBinding;
import com.eipna.centsation.ui.adapters.TransactionAdapter;
import com.eipna.centsation.ui.viewmodel.HistoryViewModel;

import java.util.List;

public class HistoryActivity extends CentsationActivity {

    private ActivityHistoryBinding binding;
    private HistoryViewModel viewModel;
    private TransactionAdapter transactionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        String selectedSavingID = getIntent().getStringExtra(Database.COLUMN_SAVING_ID);

        viewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
        viewModel.setSavingID(selectedSavingID);

        transactionAdapter = new TransactionAdapter(this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(transactionAdapter);

        viewModel.getTransactions().observe(this, this::onTransactionsChanged);
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadTransactions();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    private void onTransactionsChanged(List<Transaction> updated) {
        transactionAdapter.submitList(updated);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) finish();
        return true;
    }
}