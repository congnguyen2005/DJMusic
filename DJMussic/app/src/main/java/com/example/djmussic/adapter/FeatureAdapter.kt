package com.example.djmussic.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.djmussic.R

// Định nghĩa FeatureItem ngay trong file này
data class FeatureItem(
    val title: String,
    val iconRes: Int,
    val targetActivity: Class<*>?
)

class FeatureAdapter(
    private val features: List<FeatureItem>,
    private val onClick: (FeatureItem) -> Unit
) : RecyclerView.Adapter<FeatureAdapter.ViewHolder>() {

    class ViewHolder(private val cardView: CardView) : RecyclerView.ViewHolder(cardView) {
        private val iconView: ImageView = cardView.findViewById(R.id.feature_icon)
        private val titleView: TextView = cardView.findViewById(R.id.feature_title)

        fun bind(feature: FeatureItem, clickListener: (FeatureItem) -> Unit) {
            iconView.setImageDrawable(ContextCompat.getDrawable(cardView.context, feature.iconRes))
            titleView.text = feature.title
            cardView.setOnClickListener { clickListener(feature) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feature, parent, false) as CardView
        return ViewHolder(view)
    }

    override fun getItemCount() = features.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(features[position], onClick)
    }
}