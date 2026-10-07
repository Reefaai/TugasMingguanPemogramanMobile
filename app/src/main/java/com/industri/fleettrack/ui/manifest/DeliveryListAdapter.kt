package com.industri.fleettrack.ui.manifest

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.industri.fleettrack.data.local.entity.DeliveryOrderEntity
import com.industri.fleettrack.databinding.ItemDeliveryOrderBinding
import java.text.NumberFormat
import java.util.Locale

class DeliveryListAdapter(
    private val onDeliverClicked: (DeliveryOrderEntity) -> Unit
) : ListAdapter<DeliveryOrderEntity, DeliveryListAdapter.OrderViewHolder>(OrderDiffCallback()) {

    private val rupiahFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"))

    class OrderViewHolder(val binding: ItemDeliveryOrderBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemDeliveryOrderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val item = getItem(position)
        with(holder.binding) {
            tvTrackingNo.text = item.trackingNumber
            tvRecipient.text = "${item.recipientName} • ${item.recipientPhone}"
            tvAddress.text = item.destinationAddress

            if (item.codAmount > 0) {
                tvCodAmount.text = "COD: ${rupiahFormat.format(item.codAmount)}"
            } else {
                tvCodAmount.text = "Non-COD (Lunas)"
            }

            tvStatusBadge.text = item.deliveryStatus
            when (item.deliveryStatus) {
                "DELIVERED" -> {
                    tvStatusBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#DCFCE7"))
                    tvStatusBadge.setTextColor(Color.parseColor("#15803D"))
                    btnActionDeliver.isEnabled = false
                    btnActionDeliver.text = "Selesai ✓"
                    btnActionDeliver.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#94A3B8"))
                }
                "IN_DELIVERY" -> {
                    tvStatusBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E0F2FE"))
                    tvStatusBadge.setTextColor(Color.parseColor("#0369A1"))
                    btnActionDeliver.isEnabled = true
                    btnActionDeliver.text = "Terkirim"
                    btnActionDeliver.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#2563EB"))
                }
                else -> { // PENDING
                    tvStatusBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FEF3C7"))
                    tvStatusBadge.setTextColor(Color.parseColor("#B45309"))
                    btnActionDeliver.isEnabled = true
                    btnActionDeliver.text = "Terkirim"
                    btnActionDeliver.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#2563EB"))
                }
            }

            btnActionDeliver.setOnClickListener {
                onDeliverClicked(item)
            }
        }
    }

    class OrderDiffCallback : DiffUtil.ItemCallback<DeliveryOrderEntity>() {
        override fun areItemsTheSame(oldItem: DeliveryOrderEntity, newItem: DeliveryOrderEntity): Boolean {
            return oldItem.orderId == newItem.orderId
        }

        override fun areContentsTheSame(oldItem: DeliveryOrderEntity, newItem: DeliveryOrderEntity): Boolean {
            return oldItem == newItem
        }
    }
}
