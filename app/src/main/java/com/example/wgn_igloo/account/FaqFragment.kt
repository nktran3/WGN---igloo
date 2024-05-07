package com.example.wgn_igloo.account

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wgn_igloo.R
import com.example.wgn_igloo.databinding.FragmentFaqBinding

class FaqFragment : Fragment() {
    private lateinit var faqAdapter: FaqAdapter

    // Preset list of questions for FAQ
    private var faqList: List<FaqItem> = listOf(
        FaqItem("How does igloo help reduce food waste?", "igloo helps reduce food waste by allowing users to keep track of the items in their fridge, including their expiration dates. With features like notifications for upcoming expirations and recipe suggestions based on fridge contents, users can better plan their meals and use up ingredients before they go to waste."),
        FaqItem("How do you add items to your fridge?", "You can either manually add items to your fridge inventory or you can use the barcode scanning feature to add items to your fridge inventory in igloo."),
        FaqItem("How do you manage the items in your fridge?", "You can manage items in your fridge by clicking the item you want to manage, when you do this the item's details will show and from their you can either edit, delete, or add your items to your grocery list."),
        FaqItem("Is the barcode scanner feature available for all items?", "The barcode scanner feature in igloo works for most items with barcodes. However, some unique or specialty items may not be recognized by the scanner."),
        FaqItem("How does the recipe search feature work?", "The recipe search feature in igloo allows users to input the ingredients they have in their fridge, and the app will generate recipe suggestions based on those ingredients. It allows users to search up recipes that they want to make or save for later. igloo also suggests recipes for users based on what they already have in their fridge, so no extra searching is needed."),
        FaqItem("Can I share my fridge inventory with others?", "Yes, igloo allows users to share their fridge inventory with friends, family, or roommates. This feature is especially useful for those living in shared spaces to keep track of food ownership and prevent confusion. Users are also able to share items with their friends or request to borrow items from their friend's inventory."),
        FaqItem("How do notifications work in igloo?", "igloo's notification system keeps users informed about upcoming expiration dates, borrowing requests, and request responses."),
        FaqItem("How does the grocery list feature work?", "Users are able to add items to their grocery list for when they go grocery shopping. Once the user buys the items in their grocery list, they can easily add it to their inventory by clicking the check box. Users can either add items to their grocery list using the input form or by clicking the add to grocery list button from their inventory."),
        FaqItem("Is igloo available on both iOS and Android devices?", "Currently, igloo is only available for Android devices, but we are currently working to make it available for iOS devices."),
        FaqItem("How can I provide feedback or report issues with igloo?", "We welcome feedback from our users! You can provide feedback or report any issues with igloo by clicking the support button available on the accounts page. This will lead you to a form that you can use to send your feedback or report issues. Our team works very hard to respond to feed back in a timely manner."),
    )

    // Binding to xml layout
    private var _binding: FragmentFaqBinding? = null
    private val binding get() = _binding!!

    // Toolbar initialization
    private lateinit var toolbarFaq: Toolbar
    private lateinit var toolbarFaqTitle: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFaqBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup RecyclerView
        binding.faqRecyclerView.layoutManager = LinearLayoutManager(context)
        faqAdapter = FaqAdapter(faqList)
        binding.faqRecyclerView.adapter = faqAdapter

        // Setup toolbar
        toolbarFaq = binding.toolbarFaq
        toolbarFaqTitle = binding.toolbarFaqTitle
        updateToolbar()
    }


    // Update toolbar with back button and page title
    private fun updateToolbar() {
        toolbarFaq.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.back_icon)
        toolbarFaq.setNavigationOnClickListener { activity?.onBackPressed() }
        toolbarFaqTitle.text = "Frequently Asked Questions"
    }

    // Cleanup binding on destroyed view
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
