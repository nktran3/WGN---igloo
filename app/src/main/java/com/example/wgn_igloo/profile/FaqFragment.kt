package com.example.wgn_igloo.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wgn_igloo.R
import com.example.wgn_igloo.databinding.FragmentFaqBinding

class FaqFragment : Fragment() {
    private lateinit var faqAdapter: FaqAdapter
    private var faqList: List<FaqItem> = listOf(
        FaqItem("How to use the app?", "Here's how you can use the app..."),
        FaqItem("Where to register?", "You can register on the login page..."),
        FaqItem("How do you add members to your fridge", "You can add members by...")
    )
    private var _binding: FragmentFaqBinding? = null
    private val binding get() = _binding!!
    private lateinit var toolbarFaq: Toolbar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFaqBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.faqRecyclerView.layoutManager = LinearLayoutManager(context)
        faqAdapter = FaqAdapter(faqList)
        binding.faqRecyclerView.adapter = faqAdapter
        toolbarFaq = binding.toolbarFaq
        updateToolbar()
    }

    private fun updateToolbar() {
        toolbarFaq.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.back_icon)
        toolbarFaq.setNavigationOnClickListener { activity?.onBackPressed() }

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
