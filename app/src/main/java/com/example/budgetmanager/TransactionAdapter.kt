package com.example.budgetmanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class TransactionAdapter(private var transactions: List<TransactionItem>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvType: TextView = itemView.findViewById(R.id.tvType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]

        holder.tvDescription.text = transaction.description
        holder.tvDate.text = transaction.date
        holder.tvType.text = transaction.type

        // Set amount with color coding
        if (transaction.amount < 0) {
            // Expense - red color
            holder.tvAmount.text = "-$${String.format("%.2f", -transaction.amount)}"
            holder.tvAmount.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_red_dark))
            holder.tvType.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_red_dark))
        } else {
            // Income - green color
            holder.tvAmount.text = "+$${String.format("%.2f", transaction.amount)}"
            holder.tvAmount.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_green_dark))
            holder.tvType.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_green_dark))
        }
    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    fun updateTransactions(newTransactions: List<TransactionItem>) {
        this.transactions = newTransactions
        notifyDataSetChanged()
    }
}