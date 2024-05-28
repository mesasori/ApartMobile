package com.example.apart.features.map.ui.places

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.apart.R
import com.example.apart.databinding.FragmentPlacesDialogBinding
import com.example.apart.utils.toPx
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

class PlacesDialogFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentPlacesDialogBinding
    private val viewModel: PlacesDialogViewModel by viewModels()

    private lateinit var placeAdapter: PlaceListAdapter
    private lateinit var swipeHelper: ItemTouchHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlacesDialogBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createAdapter()
        setUpAdapter()
        setUpHelper()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    setUpViews(it)
                }
            }
        }
    }

    private fun setUpAdapter() {
        binding.apply {
            listPlaces.adapter = placeAdapter
            listPlaces.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            listPlaces.addItemDecoration(
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

    private fun createAdapter() {
        placeAdapter = PlaceListAdapter(object : PlaceItemClickListener {
            override fun onPlusClick(place: PlaceHolderItem) {
                if (place.importance < 10) place.importance += 1
                else Toast.makeText(requireContext(), "Can't be more than 10", Toast.LENGTH_SHORT)
                    .show()
                viewModel.updatePlace(place)
            }

            override fun onMinusClick(place: PlaceHolderItem) {
                if (place.importance > 0) place.importance -= 1
                else Toast.makeText(requireContext(), "Can't be lower than 0", Toast.LENGTH_SHORT)
                    .show()
                viewModel.updatePlace(place)
            }
        })
    }

    private fun setUpViews(uiState: PlaceUiState) {
        when (uiState) {
            is PlaceUiState.Loading -> {
                binding.progressCircular.visibility = View.VISIBLE
            }

            is PlaceUiState.Error -> Toast.makeText(
                requireContext(),
                "Error: ${uiState.throwable.toString()}",
                Toast.LENGTH_SHORT
            ).show()

            is PlaceUiState.Success -> {
                placeAdapter.items = uiState.data
                if (placeAdapter.items.isEmpty()) binding.textPlaces.text = "Add a place to the list"
                else binding.textPlaces.text = "PLACES"
                binding.progressCircular.visibility = View.GONE
            }
        }
    }

    private fun setUpHelper() {
        val displayMetrics = resources.displayMetrics
        val width = (displayMetrics.widthPixels / displayMetrics.density).toInt().dp
        val deleteIcon = ContextCompat.getDrawable(requireContext(), R.drawable.round_delete_24)

        swipeHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = true

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.adapterPosition
                viewModel.removePlace(placeAdapter.items[pos])
                placeAdapter.notifyItemRemoved(pos)

                Log.d("PlacesDialogFragment", "Deleted")
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val corners = floatArrayOf(
                    35f, 35f,   // Top left radius in px
                    35f, 35f,   // Top right radius in px
                    35f, 35f,   // Bottom right radius in px
                    35f, 35f,   // Bottom left radius in px
                )
                val colorRect = RectF(
                    viewHolder.itemView.left.toFloat(), viewHolder.itemView.top.toFloat(),
                    viewHolder.itemView.right.toFloat(), viewHolder.itemView.bottom.toFloat()
                )
                val path = Path()
                val grayPaint = Paint()
                grayPaint.color = resources.getColor(R.color.gray, null)
                val redPaint = Paint()
                redPaint.color = Color.RED
                path.addRoundRect(colorRect, corners, Path.Direction.CW)
                when {
                    abs(dX) < width / 3 -> c.drawPath(path, grayPaint)
                    dX < width / 3 -> c.drawPath(path, redPaint)
                }

                val textMargin = resources.getDimension(R.dimen.text_margin).roundToInt()
                deleteIcon?.let {
                    it.bounds = Rect(
                        width - 3 * textMargin - deleteIcon.intrinsicWidth,
                        viewHolder.itemView.top + (viewHolder.itemView.height - deleteIcon.intrinsicHeight) / 2,
                        width - 3 * textMargin,
                        viewHolder.itemView.top + (viewHolder.itemView.height + deleteIcon.intrinsicHeight) / 2
                    )

                    if (dX <= 0) deleteIcon.draw(c)
                }

                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )

            }
        })

        swipeHelper.attachToRecyclerView(binding.listPlaces)
    }

    private val Int.dp
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            toFloat(), resources.displayMetrics
        ).roundToInt()

}