package com.ishihata_tech.myna_card_demo.ui.set_auth_certificate

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ishihata_tech.myna_card_demo.myna.Reader
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.security.cert.X509Certificate

class SetAuthCertificateViewModel : ViewModel() {
    companion object {
        private const val LOG_TAG = "SetAuthCertificateViewModel"
    }

    enum class ResultStatus {
        SUCCESS,
        ERROR_CONNECTION,
    }
    data class Result(
        val status: ResultStatus,
        val certificate: X509Certificate? = null,
    )

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
        // AP選択
        val jpkiAP = reader.selectJpkiAp()

        // 認証用証明書取得
        val cert = jpkiAP.readAuthCertificate()
        Log.d(LOG_TAG, "cert: $cert")

        return Result(ResultStatus.SUCCESS, cert)
    }
}