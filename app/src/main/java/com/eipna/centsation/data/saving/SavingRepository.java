package com.eipna.centsation.data.saving;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.eipna.centsation.data.Database;
import com.eipna.centsation.data.transaction.Transaction;
import com.eipna.centsation.data.transaction.TransactionRepository;
import com.eipna.centsation.data.transaction.TransactionType;
import com.eipna.centsation.util.AlarmSetter;

import java.util.ArrayList;
import java.util.List;

public class SavingRepository extends Database {

    private final TransactionRepository transactionRepository;

    private final Context context;

    public SavingRepository(@Nullable Context context) {
        super(context);
        this.context = context == null ? null : context.getApplicationContext();
        this.transactionRepository = new TransactionRepository(context);
    }

    public void create(Saving createdSaving) {
        SQLiteDatabase database = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_SAVING_ID, createdSaving.getID());
        values.put(COLUMN_SAVING_NAME, createdSaving.getName());
        values.put(COLUMN_SAVING_CURRENT_SAVING, createdSaving.getCurrentSaving());
        values.put(COLUMN_SAVING_GOAL, createdSaving.getGoal());
        values.put(COLUMN_SAVING_NOTES, createdSaving.getNotes());
        values.put(COLUMN_SAVING_IS_ARCHIVED, createdSaving.getIsArchived());
        values.put(COLUMN_SAVING_DEADLINE, createdSaving.getDeadline());
        database.insert(TABLE_SAVING, null, values);

        Transaction initialTransaction = new Transaction();
        initialTransaction.setSavingID(createdSaving.getID());
        initialTransaction.setAmount(createdSaving.getCurrentSaving());
        initialTransaction.setType(TransactionType.CREATED.VALUE);
        transactionRepository.create(initialTransaction);
        database.close();
    }

    public void edit(Saving editedSaving) {
        SQLiteDatabase database = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_SAVING_NAME, editedSaving.getName());
        values.put(COLUMN_SAVING_CURRENT_SAVING, editedSaving.getCurrentSaving());
        values.put(COLUMN_SAVING_GOAL, editedSaving.getGoal());
        values.put(COLUMN_SAVING_NOTES, editedSaving.getNotes());
        values.put(COLUMN_SAVING_IS_ARCHIVED, editedSaving.getIsArchived());
        values.put(COLUMN_SAVING_DEADLINE, editedSaving.getDeadline());

        database.update(TABLE_SAVING, values, COLUMN_SAVING_ID + " = ?", new String[]{editedSaving.getID()});
        database.close();
    }

    public void delete(String savingID) {
        SQLiteDatabase database = getWritableDatabase();
        database.delete(TABLE_SAVING, COLUMN_SAVING_ID + " = ?", new String[]{savingID});
        database.close();
    }

    public void makeTransaction(Saving updatedSaving, double amount, TransactionType type) {
        SQLiteDatabase database = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_SAVING_CURRENT_SAVING, updatedSaving.getCurrentSaving());
        database.update(TABLE_SAVING, values, COLUMN_SAVING_ID + " = ?", new String[]{updatedSaving.getID()});

        Transaction transaction = new Transaction();
        transaction.setSavingID(updatedSaving.getID());
        transaction.setAmount(Math.abs(amount));
        transaction.setType(type.VALUE);
        transaction.setDate(System.currentTimeMillis());
        transactionRepository.create(transaction);
        database.close();
    }

    public ArrayList<Saving> getSavings(int isArchive) {
        ArrayList<Saving> list = new ArrayList<>();
        SQLiteDatabase database = getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_SAVING + " WHERE " + COLUMN_SAVING_IS_ARCHIVED + " = ?";
        Cursor cursor = database.rawQuery(query, new String[]{String.valueOf(isArchive)});

        if (cursor.moveToFirst()) {
            do {
                Saving queriedSaving = new Saving();
                queriedSaving.setID(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SAVING_ID)));
                queriedSaving.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SAVING_NAME)));
                queriedSaving.setCurrentSaving(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_SAVING_CURRENT_SAVING)));
                queriedSaving.setGoal(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_SAVING_GOAL)));
                queriedSaving.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SAVING_NOTES)));
                queriedSaving.setIsArchived(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SAVING_IS_ARCHIVED)));
                queriedSaving.setDeadline(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_SAVING_DEADLINE)));
                list.add(queriedSaving);
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return list;
    }

    public ArrayList<Saving> getAllSavings() {
        ArrayList<Saving> list = new ArrayList<>();
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_SAVING, null);

        if (cursor.moveToFirst()) {
            do {
                Saving queriedSaving = new Saving();
                queriedSaving.setID(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SAVING_ID)));
                queriedSaving.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SAVING_NAME)));
                queriedSaving.setCurrentSaving(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_SAVING_CURRENT_SAVING)));
                queriedSaving.setGoal(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_SAVING_GOAL)));
                queriedSaving.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SAVING_NOTES)));
                queriedSaving.setIsArchived(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SAVING_IS_ARCHIVED)));
                queriedSaving.setDeadline(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_SAVING_DEADLINE)));
                list.add(queriedSaving);
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return list;
    }

    public Saving getSaving(String savingID) {
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_SAVING + " WHERE " + Database.COLUMN_SAVING_ID + " = ?", new String[]{savingID});

        if (cursor.moveToFirst()) {
            Saving queriedSaving = new Saving();
            queriedSaving.setID(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SAVING_ID)));
            queriedSaving.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SAVING_NAME)));
            queriedSaving.setCurrentSaving(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_SAVING_CURRENT_SAVING)));
            queriedSaving.setGoal(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_SAVING_GOAL)));
            queriedSaving.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SAVING_NOTES)));
            queriedSaving.setIsArchived(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SAVING_IS_ARCHIVED)));
            queriedSaving.setDeadline(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_SAVING_DEADLINE)));

            cursor.close();
            database.close();
            return queriedSaving;
        }
        cursor.close();
        database.close();
        return null;
    }

    public void bulkImport(@NonNull List<Saving> savings, @NonNull List<Transaction> transactions) {
        try (SQLiteDatabase database = getWritableDatabase()) {
            database.beginTransaction();
            try {
                for (Saving saving : savings) {
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_SAVING_ID, saving.getID());
                    values.put(COLUMN_SAVING_NAME, saving.getName());
                    values.put(COLUMN_SAVING_CURRENT_SAVING, saving.getCurrentSaving());
                    values.put(COLUMN_SAVING_GOAL, saving.getGoal());
                    values.put(COLUMN_SAVING_NOTES, saving.getNotes());
                    values.put(COLUMN_SAVING_IS_ARCHIVED, saving.getIsArchived());
                    values.put(COLUMN_SAVING_DEADLINE, saving.getDeadline());
                    database.insert(TABLE_SAVING, null, values);
                }

                for (Transaction transaction : transactions) {
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_TRANSACTION_SAVING_ID, transaction.getSavingID());
                    values.put(COLUMN_TRANSACTION_AMOUNT, transaction.getAmount());
                    values.put(COLUMN_TRANSACTION_TYPE, transaction.getType());
                    values.put(COLUMN_TRANSACTION_DATE, transaction.getDate());
                    database.insert(TABLE_TRANSACTION, null, values);
                }

                database.setTransactionSuccessful();
            } finally {
                database.endTransaction();
            }
        }

        long now = System.currentTimeMillis();
        for (Saving saving : savings) {
            long deadline = saving.getDeadline();
            if (deadline != AlarmSetter.NO_ALARM && deadline > now) {
                AlarmSetter.set(context, saving);
            }
        }
    }
}