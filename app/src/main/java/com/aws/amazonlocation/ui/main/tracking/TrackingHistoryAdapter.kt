package com.aws.amazonlocation.ui.main.tracking

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import aws.smithy.kotlin.runtime.time.epochMilliseconds
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.response.TrackingHistoryData
import com.aws.amazonlocation.databinding.RvLoaderItemBinding
import com.aws.amazonlocation.databinding.RvTrackingHeaderBinding
import com.aws.amazonlocation.databinding.RvTrackingItemBinding
import com.aws.amazonlocation.utils.DateFormat
import com.aws.amazonlocation.utils.hide
import com.aws.amazonlocation.utils.show
import com.aws.amazonlocation.utils.stickyHeaders.StickyHeaderAdapter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TrackingHistoryAdapter :
    androidx.recyclerview.widget.ListAdapter<TrackingHistoryData, RecyclerView.ViewHolder>(
        EqualsDiffItemCallback()
    ),
    StickyHeaderAdapter<RecyclerView.ViewHolder> {
    private val mDATA = 1
    private val mLOADER = 2

    class HeaderViewHolder(val binding: RvTrackingHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TrackingHistoryData) {
            binding.tvTrackerDate.text = item.headerString
        }

        companion object {
            fun from(parent: ViewGroup): HeaderViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding =
                    RvTrackingHeaderBinding.inflate(layoutInflater, parent, false)
                return HeaderViewHolder(binding = binding)
            }
        }
    }

    class ItemViewHolder(
        private val binding: RvTrackingItemBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TrackingHistoryData) {
            binding.apply {
                item.devicePositionData?.let {
                    tvLatLng.text = String.format("%7f , %7f", it.position[1], it.position[0])
                    val date = convertToCustomFormat(it.receivedTime.epochMilliseconds)
                    tvTime.text = date
                }
                when (item.headerData) {
                    viewTopDotted.context.getString(R.string.label_position_start) -> {
                        viewTopDotted.hide()
                        viewBottomDotted.show()
                    }
                    viewTopDotted.context.getString(R.string.label_position_end) -> {
                        viewTopDotted.show()
                        viewBottomDotted.hide()
                    }
                    viewTopDotted.context.getString(R.string.label_position_data) -> {
                        viewTopDotted.show()
                        viewBottomDotted.show()
                    }
                    else -> {
                        viewTopDotted.hide()
                        viewBottomDotted.hide()
                    }
                }
            }
        }

        private fun convertToCustomFormat(receivedTime: Long): String? {
            val date = Date(receivedTime)
            val destFormat = SimpleDateFormat(DateFormat.HH_MM_AA, Locale.getDefault())
            return date.let { destFormat.format(it) }
        }

        companion object {
            fun from(parent: ViewGroup): ItemViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvTrackingItemBinding.inflate(layoutInflater, parent, false)
                return ItemViewHolder(binding)
            }
        }
    }
    class ItemLoaderViewHolder(
        private val binding: RvLoaderItemBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.apply {
                binding.viewLoader.show()
            }
        }

        companion object {
            fun from(parent: ViewGroup): ItemLoaderViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvLoaderItemBinding.inflate(layoutInflater, parent, false)
                return ItemLoaderViewHolder(binding)
            }
        }
    }

    private class EqualsDiffItemCallback<T : Any> : DiffUtil.ItemCallback<T>() {
        override fun areItemsTheSame(item1: T, item2: T) = areContentsTheSame(item1, item2)

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(item1: T, item2: T) = item1 == item2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == mDATA) {
            ItemViewHolder.from(parent = parent)
        } else {
            ItemLoaderViewHolder.from(parent = parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemViewHolder -> {
                holder.bind(getItem(position))
            }
            is ItemLoaderViewHolder -> {
                holder.bind()
            }
        }
    }

    override fun getHeaderId(position: Int) =
        if (position > currentList.size || position < 0 || currentList.size == 0) -1L else getItem(position).headerId.toLong()

    override fun onCreateHeaderViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return HeaderViewHolder.from(parent = parent)
    }

    override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                val item = getItem(position)
                holder.bind(item)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position).headerData) {
            "Loader" -> mLOADER
            else -> mDATA
        }
    }
}
