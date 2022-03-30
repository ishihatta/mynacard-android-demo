package com.ishihata_tech.myna_card_demo.ui.auth

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ishihata_tech.myna_card_demo.certification_service.CertificationService
import com.ishihata_tech.myna_card_demo.myna.Reader
import com.ishihata_tech.myna_card_demo.myna.TextAP
import com.ishihata_tech.myna_card_demo.myna.hexToByteArray
import com.ishihata_tech.myna_card_demo.myna.toHexString
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest

class AuthViewModel : ViewModel() {
    companion object {
        private const val LOG_TAG = "AuthViewModel"
    }

    enum class ResultStatus {
        SUCCESS,
        ERROR_CERTIFICATE_NOT_SET,
        ERROR_FAILURE_TO_VERIFY_SIGNATURE,
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

    private lateinit var certificationService: CertificationService

    val pin = MutableLiveData("")

    private val _result = MutableSharedFlow<Result>()
    val result: SharedFlow<Result> = _result

    fun onCreate(context: Context) {
        certificationService = CertificationService(context)
    }

    fun onConnect(reader: Reader) {
        val resultData = try {
            procedure(reader)
        } catch (e: Exception) {
            Log.d(LOG_TAG, "onConnect: exception", e)
            Result(ResultStatus.ERROR_CONNECTION)
        }

        viewModelScope.launch {
            _result.emit(resultData)
        }
    }

    private fun procedure(reader: Reader): Result {
        // 認証サービスから nonce を取得する
        val nonce = certificationService.requestCertification()
        Log.d(LOG_TAG, "nonce: ${nonce.toHexString()}")

        // nonce に署名する
        val (signature, result) = computeSignature(reader, nonce)
        result?.also {
            return it
        }

        // 認証サービスに署名を送って検証してもらう
        val verifyResult = certificationService.verifySignature(signature!!)

        return if (verifyResult) {
            Result(ResultStatus.SUCCESS)
        } else {
            Result(ResultStatus.ERROR_FAILURE_TO_VERIFY_SIGNATURE)
        }
    }

    private fun computeSignature(reader: Reader, data: ByteArray): Pair<ByteArray?, Result?> {
        // PIN取得
        val pin = pin.value ?: ""
        if (pin.length != 4) {
            return Pair(null, Result(ResultStatus.ERROR_INSUFFICIENT_PIN))
        }

        // AP選択
        val jpkiAP = reader.selectJpkiAp()

        // PINの残りカウント取得
        val count = jpkiAP.lookupAuthPin()
        if (count == 0) {
            return Pair(null, Result(ResultStatus.ERROR_TRY_COUNT_IS_NOT_LEFT))
        }

        // PIN解除
        if (!jpkiAP.verifyAuthPin(pin)) {
            return Pair(null, Result(ResultStatus.ERROR_INCORRECT_PIN, count - 1))
        }

        // 署名対象のデータを SHA-1 でハッシュ化
        val digest = MessageDigest.getInstance("SHA-1").digest(data)
        Log.d(LOG_TAG, "digest: ${digest.toHexString()}")

        // ハッシュ値を DigestInfo の形式に変換する
        val header = "3021300906052B0E03021A05000414"
        val digestInfo = header.hexToByteArray() + digest

        // カードの秘密鍵で署名する
        val signature = jpkiAP.authSignature(digestInfo)
        Log.d(LOG_TAG, "signature: ${signature.toHexString()}")
        return Pair(signature, null)
    }
}