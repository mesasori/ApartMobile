package com.example.apart.features.map.ui.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.example.apart.databinding.FragmentGeoDetailsDialogBinding
import com.example.apart.features.map.data.GeoObjectHolder
import com.example.apart.features.map.data.goneOrRun
import com.example.apart.features.map.data.repository.PlaceRepository
import com.example.apart.features.map.data.room.PlaceDatabase
import com.example.apart.features.map.ui.places.PlaceHolderItem
import com.example.apart.features.map.ui.places.PlacesDialogFragment
import com.example.apart.features.map.ui.places.PlacesDialogViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class GeoDetailsDialogFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentGeoDetailsDialogBinding

    private lateinit var viewModel: GeoDetailsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGeoDetailsDialogBinding.inflate(layoutInflater)
        viewModel = GeoDetailsViewModel(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.uiState()?.let { uiState ->
            binding.apply {
                textTitle.text = uiState.title
                textSubtitle.text = uiState.descriptionText
                textPlace.text = "${uiState.location?.latitude}, ${uiState.location?.longitude}"

                when (val state = uiState.typeSpecificState) {
                    is TypeSpecificState.Business -> {
                        layoutBusinessInfo.isVisible = true
                        textType.text = "Business organisation:"
                        textBusinessName.text = state.name
                        textBusinessWorkingHours.goneOrRun(state.workingHours) {
                            text = it
                        }
                        textBusinessCategories.text = state.categories
                        textBusinessPhones.text = state.phones
                        textBusinessLinks.goneOrRun(state.link) {
                            text = it
                        }
                    }
                    is TypeSpecificState.Toponym -> {
                        layoutToponymInfo.isVisible = true
                        textType.text = "Toponym:"
                        textToponymAddress.text = state.address
                    }
                    TypeSpecificState.Undefined -> {
                        textType.isVisible = false
                    }
                }

                buttonAdd.setOnClickListener {
                    GeoObjectHolder.selectedObject = GeoObjectHolder.tappedObject
                    viewModel.addPlace(PlaceHolderItem(uiState.title, uiState.descriptionText, uiState.location!!))
                    showPlacesDialog()
                }
            }
        }
    }
    private fun showPlacesDialog() {
        val placesDialog = PlacesDialogFragment()
        placesDialog.show(this@GeoDetailsDialogFragment.parentFragmentManager, null)
    }
}