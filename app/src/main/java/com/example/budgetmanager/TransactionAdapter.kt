package com.example.budgetmanager

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TransactionAdapter(private val transactions: List<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Use the actual IDs from your existing item_transaction.xml layout
        val descriptionTextView: TextView = itemView.findViewById(R.id.tvDescription)
        val amountTextView: TextView = itemView.findViewById(R.id.tvAmount)
        val categoryTextView: TextView = itemView.findViewById(R.id.tvCategory)
        val dateTextView: TextView = itemView.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]

        // Use description instead of title
        holder.descriptionTextView.text = transaction.description
        holder.categoryTextView.text = transaction.category
        holder.dateTextView.text = transaction.date

        // Set amount with color coding
        if (transaction.type == "income") {
            holder.amountTextView.text = "+₹${transaction.amount}"
            holder.amountTextView.setTextColor(Color.GREEN)
        } else {
            holder.amountTextView.text = "-₹${transaction.amount}"
            holder.amountTextView.setTextColor(Color.RED)
        }
    }

    override fun getItemCount(): Int {
        return transactions.size
    }
}