package com.example.wgn_igloo.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.wgn_igloo.R


class HomePage : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var carouselAdapter: CarouselAdapter
    private lateinit var itemList: MutableList<CarouselAdapter.ItemData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCarousel(view)
        setupAddButton(view)
    }

    private fun setupCarousel(view: View) {
        recyclerView = view.findViewById(R.id.carousel)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        itemList = listOf(
            CarouselAdapter.ItemData(R.drawable.all, "All"),
            CarouselAdapter.ItemData(R.drawable.condiments, "Condiments"),
            CarouselAdapter.ItemData(R.drawable.dairy, "Dairy"),
            CarouselAdapter.ItemData(R.drawable.drinks, "Drinks"),
            CarouselAdapter.ItemData(R.drawable.freezer, "Freezer"),
            CarouselAdapter.ItemData(R.drawable.meat, "Meats"),
            CarouselAdapter.ItemData(R.drawable.produce, "Produce"),
            CarouselAdapter.ItemData(R.drawable.other, "Other")
        ).toMutableList()

        val initialSelectedPosition = itemList.indexOfFirst { it.text == "All" }

        carouselAdapter = CarouselAdapter(itemList, requireContext(), object : CarouselAdapter.OnItemClickListener {
            override fun onItemClicked(position: Int) {
                recyclerView.smoothScrollToPosition(position)
            }
        }, initialSelectedPosition)
        recyclerView.adapter = carouselAdapter

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)

        recyclerView.post {
            (recyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(initialSelectedPosition, recyclerView.width / 2 - view.width / 2)
        }
//
//        val middlePosition = Integer.MAX_VALUE / 2
//        val startScrollPosition = middlePosition - (middlePosition % itemList.size) + initialSelectedPosition
//        recyclerView.scrollToPosition(startScrollPosition)
    }

    private fun setupAddButton(view: View) {
        val addButton: Button = view.findViewById(R.id.add_button)
        addButton.bringToFront()
        addButton.setOnClickListener { v ->
            val popup = PopupMenu(requireContext(), v, 0, 0, R.style.CustomPopupMenu)
            popup.menuInflater.inflate(R.menu.add_popup_menu, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.add_manually -> {
                        val formFragment = NewItemsFormFragment()
                        requireActivity().supportFragmentManager.beginTransaction().replace(R.id.fragment_container, formFragment).commit()
                        true
                    }
                    R.id.add_barcode -> {
                        val barcodeFragment = BarcodeScannerFragment()
                        requireActivity().supportFragmentManager.beginTransaction().replace(R.id.fragment_container, barcodeFragment).commit()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }
}