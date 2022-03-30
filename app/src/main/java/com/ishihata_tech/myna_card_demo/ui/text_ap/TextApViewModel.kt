package com.ishihata_tech.myna_card_demo.ui.text_ap

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ishihata_tech.myna_card_demo.myna.Reader
import com.ishihata_tech.myna_card_demo.myna.TextAP
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class TextApViewModel : ViewModel() {
    enum class ResultStatus {
        SUCCESS,
        ERROR_TRY_COUNT_IS_NOT_LEFT,
        ERROR_INSUFFICIENT_PIN,
        ERROR_INCORRECT_PIN,
        ERROR_CONNECTION,
    }
    data class Result(
        val status: ResultStatus,
        val tryCountRemain: Int? = null,
        val myNumber: String? = null,
        val attributes: TextAP.Attributes? = null,
    )

    val pin = MutableLiveData("")

    private val _result = MutableSharedFlow<Result>()
    val result: SharedFlow<Result> = _result

    fun onConnect(reader: Reader) {
        val resultData = try {
            procedure(reader)
        } catch (e: Exception) {
            Result(ResultStatus.ERROR_CONNECTION)
        }

        viewModelScope.launch {
            _result.emit(resultData)
        }
    }

    private fun procedure(reader: Reader): Result {
        // PIN取得
        val pin = pin.value ?: ""
        if (pin.length != 4) {
            return Result(ResultStatus.ERROR_INSUFFICIENT_PIN)
        }

        // AP選択
        val textAP = reader.selectTextAp()

        // PINの残りカウント取得
        val count = textAP.lookupPin()
        if (count == 0) {
            return Result(ResultStatus.ERROR_TRY_COUNT_IS_NOT_LEFT)
        }

        // PIN解除
        if (!textAP.verifyPin(pin)) {
            return Result(ResultStatus.ERROR_INCORRECT_PIN, count - 1)
        }

        // マイナンバー取得
        val myNumber = textAP.readMyNumber()

        // その他の情報を取得
        val attributes = textAP.readAttributes()

        return Result(ResultStatus.SUCCESS, count, myNumber, attributes)
    }
}