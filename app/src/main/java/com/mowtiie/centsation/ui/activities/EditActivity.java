package com.mowtiie.centsation.ui.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.mowtiie.centsation.R;
import com.mowtiie.centsation.data.Currency;
import com.mowtiie.centsation.data.Database;
import com.mowtiie.centsation.data.saving.Saving;
import com.mowtiie.centsation.data.saving.SavingRepository;
import com.mowtiie.centsation.databinding.ActivityEditBinding;
import com.mowtiie.centsation.util.AlarmUtil;
import com.mowtiie.centsation.util.DateParser;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;

import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class EditActivity extends CentsationActivity {

    private ActivityEditBinding binding;
    private SavingRepository savingRepository;
    private Saving currentSaving;

    private final ActivityResultLauncher<String> requestNotificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    showDeadlineDialog();
                } else {
                    Toast.makeText(this, R.string.toast_notification_permission_denied, Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditBinding.inflate(getLayoutInflater());
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

        String savingIDExtra = getIntent().getStringExtra(Database.COLUMN_SAVING_ID);

        savingRepository = new SavingRepository(this);
        currentSaving = savingRepository.getSaving(savingIDExtra);

        String selectedCurrencySymbol = Currency.getSymbol(preferences.getCurrency());
        binding.fieldSavingGoalLayout.setPrefixText(selectedCurrencySymbol);

        binding.fieldSavingNameText.setText(currentSaving.getName());
        binding.fieldSavingGoalText.setText(String.format(Locale.getDefault(), "%.2f", currentSaving.getGoal()));
        binding.fieldSavingNotesText.setText(currentSaving.getNotes());
        binding.fieldSavingDeadlineLayout.setEndIconVisible(false);

        if (currentSaving.getDeadline() != AlarmUtil.NO_ALARM) {
            String deadlineFormat = preferences.getDeadlineFormat();
            binding.fieldSavingDeadlineLayout.setEndIconVisible(true);
            binding.fieldSavingDeadlineText.setText(DateParser.getStringDate(currentSaving.getDeadline(), deadlineFormat));
        }

        binding.fieldSavingDeadlineText.setOnClickListener(v -> hasNotificationPermission());
        binding.fieldSavingDeadlineLayout.setEndIconOnClickListener(v -> {
            binding.fieldSavingDeadlineText.setText("");
            binding.fieldSavingDeadlineLayout.setEndIconVisible(false);
        });
    }

    private void hasNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                showDeadlineDialog();
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)) {
                Snackbar.make(binding.getRoot(), getString(R.string.snack_bar_permission_notifications), Snackbar.LENGTH_SHORT)
                        .setAction(R.string.dialog_button_grant, v -> {
                            Intent notificationSettingsIntent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                            notificationSettingsIntent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                            startActivity(notificationSettingsIntent);
                        }).show();
            } else {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            showDeadlineDialog();
        }
    }

    private void showDeadlineDialog() {
        CalendarConstraints.Builder calendarConstraints = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now());

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setCalendarConstraints(calendarConstraints.build())
                .setTitleText("Select deadline date")
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(selection);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            currentSaving.setDeadline(calendar.getTimeInMillis());
            String deadlineFormat = preferences.getDeadlineFormat();

            binding.fieldSavingDeadlineText.setText(DateParser.getStringDate(selection, deadlineFormat));
            binding.fieldSavingDeadlineLayout.setEndIconVisible(true);
        });
        datePicker.show(getSupportFragmentManager(), null);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) finish();
        if (item.getItemId() == R.id.save) editSaving();
        return true;
    }

    private void editSaving() {
        String nameText = Objects.requireNonNull(binding.fieldSavingNameText.getText()).toString();
        String goalText = Objects.requireNonNull(binding.fieldSavingGoalText.getText()).toString();
        String notesText = Objects.requireNonNull(binding.fieldSavingNotesText.getText()).toString();
        String deadlineText = Objects.requireNonNull(binding.fieldSavingDeadlineText.getText()).toString();

        binding.fieldSavingNameLayout.setError(
                nameText.isEmpty() ? getString(R.string.field_error_required) : null);
        binding.fieldSavingGoalLayout.setError(
                goalText.isEmpty() ? getString(R.string.field_error_required) : null);
        if (nameText.isEmpty() || goalText.isEmpty()) return;

        double goal;
        try {
            goal = Double.parseDouble(goalText);
        } catch (NumberFormatException e) {
            binding.fieldSavingGoalLayout.setError(getString(R.string.field_error_invalid_number));
            return;
        }

        Saving editedSaving = new Saving();
        editedSaving.setID(currentSaving.getID());
        editedSaving.setName(nameText);
        editedSaving.setCurrentSaving(currentSaving.getCurrentSaving());
        editedSaving.setGoal(goal);
        editedSaving.setNotes(notesText);
        editedSaving.setIsArchived(currentSaving.getIsArchived());
        editedSaving.setDeadline(currentSaving.getDeadline());

        if (deadlineText.isEmpty()) {
            AlarmUtil.cancel(this, editedSaving);
            editedSaving.setDeadline(AlarmUtil.NO_ALARM);
        } else {
            AlarmUtil.set(this, editedSaving);
        }

        savingRepository.edit(editedSaving);
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_saving, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}