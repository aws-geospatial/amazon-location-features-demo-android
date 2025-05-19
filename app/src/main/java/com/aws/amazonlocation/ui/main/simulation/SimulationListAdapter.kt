package com.aws.amazonlocation.ui.main.simulation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.response.SimulationHistoryData
import com.aws.amazonlocation.databinding.RvSimulationItemBinding
import com.aws.amazonlocation.utils.DateFormat
import com.aws.amazonlocation.utils.LANGUAGE_CODE_ARABIC
import com.aws.amazonlocation.utils.hide
import com.aws.amazonlocation.utils.invisible
import com.aws.amazonlocation.utils.show
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SimulationListAdapter(private val defaultLocale: Locale?) :
    ListAdapter<SimulationHistoryData, SimulationListAdapter.ItemViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            RvSimulationItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ItemViewHolder(private val binding: RvSimulationItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SimulationHistoryData) = binding.apply {
            item.devicePositionData?.let {
                tvLatLng.text = String.format(
                    Locale.getDefault(),
                    "%7f, %7f",
                    it.latitude,
                    it.longitude
                )
                val date = convertToCustomFormat(it.receivedTime)
                tvTime.text = date
            }
            if (item.isBusStopData) {
                if (defaultLocale?.language == LANGUAGE_CODE_ARABIC) {
                    tvBusStop.textDirection = View.TEXT_DIRECTION_RTL
                }
                tvBusStop.show()
                tvBusStop.text = String.format(
                    Locale.getDefault(),
                    "Bus stop number %d",
                    item.busStopCount
                )
                tvLatLng.setTextColor(
                    ContextCompat.getColor(
                        tvLatLng.context,
                        R.color.color_hint_text
                    )
                )
                ivSimulation.show()
                ivNavigationIcon.invisible()
                val padding = tvLatLng.context.resources.getDimensionPixelSize(R.dimen.dp_4)
                clNavigationItem.setPadding(
                    clNavigationItem.paddingLeft,
                    padding,
                    clNavigationItem.paddingRight,
                    padding
                )
            } else {
                tvBusStop.hide()
                tvLatLng.setTextColor(
                    ContextCompat.getColor(
                        tvLatLng.context,
                        R.color.color_medium_black
                    )
                )
                ivSimulation.hide()
                ivNavigationIcon.show()
                val padding = tvLatLng.context.resources.getDimensionPixelSize(R.dimen.dp_14)
                clNavigationItem.setPadding(
                    clNavigationItem.paddingLeft,
                    padding,
                    clNavigationItem.paddingRight,
                    padding
                )
            }
            when (adapterPosition) {
                0 -> {
                    viewTopDotted.hide()
                    viewBottomDotted.show()
                }
                currentList.size -> {
                    viewTopDotted.show()
                    viewBottomDotted.hide()
                }
                else -> {
                    viewTopDotted.show()
                    viewBottomDotted.show()
                }
            }
        }
    }
    private fun convertToCustomFormat(receivedTime: Date): String? {
        val destFormat = SimpleDateFormat(DateFormat.HH_MM_AA, Locale.getDefault())
        return receivedTime.let { destFormat.format(it) }
    }

    class DiffCallback : DiffUtil.ItemCallback<SimulationHistoryData>() {
        override fun areItemsTheSame(
            oldItem: SimulationHistoryData,
            newItem: SimulationHistoryData
        ): Boolean = oldItem.devicePositionData?.latitude == newItem.devicePositionData?.latitude

        override fun areContentsTheSame(
            oldItem: SimulationHistoryData,
            newItem: SimulationHistoryData
        ): Boolean = oldItem.devicePositionData?.latitude == newItem.devicePositionData?.latitude
    }
}
