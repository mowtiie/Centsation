package com.eipna.centsation.ui.activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.eipna.centsation.R;
import com.eipna.centsation.data.Database;
import com.eipna.centsation.data.saving.Saving;
import com.eipna.centsation.data.saving.SavingOperation;
import com.eipna.centsation.databinding.ActivityArchiveBinding;
import com.eipna.centsation.ui.adapters.SavingAdapter;
import com.eipna.centsation.ui.viewmodel.ArchiveViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.shape.MaterialShapeDrawable;

import java.util.List;

public class ArchiveActivity extends BaseActivity implements SavingAdapter.Listener {

    private ActivityArchiveBinding binding;
    private ArchiveViewModel viewModel;
    private SavingAdapter savingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityArchiveBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        Drawable appBarDrawable = MaterialShapeDrawable.createWithElevationOverlay(this);
        binding.appBar.setStatusBarForeground(appBarDrawable);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        viewModel = new ViewModelProvider(this).get(ArchiveViewModel.class);

        savingAdapter = new SavingAdapter(this, this);
        binding.savingList.setLayoutManager(new LinearLayoutManager(this));
        binding.savingList.setAdapter(savingAdapter);

        viewModel.getSavings().observe(this, this::onSavingsChanged);
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadSavings();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    private void onSavingsChanged(List<Saving> updated) {
        savingAdapter.submitList(updated);

        boolean empty = updated.isEmpty();
        binding.emptyIndicator.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.savingList.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onClick(int position) {
        Saving selectedSaving = savingAdapter.getSavingAt(position);
        Intent editIntent = new Intent(this, EditActivity.class);
        editIntent.putExtra(Database.COLUMN_SAVING_ID, selectedSaving.getID());
        startActivity(editIntent);
    }

    @Override
    public void onOperationClick(SavingOperation operation, int position) {
        Saving selectedSaving = savingAdapter.getSavingAt(position);
        if (operation.equals(SavingOperation.UNARCHIVE)) viewModel.unarchiveSaving(selectedSaving);
        if (operation.equals(SavingOperation.SHARE)) showShareIntent(selectedSaving.getNotes());
        if (operation.equals(SavingOperation.DELETE)) showDeleteDialog(selectedSaving);
        if (operation.equals(SavingOperation.HISTORY)) showHistoryActivity(selectedSaving);
    }

    private void showShareIntent(String notes) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_TEXT, notes);
        startActivity(Intent.createChooser(sendIntent, null));
    }

    private void showDeleteDialog(Saving saving) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_title_delete_saving)
                .setIcon(R.drawable.ic_delete_forever)
                .setMessage(R.string.dialog_message_delete_saving)
                .setNegativeButton(R.string.dialog_button_cancel, null)
                .setPositiveButton(R.string.dialog_button_delete, (dialogInterface, i) ->
                        viewModel.deleteSaving(saving));
        builder.create().show();
    }

    private void showHistoryActivity(Saving saving) {
        Intent historyIntent = new Intent(this, HistoryActivity.class);
        historyIntent.putExtra(Database.COLUMN_SAVING_ID, saving.getID());
        startActivity(historyIntent);
    }
}