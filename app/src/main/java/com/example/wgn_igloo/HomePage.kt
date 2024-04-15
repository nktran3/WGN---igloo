package com.example.wgn_igloo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.constraintlayout.helper.widget.Carousel
import androidx.fragment.app.Fragment


class HomePage : Fragment() {


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

        val carousel: Carousel = view.findViewById(R.id.carousel)

        val carouselItems = listOf(
            R.drawable.condiments_icon,
            R.id.spacer1,
            R.drawable.dairy_icon,
            R.id.spacer2,
            R.drawable.drinks_icon,
            R.id.spacer3,
            R.drawable.freezer_icon,
            R.id.spacer4,
            R.drawable.meats_icon,
            R.id.spacer5,
            R.drawable.produce_icon,
            R.id.spacer6,
            R.drawable.miscellaneous_icon
        )

        carousel.setAdapter(object : Carousel.Adapter {
            override fun count(): Int {
                return carouselItems.size
            }

            override fun populate(view: View?, index: Int) {
                if (view is ImageView) {
                    view.setImageResource(carouselItems[index])
                    view.setOnClickListener {
                        Toast.makeText(context, "Item at index $index clicked", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onNewItem(index: Int) {
                // complete
            }
        })

        val addButton : Button = view.findViewById(R.id.add_button)
        addButton.bringToFront();

        addButton.setOnClickListener{
            val formFragment = NewItemsFormFragment()
            requireActivity().supportFragmentManager.beginTransaction().replace(R.id.fragment_container, formFragment).commit()



        }





    }
}