package com.example.preely.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.preely.R;
import com.example.preely.model.entities.Transaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactionList = new ArrayList<>();
    private OnTransactionClickListener listener;

    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction);
        void onTransactionEdit(Transaction transaction);
        void onTransactionDelete(Transaction transaction);
    }

    public void setOnTransactionClickListener(OnTransactionClickListener listener) {
        this.listener = listener;
    }

    public void setTransactionList(List<Transaction> transactionList) {
        this.transactionList = transactionList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        holder.bind(transaction);
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    class TransactionViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTransactionId;
        private TextView tvTransactionAmount;
        private TextView tvTransactionStatus;
        private TextView tvTransactionDate;
        private ImageButton btnEdit;
        private ImageButton btnDelete;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTransactionId = itemView.findViewById(R.id.tv_transaction_id);
            tvTransactionAmount = itemView.findViewById(R.id.tv_transaction_amount);
            tvTransactionStatus = itemView.findViewById(R.id.tv_transaction_status);
            tvTransactionDate = itemView.findViewById(R.id.tv_transaction_date);
            btnEdit = itemView.findViewById(R.id.btn_edit_transaction);
            btnDelete = itemView.findViewById(R.id.btn_delete_transaction);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onTransactionClick(transactionList.get(position));
                }
            });

            btnEdit.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onTransactionEdit(transactionList.get(position));
                }
            });

            btnDelete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onTransactionDelete(transactionList.get(position));
                }
            });
        }

        public void bind(Transaction transaction) {
            tvTransactionId.setText(transaction.getId() != null ? "ID: " + transaction.getId() : "N/A");
            tvTransactionAmount.setText(transaction.getAmount() != null ? "$" + transaction.getAmount() : "N/A");
            tvTransactionStatus.setText(transaction.getStatus() != null ? transaction.getStatus() : "N/A");
            tvTransactionDate.setText(transaction.getTransaction_date() != null ? transaction.getTransaction_date().toString() : "N/A");

            // Set status color
            if (transaction.getStatus() != null) {
                switch (transaction.getStatus().toLowerCase()) {
                    case "paid":
                        tvTransactionStatus.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
                        break;
                    case "unpaid":
                        tvTransactionStatus.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
                        break;
                    case "pending":
                        tvTransactionStatus.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_orange_dark));
                        break;
                    default:
                        tvTransactionStatus.setTextColor(itemView.getContext().getResources().getColor(android.R.color.darker_gray));
                        break;
                }
            }
        }
    }
} 