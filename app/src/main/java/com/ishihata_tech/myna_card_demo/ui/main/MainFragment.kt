package com.ishihata_tech.myna_card_demo.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ishihata_tech.myna_card_demo.R
import com.ishihata_tech.myna_card_demo.databinding.MainFragmentBinding

class MainFragment : Fragment() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<MainFragmentBinding>(
            inflater,
            R.layout.main_fragment,
            container,
            false
        ).also {
            it.lifecycleOwner = this
            it.viewModel = viewModel
        }

        binding.buttonTextAp.setOnClickListener {
            findNavController().navigate(
                MainFragmentDirections.actionMainFragmentToTextApFragment()
            )
        }
        binding.buttonSetAuthCert.setOnClickListener {
            findNavController().navigate(
                MainFragmentDirections.actionMainFragmentToSetAuthCertificateFragment()
            )
        }
        binding.buttonAuthTest.setOnClickListener {
            findNavController().navigate(
                MainFragmentDirections.actionMainFragmentToAuthFragment()
            )
        }
        return binding.root
    }
}