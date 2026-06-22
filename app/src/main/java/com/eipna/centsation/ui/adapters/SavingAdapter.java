package com.eipna.centsation.ui.adapters;

import android.content.Context;
import android.icu.text.NumberFormat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.eipna.centsation.R;
import com.eipna.centsation.data.Currency;
import com.eipna.centsation.data.saving.Saving;
import com.eipna.centsation.data.saving.SavingOperation;
import com.eipna.centsation.util.AlarmSetter;
import com.eipna.centsation.util.DateParser;
import com.eipna.centsation.util.PreferenceUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textview.MaterialTextView;

import java.util.Objects;

public class SavingAdapter extends ListAdapter<Saving, SavingAdapter.ViewHolder> {

    private final Context context;
    private final Listener listener;
    private final PreferenceUtil preferences;

    public SavingAdapter(Context context, Listener listener) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.listener = listener;
        this.preferences = new PreferenceUtil(context);
    }

    public interface Listener {
        void onClick(int position);
        void onOperationClick(SavingOperation operation, int position);
    }

    public Saving getSavingAt(int position) {
        return getItem(position);
    }

    private static final DiffUtil.ItemCallback<Saving> DIFF_CALLBACK = new DiffUtil.ItemCallback<Saving>() {
        @Override
        public boolean areItemsTheSame(@NonNull Saving oldItem, @NonNull Saving newItem) {
            return Objects.equals(oldItem.getID(), newItem.getID());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Saving oldItem, @NonNull Saving newItem) {
            return Objects.equals(oldItem.getName(), newItem.getName())
                    && oldItem.getCurrentSaving() == newItem.getCurrentSaving()
                    && oldItem.getGoal() == newItem.getGoal()
                    && Objects.equals(oldItem.getNotes(), newItem.getNotes())
                    && oldItem.getDeadline() == newItem.getDeadline()
                    && oldItem.getIsArchived() == newItem.getIsArchived();
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View savingView = LayoutInflater.from(context).inflate(R.layout.recycler_saving, parent, false);
        return new ViewHolder(savingView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Saving currentSaving = getItem(position);
        holder.bind(currentSaving, preferences);

        holder.itemView.setOnClickListener(view -> dispatchClick(holder));
        holder.delete.setOnClickListener(view -> dispatchOperation(holder, SavingOperation.DELETE));
        holder.share.setOnClickListener(view -> dispatchOperation(holder, SavingOperation.SHARE));
        holder.update.setOnClickListener(view -> dispatchOperation(holder, SavingOperation.TRANSACTION));
        holder.archive.setOnClickListener(view -> dispatchOperation(holder, SavingOperation.ARCHIVE));
        holder.unarchive.setOnClickListener(view -> dispatchOperation(holder, SavingOperation.UNARCHIVE));
        holder.history.setOnClickListener(view -> dispatchOperation(holder, SavingOperation.HISTORY));
    }

    private void dispatchClick(ViewHolder holder) {
        int pos = holder.getBindingAdapterPosition();
        if (pos == RecyclerView.NO_POSITION) return;
        listener.onClick(pos);
    }

    private void dispatchOperation(ViewHolder holder, SavingOperation operation) {
        int pos = holder.getBindingAdapterPosition();
        if (pos == RecyclerView.NO_POSITION) return;
        listener.onOperationClick(operation, pos);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        MaterialCardView parent;
        MaterialTextView name, saving, goal, percent, deadline;
        MaterialButton update, history, archive, unarchive, delete, share;

        LinearLayout description;
        LinearProgressIndicator progress;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            parent = itemView.findViewById(R.id.saving_parent);
            name = itemView.findViewById(R.id.saving_name);
            saving = itemView.findViewById(R.id.saving_current_saving);
            goal = itemView.findViewById(R.id.saving_goal);
            percent = itemView.findViewById(R.id.saving_percent);
            description = itemView.findViewById(R.id.saving_description);
            progress = itemView.findViewById(R.id.saving_progress);
            deadline = itemView.findViewById(R.id.saving_deadline);

            update = itemView.findViewById(R.id.saving_update);
            history = itemView.findViewById(R.id.saving_history);
            archive = itemView.findViewById(R.id.saving_archive);
            unarchive = itemView.findViewById(R.id.saving_unarchive);
            delete = itemView.findViewById(R.id.saving_delete);
            share = itemView.findViewById(R.id.saving_share);
        }

        public void bind(Saving currentSaving, PreferenceUtil preferences) {
            String currencySymbol = Currency.getSymbol(preferences.getCurrency());
            String deadlineFormat = preferences.getDeadlineFormat();
            int percentValue = (int) ((currentSaving.getCurrentSaving() / currentSaving.getGoal()) * 100);

            if (Currency.isRTLCurrency(preferences.getCurrency())) {
                description.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
                saving.setTextDirection(View.TEXT_DIRECTION_RTL);
                goal.setTextDirection(View.TEXT_DIRECTION_RTL);
            } else {
                description.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
                saving.setTextDirection(View.TEXT_DIRECTION_LTR);
                goal.setTextDirection(View.TEXT_DIRECTION_LTR);
            }

            if (currentSaving.getIsArchived() == Saving.IS_ARCHIVE) {
                archive.setVisibility(View.GONE);
                update.setVisibility(View.GONE);
                name.setAlpha(0.6f);

                SpannableString spannableString = new SpannableString(currentSaving.getName());
                spannableString.setSpan(new StrikethroughSpan(), 0, currentSaving.getName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                name.setText(spannableString);
            } else {
                unarchive.setVisibility(View.GONE);
                name.setText(currentSaving.getName());
            }

            String notes = currentSaving.getNotes();
            share.setVisibility((notes == null || notes.isEmpty()) ? View.GONE : View.VISIBLE);

            deadline.setVisibility(currentSaving.getDeadline() == AlarmSetter.NO_ALARM ? View.GONE : View.VISIBLE);
            deadline.setText(String.format("Deadline: %s", DateParser.getStringDate(currentSaving.getDeadline(), deadlineFormat)));

            percent.setText(String.format("(%s%c)", percentValue, '%'));
            parent.setChecked(currentSaving.getCurrentSaving() >= currentSaving.getGoal());
            saving.setText(String.format("%s%s", currencySymbol, NumberFormat.getInstance().format(currentSaving.getCurrentSaving())));
            goal.setText(String.format("%s%s", currencySymbol, NumberFormat.getInstance().format(currentSaving.getGoal())));
            progress.setProgress(percentValue, true);
        }
    }
}