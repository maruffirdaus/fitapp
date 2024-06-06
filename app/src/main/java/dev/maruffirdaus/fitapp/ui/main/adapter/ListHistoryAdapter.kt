package dev.maruffirdaus.fitapp.ui.main.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.maruffirdaus.fitapp.R
import dev.maruffirdaus.fitapp.data.model.History
import dev.maruffirdaus.fitapp.databinding.ItemRowHistoryBinding

class ListHistoryAdapter(
    private val listHistory: List<History>
) : RecyclerView.Adapter<ListHistoryAdapter.ListViewHolder>() {
    private lateinit var context: Context

    inner class ListViewHolder(var binding: ItemRowHistoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding = ItemRowHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = parent.context
        return ListViewHolder(binding)
    }

    override fun getItemCount(): Int = listHistory.size

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        with(holder.binding) {
            date.text = listHistory[position].date
            startingPoint.text = listHistory[position].startingPoint
            endPoint.text = listHistory[position].endPoint
            inputDistance.text = listHistory[position].inputDistance.toString()
            outputDistance.text = listHistory[position].outputDistance.toString()
            algorithm.text = if (listHistory[position].algorithm == 0) {
                context.getString(R.string.backtracking_algorithm)
            } else {
                context.getString(R.string.dijkstras_algorithm)
            }
            runningTime.text = listHistory[position].runningTime.toString()

            if (position == listHistory.lastIndex) {
                materialDivider.visibility = View.GONE
            } else {
                materialDivider.visibility = View.VISIBLE
            }
        }
    }
}