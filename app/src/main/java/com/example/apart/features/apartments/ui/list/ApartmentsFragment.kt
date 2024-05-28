package com.example.apart.features.apartments.ui.list

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
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
import com.example.apart.features.map.ui.places.PlaceItemDecoration
import com.example.apart.utils.toPx
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

        viewLifecycleOwner.lifecycleScope.launch {

//            viewModel.uploadData()

            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    setUpViews(it)
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
            apartmentsList.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
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
                Log.i("ApartmentsFragment", uiState.data.joinToString())
                apartmentAdapter.items = uiState.data
                binding.progressCircular.visibility = View.GONE

            }
        }
    }

}