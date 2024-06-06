package dev.maruffirdaus.fitapp.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.maruffirdaus.fitapp.R
import dev.maruffirdaus.fitapp.databinding.FragmentHistoryBinding
import dev.maruffirdaus.fitapp.ui.main.adapter.ListHistoryAdapter

class HistoryFragment : Fragment() {
    private lateinit var binding: FragmentHistoryBinding
    private val viewModel by activityViewModels<MainViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInsets()
        setRecyclerView()
    }

    private fun setInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.emptyMessage) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.updateLayoutParams<MarginLayoutParams> {
                topMargin = insets.top
            }

            WindowInsetsCompat.CONSUMED
        }
    }

    private fun setRecyclerView() {
        val history = viewModel.getHistory()

        with(binding) {
            if (history.isNotEmpty()) {
                historyRecyclerView.layoutManager = LinearLayoutManager(requireActivity())
                historyRecyclerView.adapter = ListHistoryAdapter(history)
            } else {
                historyRecyclerView.visibility = View.GONE
                emptyMessage.visibility = View.VISIBLE
            }

            appBar.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.clear -> {
                        MaterialAlertDialogBuilder(requireActivity())
                            .setTitle("Bersihkan riwayat?")
                            .setMessage("Seluruh riwayat akan dibersihkan.")
                            .setPositiveButton("Ya") { _, _ ->
                                if (history.isNotEmpty()) {
                                    historyRecyclerView.adapter?.notifyItemRangeRemoved(
                                        0,
                                        history.size
                                    )
                                    viewModel.clearHistory()
                                    historyRecyclerView.visibility = View.GONE
                                    emptyMessage.visibility = View.VISIBLE
                                }
                            }
                            .setNegativeButton("Tidak") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()

                        true
                    }

                    else -> false
                }
            }
        }
    }
}