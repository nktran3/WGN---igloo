package com.example.wgn_igloo.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.example.wgn_igloo.R
import com.example.wgn_igloo.databinding.FragmentAboutBinding
import com.example.wgn_igloo.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var toolbarSettings: Toolbar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbarSettings = binding.toolbarSettings
        updateToolbar()
    }
    private fun updateToolbar() {
        toolbarSettings.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.back_icon)
        toolbarSettings.setNavigationOnClickListener { activity?.onBackPressed() }

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
