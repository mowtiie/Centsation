package com.eipna.centsation.ui.activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.MenuCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.eipna.centsation.R;
import com.eipna.centsation.data.Currency;
import com.eipna.centsation.data.Database;
import com.eipna.centsation.data.saving.Saving;
import com.eipna.centsation.data.saving.SavingOperation;
import com.eipna.centsation.data.saving.SavingSort;
import com.eipna.centsation.databinding.ActivityMainBinding;
import com.eipna.centsation.ui.adapters.SavingAdapter;
import com.eipna.centsation.ui.viewmodel.MainViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.eipna.centsation.util.CrashReporter;

import java.util.List;
import java.util.Objects;

public class MainActivity extends CentsationActivity implements SavingAdapter.Listener {

    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private SavingAdapter savingAdapter;

    private final ActivityResultLauncher<Intent> saveCrashLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                            return;
                        }

                        Uri uri = result.getData().getData();
                        if (uri == null) {
                            return;
                        }

                        if (CrashReporter.writeReportToUri(this, uri)) {
                            Toast.makeText(this, R.string.crash_save_toast_success, Toast.LENGTH_SHORT).show();
                            CrashReporter.deleteReport(this);
                        } else {
                            Toast.makeText(this, R.string.crash_save_toast_failure, Toast.LENGTH_SHORT).show();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        Drawable appBarDrawable = MaterialShapeDrawable.createWithElevationOverlay(this);
        binding.appBar.setStatusBarForeground(appBarDrawable);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        savingAdapter = new SavingAdapter(this, this);
        binding.savingList.setLayoutManager(new LinearLayoutManager(this));
        binding.savingList.setAdapter(savingAdapter);

        viewModel.getSavings().observe(this, this::onSavingsChanged);

        binding.createSaving.setOnClickListener(v -> startActivity(new Intent(this, CreateActivity.class)));

        if (savedInstanceState == null) {
            CrashReporter.showDialogIfPending(this, saveCrashLauncher);
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuCompat.setGroupDividerEnabled(menu, true);
        loadSortingMenu(menu);
        return true;
    }

    private void loadSortingMenu(Menu menu) {
        String criteria = viewModel.getSortCriteria();
        if (SavingSort.NAME.SORT.equals(criteria)) {
            menu.findItem(R.id.sort_name).setChecked(true);
        } else if (SavingSort.VALUE.SORT.equals(criteria)) {
            menu.findItem(R.id.sort_value).setChecked(true);
        } else if (SavingSort.GOAL.SORT.equals(criteria)) {
            menu.findItem(R.id.sort_goal).setChecked(true);
        } else if (SavingSort.DEADLINE.SORT.equals(criteria)) {
            menu.findItem(R.id.sort_deadline).setChecked(true);
        }

        if (viewModel.isSortAscending()) {
            menu.findItem(R.id.sort_ascending).setChecked(true);
        } else {
            menu.findItem(R.id.sort_descending).setChecked(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.archive) {
            startActivity(new Intent(this, ArchiveActivity.class));
            return true;
        }
        if (id == R.id.settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        if (id == R.id.about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }

        if (id == R.id.sort_name) return applySortCriteria(item, SavingSort.NAME.SORT);
        if (id == R.id.sort_value) return applySortCriteria(item, SavingSort.VALUE.SORT);
        if (id == R.id.sort_goal) return applySortCriteria(item, SavingSort.GOAL.SORT);
        if (id == R.id.sort_deadline) return applySortCriteria(item, SavingSort.DEADLINE.SORT);

        if (id == R.id.sort_ascending) return applySortOrder(item, true);
        if (id == R.id.sort_descending) return applySortOrder(item, false);

        return super.onOptionsItemSelected(item);
    }

    private boolean applySortCriteria(MenuItem item, String criteria) {
        item.setChecked(true);
        viewModel.setSortCriteria(criteria);
        return true;
    }

    private boolean applySortOrder(MenuItem item, boolean ascending) {
        item.setChecked(true);
        viewModel.setSortAscending(ascending);
        return true;
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
        switch (operation) {
            case DELETE:      showDeleteDialog(selectedSaving); break;
            case SHARE:       showShareIntent(selectedSaving.getNotes()); break;
            case TRANSACTION: showTransactionDialog(selectedSaving); break;
            case ARCHIVE:     viewModel.archiveSaving(selectedSaving); break;
            case HISTORY:     showHistoryActivity(selectedSaving); break;
        }
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

    private void showShareIntent(String notes) {
        Intent sendIntent = new Intent();
        sendIntent.setType("text/plain");
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, notes);
        startActivity(Intent.createChooser(sendIntent, null));
    }

    private void showTransactionDialog(Saving selectedSaving) {
        View transactionDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_saving_transaction, null, false);
        String currentCurrencySymbol = Currency.getSymbol(preferenceUtil.getCurrency());

        TextInputLayout amountLayout = transactionDialogView.findViewById(R.id.field_saving_amount_layout);
        TextInputEditText amountInput = transactionDialogView.findViewById(R.id.field_saving_amount_text);

        MaterialRadioButton depositOption = transactionDialogView.findViewById(R.id.radio_button_deposit);
        MaterialRadioButton withdrawOption = transactionDialogView.findViewById(R.id.radio_button_withdraw);

        amountLayout.setPrefixText(currentCurrencySymbol);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_title_create_transaction)
                .setIcon(R.drawable.ic_add_circle)
                .setView(transactionDialogView)
                .setNegativeButton(R.string.dialog_button_cancel, null)
                .setPositiveButton(R.string.dialog_button_submit, null);

        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }

        dialog.setOnShowListener(dialogInterface -> {
            amountInput.requestFocus();

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String amountText = Objects.requireNonNull(amountInput.getText()).toString();

                if (amountText.isEmpty()) {
                    amountLayout.setError(getString(R.string.field_error_required));
                    return;
                }

                double amount;
                try {
                    amount = Double.parseDouble(amountText);
                } catch (NumberFormatException e) {
                    amountLayout.setError(getString(R.string.field_error_invalid_number));
                    return;
                }

                if (depositOption.isChecked()) {
                    viewModel.deposit(selectedSaving, amount);
                    dialog.dismiss();
                } else if (withdrawOption.isChecked()) {
                    if (!viewModel.withdraw(selectedSaving, amount)) {
                        amountLayout.setError(getString(R.string.field_error_negative_saving));
                        return;
                    }
                    dialog.dismiss();
                }
            });
        });
        dialog.show();
    }

    private void showHistoryActivity(Saving saving) {
        Intent historyIntent = new Intent(this, HistoryActivity.class);
        historyIntent.putExtra(Database.COLUMN_SAVING_ID, saving.getID());
        startActivity(historyIntent);
    }
}