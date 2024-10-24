package com.aws.amazonlocation.ui.main.map_style

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.aws.amazonlocation.R
import com.aws.amazonlocation.data.response.MapStyleInnerData
import com.aws.amazonlocation.databinding.ItemEsriMapStyleBinding

@SuppressLint("NotifyDataSetChanged")
class MapStyleAdapter(
    private val esriArrayList: ArrayList<MapStyleInnerData>,
    private val mapStyleInterface: MapStyleInterface
) :
    RecyclerView.Adapter<MapStyleAdapter.EsriVH>() {

    var selectedPosition = -1

    inner class EsriVH(private val binding: ItemEsriMapStyleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: MapStyleInnerData) {
            binding.apply {
                tvStyleName.text = data.mapName
                ivChecked.setImageResource(data.image)
                if (data.isSelected) {
                    selectedPosition = adapterPosition
                    cardMapImage.strokeWidth = 2
                    tvStyleName.setTextColor(
                        ContextCompat.getColor(
                            tvStyleName.context,
                            R.color.color_primary_green
                        )
                    )
                } else {
                    cardMapImage.strokeWidth = 0
                    tvStyleName.setTextColor(
                        ContextCompat.getColor(
                            tvStyleName.context,
                            R.color.color_medium_black
                        )
                    )
                }
                clMain.setOnClickListener {
                    mapStyleInterface.styleClick(adapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EsriVH {
        return EsriVH(
            ItemEsriMapStyleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return esriArrayList.size
    }

    override fun onBindViewHolder(holder: EsriVH, position: Int) {
        holder.bind(esriArrayList[position])
    }

    interface MapStyleInterface {
        fun styleClick(position: Int)
    }
}