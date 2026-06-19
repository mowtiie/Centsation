package com.eipna.centsation.ui.activities;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.eipna.centsation.R;
import com.eipna.centsation.data.Contrast;
import com.eipna.centsation.data.Currency;
import com.eipna.centsation.data.Database;
import com.eipna.centsation.data.Theme;
import com.eipna.centsation.data.saving.Saving;
import com.eipna.centsation.data.saving.SavingRepository;
import com.eipna.centsation.data.transaction.Transaction;
import com.eipna.centsation.data.transaction.TransactionRepository;
import com.eipna.centsation.databinding.ActivitySettingsBinding;
import com.eipna.centsation.util.AlarmUtil;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SettingsActivity extends BaseActivity {

    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
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

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings_container, new SettingsFragment())
                    .commit();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        private static final String TAG_EXPORT = "SettingsExport";
        private static final String TAG_IMPORT = "SettingsImport";

        private final ExecutorService executor = Executors.newSingleThreadExecutor();

        private AlertDialog progressDialog;

        private ListPreference listContrast;
        private ListPreference listTheme;
        private ListPreference listCurrency;

        private SwitchPreferenceCompat switchDynamicColors;
        private SwitchPreferenceCompat switchScreenPrivacy;

        private Preference exportSavings;
        private Preference importSavings;

        private final ActivityResultLauncher<Intent> exportDataLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            exportJSON(data.getData());
                        }
                    }
                });

        private final ActivityResultLauncher<Intent> importDataLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            importJSON(data.getData());
                        }
                    }
                });

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences_settings, rootKey);
            findPreferences();

            exportSavings.setOnPreferenceClickListener(preference -> {
                exportData();
                return true;
            });

            importSavings.setOnPreferenceClickListener(preference -> {
                importData();
                return true;
            });

            listCurrency.setEntries(Currency.getNames());
            listCurrency.setEntryValues(Currency.getCodes());

            listTheme.setOnPreferenceChangeListener((preference, newValue) -> {
                String selectedTheme = (String) newValue;
                if (selectedTheme.equals(Theme.SYSTEM.VALUE)) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                if (selectedTheme.equals(Theme.BATTERY.VALUE)) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);
                if (selectedTheme.equals(Theme.LIGHT.VALUE)) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                if (selectedTheme.equals(Theme.DARK.VALUE)) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                return true;
            });

            listContrast.setOnPreferenceChangeListener((preference, newValue) -> {
                requireActivity().recreate();
                return true;
            });

            switchDynamicColors.setVisible(DynamicColors.isDynamicColorAvailable());
            switchDynamicColors.setOnPreferenceChangeListener((preference, newValue) -> {
                requireActivity().recreate();
                return true;
            });

            switchScreenPrivacy.setOnPreferenceChangeListener((preference, newValue) -> {
                requireActivity().recreate();
                return true;
            });
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            dismissProgressDialog();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            executor.shutdown();
        }

        private void findPreferences() {
            listCurrency = findPreference("currency");
            listTheme = findPreference("theme");
            listContrast = findPreference("contrast");

            switchDynamicColors = findPreference("dynamic_colors");
            switchScreenPrivacy = findPreference("screen_privacy");

            exportSavings = findPreference("export");
            importSavings = findPreference("import");
        }

        private void exportData() {
            Context appContext = requireContext().getApplicationContext();
            executor.execute(() -> {
                boolean empty = isSavingsTableEmpty(appContext);
                postToUi(() -> {
                    if (empty) {
                        Toast.makeText(requireContext(), R.string.toast_export_no_savings_found, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent exportIntent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                    exportIntent.addCategory(Intent.CATEGORY_OPENABLE);
                    exportIntent.setType("application/json");
                    exportIntent.putExtra(Intent.EXTRA_TITLE, "exported_savings.json");
                    exportDataLauncher.launch(exportIntent);
                });
            });
        }

        private void exportJSON(Uri uri) {
            showProgressDialog(R.string.progress_exporting);
            Context appContext = requireContext().getApplicationContext();
            executor.execute(() -> {
                boolean success = performExport(appContext, uri);
                postToUi(() -> {
                    dismissProgressDialog();
                    Toast.makeText(requireContext(),
                            success ? R.string.toast_export_successful : R.string.toast_export_failed,
                            Toast.LENGTH_SHORT).show();
                });
            });
        }

        private boolean isSavingsTableEmpty(Context context) {
            try (SavingRepository repo = new SavingRepository(context)) {
                return repo.getAllSavings().isEmpty();
            } catch (Exception e) {
                Log.e(TAG_EXPORT, "Could not check savings table", e);
                return false;
            }
        }

        private boolean performExport(Context context, Uri uri) {
            try (SavingRepository savingRepo = new SavingRepository(context);
                 TransactionRepository txRepo = new TransactionRepository(context)) {

                ArrayList<Saving> savings = new ArrayList<>(savingRepo.getAllSavings());
                ArrayList<Transaction> transactions = new ArrayList<>(txRepo.getAll());

                JSONArray savingJsonArray = new JSONArray();
                for (Saving saving : savings) {
                    JSONObject savingObject = new JSONObject();
                    savingObject.put(Database.COLUMN_SAVING_ID, saving.getID());
                    savingObject.put(Database.COLUMN_SAVING_NAME, saving.getName());
                    savingObject.put(Database.COLUMN_SAVING_CURRENT_SAVING, saving.getCurrentSaving());
                    savingObject.put(Database.COLUMN_SAVING_GOAL, saving.getGoal());
                    savingObject.put(Database.COLUMN_SAVING_NOTES,
                            saving.getNotes() == null ? "" : saving.getNotes());
                    savingObject.put(Database.COLUMN_SAVING_IS_ARCHIVED, saving.getIsArchived());
                    savingObject.put(Database.COLUMN_SAVING_DEADLINE, saving.getDeadline());
                    savingJsonArray.put(savingObject);
                }

                JSONArray transactionJsonArray = new JSONArray();
                for (Transaction transaction : transactions) {
                    JSONObject transactionObject = new JSONObject();
                    transactionObject.put(Database.COLUMN_TRANSACTION_ID, transaction.getID());
                    transactionObject.put(Database.COLUMN_TRANSACTION_SAVING_ID, transaction.getSavingID());
                    transactionObject.put(Database.COLUMN_TRANSACTION_AMOUNT, transaction.getAmount());
                    transactionObject.put(Database.COLUMN_TRANSACTION_TYPE, transaction.getType());
                    transactionObject.put(Database.COLUMN_TRANSACTION_DATE, transaction.getDate());
                    transactionJsonArray.put(transactionObject);
                }

                JSONObject jsonExport = new JSONObject();
                jsonExport.put(Database.TABLE_SAVING, savingJsonArray);
                jsonExport.put(Database.TABLE_TRANSACTION, transactionJsonArray);

                try (OutputStream out = context.getContentResolver().openOutputStream(uri)) {
                    if (out == null) {
                        Log.e(TAG_EXPORT, "openOutputStream returned null for " + uri);
                        return false;
                    }
                    out.write(jsonExport.toString().getBytes(StandardCharsets.UTF_8));
                }
                return true;
            } catch (Exception e) {
                Log.e(TAG_EXPORT, "Export failed", e);
                return false;
            }
        }

        private void importData() {
            Intent importIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            importIntent.addCategory(Intent.CATEGORY_OPENABLE);
            importIntent.setType("application/json");
            importDataLauncher.launch(importIntent);
        }

        private void importJSON(Uri uri) {
            showProgressDialog(R.string.progress_importing);
            Context appContext = requireContext().getApplicationContext();
            executor.execute(() -> {
                boolean success = performImport(appContext, uri);
                postToUi(() -> {
                    dismissProgressDialog();
                    Toast.makeText(requireContext(),
                            success ? R.string.toast_import_successful : R.string.toast_import_failed,
                            Toast.LENGTH_SHORT).show();
                });
            });
        }

        private boolean performImport(Context context, Uri uri) {
            String jsonContent;
            try (InputStream rawStream = context.getContentResolver().openInputStream(uri)) {
                if (rawStream == null) {
                    Log.e(TAG_IMPORT, "openInputStream returned null for " + uri);
                    return false;
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(rawStream, StandardCharsets.UTF_8));
                StringBuilder jsonBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonBuilder.append(line);
                }
                jsonContent = jsonBuilder.toString();
            } catch (Exception e) {
                Log.e(TAG_IMPORT, "Could not read input file", e);
                return false;
            }

            try (Database database = new Database(context)) {
                SQLiteDatabase writableDatabase = database.getWritableDatabase();

                JSONObject jsonImport = new JSONObject(jsonContent);
                JSONArray savingJsonArray = jsonImport.getJSONArray(Database.TABLE_SAVING);
                JSONArray transactionJsonArray = jsonImport.getJSONArray(Database.TABLE_TRANSACTION);

                writableDatabase.beginTransaction();
                try {
                    long now = System.currentTimeMillis();

                    for (int i = 0; i < savingJsonArray.length(); i++) {
                        JSONObject savingObject = savingJsonArray.getJSONObject(i);
                        long savingDeadline = savingObject.getLong(Database.COLUMN_SAVING_DEADLINE);

                        if (savingDeadline != AlarmUtil.NO_ALARM && savingDeadline > now) {
                            Saving rescheduledSaving = new Saving();
                            rescheduledSaving.setID(savingObject.getString(Database.COLUMN_SAVING_ID));
                            rescheduledSaving.setName(savingObject.getString(Database.COLUMN_SAVING_NAME));
                            rescheduledSaving.setDeadline(savingDeadline);
                            AlarmUtil.set(context, rescheduledSaving);
                        }

                        ContentValues savingValues = new ContentValues();
                        savingValues.put(Database.COLUMN_SAVING_ID, savingObject.getString(Database.COLUMN_SAVING_ID));
                        savingValues.put(Database.COLUMN_SAVING_NAME, savingObject.getString(Database.COLUMN_SAVING_NAME));
                        savingValues.put(Database.COLUMN_SAVING_CURRENT_SAVING, savingObject.getDouble(Database.COLUMN_SAVING_CURRENT_SAVING));
                        savingValues.put(Database.COLUMN_SAVING_GOAL, savingObject.getDouble(Database.COLUMN_SAVING_GOAL));
                        savingValues.put(Database.COLUMN_SAVING_NOTES, savingObject.optString(Database.COLUMN_SAVING_NOTES, ""));
                        savingValues.put(Database.COLUMN_SAVING_IS_ARCHIVED, savingObject.getInt(Database.COLUMN_SAVING_IS_ARCHIVED));
                        savingValues.put(Database.COLUMN_SAVING_DEADLINE, savingDeadline);
                        writableDatabase.insert(Database.TABLE_SAVING, null, savingValues);
                    }

                    for (int i = 0; i < transactionJsonArray.length(); i++) {
                        JSONObject transactionObject = transactionJsonArray.getJSONObject(i);
                        ContentValues transactionValues = new ContentValues();
                        transactionValues.put(Database.COLUMN_TRANSACTION_SAVING_ID, transactionObject.getString(Database.COLUMN_TRANSACTION_SAVING_ID));
                        transactionValues.put(Database.COLUMN_TRANSACTION_AMOUNT, transactionObject.getDouble(Database.COLUMN_TRANSACTION_AMOUNT));
                        transactionValues.put(Database.COLUMN_TRANSACTION_TYPE, transactionObject.getString(Database.COLUMN_TRANSACTION_TYPE));
                        transactionValues.put(Database.COLUMN_TRANSACTION_DATE, transactionObject.getLong(Database.COLUMN_TRANSACTION_DATE));
                        writableDatabase.insert(Database.TABLE_TRANSACTION, null, transactionValues);
                    }

                    writableDatabase.setTransactionSuccessful();
                    return true;
                } finally {
                    writableDatabase.endTransaction();
                }
            } catch (Exception e) {
                Log.e(TAG_IMPORT, "Import failed", e);
                return false;
            }
        }

        private void showProgressDialog(@StringRes int messageRes) {
            if (!isAdded()) return;
            dismissProgressDialog();

            View view = LayoutInflater.from(requireContext())
                    .inflate(R.layout.dialog_progress, null, false);
            ((TextView) view.findViewById(R.id.progress_message)).setText(messageRes);

            progressDialog = new MaterialAlertDialogBuilder(requireContext())
                    .setView(view)
                    .setCancelable(false)
                    .create();
            progressDialog.show();
        }

        private void dismissProgressDialog() {
            if (progressDialog != null) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                progressDialog = null;
            }
        }

        private void postToUi(Runnable runnable) {
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                if (isAdded()) {
                    runnable.run();
                }
            });
        }
    }
}