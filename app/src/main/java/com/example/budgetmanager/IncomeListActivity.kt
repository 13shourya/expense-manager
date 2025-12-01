package com.example.budgetmanager

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class IncomeListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val incomes = mutableListOf<Transaction>()
    private val db = Firebase.firestore
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_income_list)

        setupRecyclerView()
        loadIncomes()
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val adapter = object : RecyclerView.Adapter<SimpleViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleViewHolder {
                val view = layoutInflater.inflate(R.layout.item_transaction, parent, false)
                return SimpleViewHolder(view)
            }

            override fun onBindViewHolder(holder: SimpleViewHolder, position: Int) {
                val transaction = incomes[position]

                holder.amount.text = "$${transaction.amount}"
                holder.category.text = transaction.category
                holder.date.text = transaction.date
                holder.description.text = transaction.description

                // DELETE BUTTON
                holder.deleteButton.setOnClickListener {
                    AlertDialog.Builder(this@IncomeListActivity)
                        .setTitle("Delete Income")
                        .setMessage("Delete ${transaction.description}?")
                        .setPositiveButton("Delete") { dialog, which ->
                            db.collection("users").document(userId)
                                .collection("transactions")
                                .document(transaction.id)
                                .delete()
                                .addOnSuccessListener {
                                    val intent = Intent(this@IncomeListActivity, IncomeListActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                    Toast.makeText(this@IncomeListActivity, "Deleted!", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this@IncomeListActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            }

            override fun getItemCount(): Int = incomes.size
        }

        recyclerView.adapter = adapter
    }

    private fun loadIncomes() {
        db.collection("users").document(userId)
            .collection("transactions")
            .whereEqualTo("type", "income")
            .get()
            .addOnSuccessListener { value ->
                incomes.clear()
                value.documents.forEach { doc ->
                    incomes.add(Transaction(
                        id = doc.id,
                        type = doc.getString("type") ?: "",
                        amount = doc.getDouble("amount") ?: 0.0,
                        category = doc.getString("category") ?: "",
                        date = doc.getString("date") ?: "",
                        description = doc.getString("description") ?: ""
                    ))
                }
                recyclerView.adapter?.notifyDataSetChanged()
                updateTotal()
            }
    }

    private fun updateTotal() {
        var total = 0.0
        incomes.forEach { total += it.amount }
        findViewById<TextView>(R.id.tvTotal)?.text = "Total: $${"%.2f".format(total)}"
    }

    // FIXED: Corrected findViewById typo and ensure view IDs exist in layout
    class SimpleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val amount: TextView = itemView.findViewById(R.id.tvAmount)
        val category: TextView = itemView.findViewById(R.id.tvCategory)
        val date: TextView = itemView.findViewById(R.id.tvDate) // FIXED: findViAewById -> findViewById
        val description: TextView = itemView.findViewById(R.id.tvDescription)
        val deleteButton: ImageButton = itemView.findViewById(R.id.btnDelete)
    }
}