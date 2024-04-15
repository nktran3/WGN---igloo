package com.example.wgn_igloo

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.wgn_igloo.databinding.FragmentNewItemsFormBinding

private const val TAG = "NewItemsForm"

class NewItemsFormFragment : Fragment() {

    private var _binding: FragmentNewItemsFormBinding? = null
    private val binding get() = _binding!!

    var message: String? = null

    companion object {
        private const val EXTRA_MESSAGE = "EXTRA_MESSAGE"

        fun newInstance(message: String): NewItemsFormFragment {
            val fragment = NewItemsFormFragment()
            val args = Bundle()
            args.putString(EXTRA_MESSAGE, message)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            message = it.getString(EXTRA_MESSAGE)
            Log.d(TAG, "Testing to see if the data went through: $message")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewItemsFormBinding.inflate(inflater, container, false)

        binding.itemInput.setText(message)


        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // Avoid memory leaks
    }
}