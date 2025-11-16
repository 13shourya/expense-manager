package com.example.budgetmanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class IncomeAdapter(private var incomes: List<IncomeItem>) :
    RecyclerView.Adapter<IncomeAdapter.IncomeViewHolder>() {

    class IncomeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_income, parent, false)
        return IncomeViewHolder(view)
    }

    override fun onBindViewHolder(holder: IncomeViewHolder, position: Int) {
        val income = incomes[position]
        holder.tvDescription.text = income.description
        holder.tvAmount.text = "+$${String.format("%.2f", income.amount)}"
        holder.tvDate.text = income.date
    }

    override fun getItemCount(): Int {
        return incomes.size
    }

    fun updateIncomes(newIncomes: List<IncomeItem>) {
        this.incomes = newIncomes
        notifyDataSetChanged()
    }
}