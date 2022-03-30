package com.ishihata_tech.myna_card_demo.ui.text_ap

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.ishihata_tech.myna_card_demo.R
import com.ishihata_tech.myna_card_demo.databinding.TextApFragmentBinding
import com.ishihata_tech.myna_card_demo.myna.Reader
import com.ishihata_tech.myna_card_demo.ui.common.CommunicateDialogFragment
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class TextApFragment : Fragment(), CommunicateDialogFragment.OnConnectListener {
    private val viewModel: TextApViewModel by viewModels()
    private lateinit var binding: TextApFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate<TextApFragmentBinding>(
            inflater,
            R.layout.text_ap_fragment,
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

    private fun onResult(result: TextApViewModel.Result) {
        val resultString: String = when(result.status) {
            TextApViewModel.ResultStatus.SUCCESS -> {
                listOf(
                    "個人番号: ${result.myNumber}",
                    "名前: ${result.attributes?.name ?: ""}",
                    "住所: ${result.attributes?.address ?: ""}",
                    "生年月日: ${result.attributes?.birth ?: ""}",
                    "性別: ${result.attributes?.sex ?: ""}",
                ).joinToString("\n")
            }
            TextApViewModel.ResultStatus.ERROR_TRY_COUNT_IS_NOT_LEFT -> {
                "暗証番号がロックされています"
            }
            TextApViewModel.ResultStatus.ERROR_INCORRECT_PIN -> {
                "暗証番号が間違っています\nあと ${result.tryCountRemain} 回試せます"
            }
            TextApViewModel.ResultStatus.ERROR_INSUFFICIENT_PIN -> {
                "暗証番号は 4 文字です"
            }
            TextApViewModel.ResultStatus.ERROR_CONNECTION -> {
                "正常に通信できませんでした"
            }
        }
        binding.textResult.text = resultString
    }
}