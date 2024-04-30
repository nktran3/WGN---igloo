package com.example.wgn_igloo.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.example.wgn_igloo.R

class UserProfileAdapter(private val users: List<String>) : PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(container.context)
        val view = inflater.inflate(R.layout.item_user_profile, container, false)
        val textView: TextView = view.findViewById(R.id.user_text_view)
        textView.text = users[position]
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun getCount(): Int {
        return users.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return "Profile ${position + 1}" // Optionally set a title for each page
    }
}