package com.mowtiie.centsation.ui.adapters;

import android.content.Context;
import android.icu.text.NumberFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mowtiie.centsation.R;
import com.mowtiie.centsation.data.Currency;
import com.mowtiie.centsation.data.transaction.Transaction;
import com.mowtiie.centsation.data.transaction.TransactionType;
import com.mowtiie.centsation.util.DateUtil;
import com.mowtiie.centsation.util.PreferenceUtil;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private final Context context;
    private final PreferenceUtil preferenceUtil;
    private final ArrayList<Transaction> transactions;

    public TransactionAdapter(Context context, ArrayList<Transaction> transactions) {
        this.context = context;
        this.transactions = transactions;
        this.preferenceUtil = new PreferenceUtil(context);
    }

    @NonNull
    @Override
    public TransactionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View transactionView = LayoutInflater.from(context).inflate(R.layout.recycler_transaction, parent, false);
        return new ViewHolder(transactionView);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionAdapter.ViewHolder holder, int position) {
        Transaction currentTransaction = transactions.get(position);
        holder.bind(currentTransaction, preferenceUtil, context);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        MaterialTextView type, amount, date;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            type = itemView.findViewById(R.id.transaction_type);
            amount = itemView.findViewById(R.id.transaction_amount);
            date = itemView.findViewById(R.id.transaction_date);
        }

        public void bind(Transaction currentTransaction, PreferenceUtil preferenceUtil, Context context) {
            String selectedCurrencySymbol = Currency.getSymbol(preferenceUtil.getCurrency());

            type.setText(currentTransaction.getType());
            date.setText(DateUtil.getStringDate(currentTransaction.getDate()));

            if (currentTransaction.getType().equals(TransactionType.DEPOSIT.VALUE) || currentTransaction.getType().equals(TransactionType.CREATED.VALUE)) {
                amount.setTextColor(context.getResources().getColor(R.color.md_theme_secondary, itemView.getContext().getTheme()));
                amount.setText(String.format("%c%s%s", '+', selectedCurrencySymbol, NumberFormat.getInstance().format(currentTransaction.getAmount())));
            } else if (currentTransaction.getType().equals(TransactionType.WITHDRAW.VALUE)) {
                amount.setTextColor(context.getResources().getColor(R.color.md_theme_error, itemView.getContext().getTheme()));
                amount.setText(String.format("%c%s%s", '-', selectedCurrencySymbol, NumberFormat.getInstance().format(currentTransaction.getAmount())));
            }
        }
    }
}