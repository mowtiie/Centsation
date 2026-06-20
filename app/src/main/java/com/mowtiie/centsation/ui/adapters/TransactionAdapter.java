package com.mowtiie.centsation.ui.adapters;

import android.content.Context;
import android.icu.text.NumberFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.mowtiie.centsation.R;
import com.mowtiie.centsation.data.Currency;
import com.mowtiie.centsation.data.transaction.Transaction;
import com.mowtiie.centsation.data.transaction.TransactionType;
import com.mowtiie.centsation.util.DateParser;
import com.mowtiie.centsation.util.PreferenceUtil;

import java.util.Objects;

public class TransactionAdapter extends ListAdapter<Transaction, TransactionAdapter.ViewHolder> {

    private static final DiffUtil.ItemCallback<Transaction> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Transaction>() {
                @Override
                public boolean areItemsTheSame(@NonNull Transaction oldItem, @NonNull Transaction newItem) {
                    return oldItem.getID() == newItem.getID();
                }

                @Override
                public boolean areContentsTheSame(@NonNull Transaction oldItem, @NonNull Transaction newItem) {
                    return oldItem.getAmount() == newItem.getAmount()
                            && oldItem.getDate() == newItem.getDate()
                            && Objects.equals(oldItem.getType(), newItem.getType());
                }
            };

    private final Context context;
    private final PreferenceUtil preferenceUtil;

    public TransactionAdapter(Context context) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.preferenceUtil = new PreferenceUtil(context);
    }

    @NonNull
    @Override
    public TransactionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View transactionView = LayoutInflater.from(context)
                .inflate(R.layout.recycler_transaction, parent, false);
        return new ViewHolder(transactionView);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionAdapter.ViewHolder holder, int position) {
        holder.bind(getItem(position), preferenceUtil, context);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        MaterialCardView parent;
        MaterialTextView amount, date;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            parent = itemView.findViewById(R.id.transaction_parent);
            amount = itemView.findViewById(R.id.transaction_amount);
            date = itemView.findViewById(R.id.transaction_date);
        }

        public void bind(Transaction currentTransaction, PreferenceUtil preferenceUtil, Context context) {
            String selectedCurrencySymbol = Currency.getSymbol(preferenceUtil.getCurrency());

            int depositCardColor = context.getResources().getColor(R.color.md_theme_primaryContainer, itemView.getContext().getTheme());
            int depositTextColor = context.getResources().getColor(R.color.md_theme_onPrimaryContainer, itemView.getContext().getTheme());

            int withdrawCardColor = context.getResources().getColor(R.color.md_theme_tertiaryContainer, itemView.getContext().getTheme());
            int withdrawTextColor = context.getResources().getColor(R.color.md_theme_onTertiaryContainer, itemView.getContext().getTheme());

            date.setText(DateParser.getStringDate(currentTransaction.getDate()));

            if (currentTransaction.getType().equals(TransactionType.DEPOSIT.VALUE) || currentTransaction.getType().equals(TransactionType.CREATED.VALUE)) {
                parent.setCardBackgroundColor(depositCardColor);
                amount.setTextColor(depositTextColor);
                amount.setText(String.format("%c%s%s", '+', selectedCurrencySymbol, NumberFormat.getInstance().format(currentTransaction.getAmount())));
            } else if (currentTransaction.getType().equals(TransactionType.WITHDRAW.VALUE)) {
                parent.setCardBackgroundColor(withdrawCardColor);
                amount.setTextColor(withdrawTextColor);
                amount.setText(String.format("%c%s%s", '-', selectedCurrencySymbol, NumberFormat.getInstance().format(currentTransaction.getAmount())));
            }
        }
    }
}