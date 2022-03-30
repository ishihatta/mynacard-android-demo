package com.ishihata_tech.myna_card_demo.ui.set_auth_certificate

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.ishihata_tech.myna_card_demo.R
import com.ishihata_tech.myna_card_demo.certification_service.CertificationService
import com.ishihata_tech.myna_card_demo.databinding.SetAuthCertificateFragmentBinding
import com.ishihata_tech.myna_card_demo.myna.Reader
import com.ishihata_tech.myna_card_demo.ui.common.CommunicateDialogFragment
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SetAuthCertificateFragment : Fragment(), CommunicateDialogFragment.OnConnectListener {
    private val viewModel: SetAuthCertificateViewModel by viewModels()
    private lateinit var binding: SetAuthCertificateFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate<SetAuthCertificateFragmentBinding>(
            inflater,
            R.layout.set_auth_certificate_fragment,
            container,
            false
        ).also {
            it.lifecycleOwner = this
            it.viewModel = viewModel
        }

        // 接続ボタン
        binding.buttonStart.setOnClickListener {
            CommunicateDialogFragment.show(childFragmentManager)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.result.collect {
                onResult(it)
            }
        }

        return binding.root
    }

    override fun onConnect(reader: Reader) {
        viewModel.onConnect(reader)
    }

    private fun onResult(result: SetAuthCertificateViewModel.Result) {
        binding.textResult.text = when (result.status) {
            SetAuthCertificateViewModel.ResultStatus.SUCCESS -> result.certificate?.toString()
            SetAuthCertificateViewModel.ResultStatus.ERROR_CONNECTION -> "正常に通信できませんでした"
        }

        // 証明書を保存する
        if (result.status == SetAuthCertificateViewModel.ResultStatus.SUCCESS) {
            result.certificate?.also {
                CertificationService(requireContext()).registerCertificate(it)
            }
        }
    }
}