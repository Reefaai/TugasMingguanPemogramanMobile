package com.praktikum.fintechbillingapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale
class InvoiceItemAdapter(private val items: List<InvoiceItem>) :
    RecyclerView.Adapter<InvoiceItemAdapter.ViewHolder>() {
    private val rupiahFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"))
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvItemName)
        val tvQtyPrice: TextView = view.findViewById(R.id.tvItemQtyPrice)
        val tvSubtotal: TextView = view.findViewById(R.id.tvItemSubtotal)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_invoice_product, parent, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvName.text = item.itemName
        holder.tvQtyPrice.text = "${item.qty} x @ ${rupiahFormat.format(item.unitPrice)}"
        holder.tvSubtotal.text = rupiahFormat.format(item.subtotal)
    }
    override fun getItemCount(): Int = items.size
}