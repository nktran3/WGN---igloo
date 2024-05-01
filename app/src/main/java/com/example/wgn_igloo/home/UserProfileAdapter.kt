package com.example.wgn_igloo.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.example.wgn_igloo.R

class UserProfileAdapter(
    private val users: List<User>,
    private val userChangeListener: OnUserChangeListener
) : PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(container.context)
        val view = inflater.inflate(R.layout.item_user_profile, container, false)
        val textView: TextView = view.findViewById(R.id.user_text_view)
        textView.text = users[position].username  // Displaying the username
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view === `object`

    override fun getCount(): Int = users.size

    override fun getPageTitle(position: Int): CharSequence? {
        return users[position].username  // Optionally set a title for each page
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        super.setPrimaryItem(container, position, `object`)
        if (position >= 0 && position < users.size) {
            userChangeListener.onUserChanged(users[position].uid)
        }
    }
}