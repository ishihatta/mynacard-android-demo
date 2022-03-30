package com.ishihata_tech.myna_card_demo.ui.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.ishihata_tech.myna_card_demo.R
import com.ishihata_tech.myna_card_demo.databinding.AuthFragmentBinding
import com.ishihata_tech.myna_card_demo.myna.Reader
import com.ishihata_tech.myna_card_demo.ui.common.CommunicateDialogFragment
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class AuthFragment : Fragment(), CommunicateDialogFragment.OnConnectListener {
    private val viewModel: AuthViewModel by viewModels()
    private lateinit var binding: AuthFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.onCreate(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate<AuthFragmentBinding>(
            inflater,
            R.layout.auth_fragment,
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

    private fun onResult(result: AuthViewModel.Result) {
        binding.textResult.text = when(result.status) {
            AuthViewModel.ResultStatus.SUCCESS -> "認証に成功しました"
            AuthViewModel.ResultStatus.ERROR_FAILURE_TO_VERIFY_SIGNATURE -> "認証に失敗しました"
            AuthViewModel.ResultStatus.ERROR_CERTIFICATE_NOT_SET -> "証明書が登録されていません"
            AuthViewModel.ResultStatus.ERROR_INCORRECT_PIN -> {
                "暗証番号が間違っています\nあと ${result.tryCountRemain} 回試せます"
            }
            AuthViewModel.ResultStatus.ERROR_INSUFFICIENT_PIN -> "暗証番号は 4 文字です"
            AuthViewModel.ResultStatus.ERROR_TRY_COUNT_IS_NOT_LEFT -> "暗証番号がロックされています"
            AuthViewModel.ResultStatus.ERROR_CONNECTION -> "正常に通信できませんでした"
        }
    }
}