package com.example.apart.features.apartments.ui

import android.content.res.Resources
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.OrientationEventListener
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apart.R
import com.example.apart.databinding.FragmentApartmentsBinding
import com.example.apart.features.apartments.data.ApartmentHolderItem
import com.example.apart.features.apartments.data.Status
import com.example.apart.features.map.ui.places.PlaceItemDecoration
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ApartmentsFragment : Fragment() {
    private val viewModel: ApartmentsViewModel by viewModels()
    private var _binding: FragmentApartmentsBinding? = null
    private val binding get() = _binding!!

    private lateinit var apartmentAdapter: ApartmentListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentApartmentsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createAdapter()
        setUpAdapter()

        startListening()
    }

    private fun startListening() {
        lifecycleScope.launch {
            viewModel.list.collect {
                when (it.status) {
                    Status.LOADING ->  binding.progressCircular.visibility = View.VISIBLE
                    Status.ERROR -> {
                        Toast.makeText(requireContext(), it.error.toString(), Toast.LENGTH_SHORT)
                            .show()
                        binding.progressCircular.visibility = View.GONE
                    }
                    Status.SUCCESS -> {
                        binding.progressCircular.visibility = View.GONE
                        apartmentAdapter.items = it.data.map { it.toHolderItem() }
                    }
                }
            }
        }
    }

    private fun createAdapter() {
        apartmentAdapter = ApartmentListAdapter(object : ApartmentItemClickListener {
            override fun onItemClick(apartment: ApartmentHolderItem) {
                Toast.makeText(requireContext(), "Go to flat", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setUpAdapter() {
        binding.apply {
            apartmentsList.adapter = apartmentAdapter
            apartmentsList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            apartmentsList.addItemDecoration(
                PlaceItemDecoration(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.recycler_view_divider,
                        null
                    )!!,
                    bottomOffset = 6f.toPx.toInt(),
                    topOffset = 6f.toPx.toInt()
                )
            )
        }
    }

    private fun setUpViews(uiState: ApartmentsUiState) {
        when (uiState) {
            is ApartmentsUiState.Loading -> {
                binding.progressCircular.visibility = View.VISIBLE
            }

            is ApartmentsUiState.Error -> {
                Toast.makeText(requireContext(), uiState.throwable.toString(), Toast.LENGTH_SHORT)
                    .show()
                binding.progressCircular.visibility = View.GONE
            }

            is ApartmentsUiState.Success -> {
                apartmentAdapter.items = uiState.data
                binding.progressCircular.visibility = View.GONE
            }
        }
    }

    private val Number.toPx
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
        )

}