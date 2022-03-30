package com.ishihata_tech.myna_card_demo.certification_service

import android.content.Context
import androidx.core.content.edit
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.security.cert.X509Certificate
import java.util.*

object AuthCertificateRepository {
    private const val SHARED_PREF_NAME = "AuthCertificateRepository"
    private const val TAG_NAME = "cert"

    fun store(context: Context, certificate: X509Certificate) {
        // 証明書をシリアライズする
        val byteArrayOutputStream = ByteArrayOutputStream()
        ObjectOutputStream(byteArrayOutputStream).use {
            it.writeObject(certificate)
        }
        // BASE64に変換する
        val certString = Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray())
        // SharedPreferences に保存する
        context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
            .edit {
                putString(TAG_NAME, certString)
            }
    }

    fun fetch(context: Context): X509Certificate? {
        // SharedPreferences から文字列を読み込む
        val base64String = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
            .getString(TAG_NAME, null) ?: return null
        // BASE64デコード
        val obj = Base64.getDecoder().decode(base64String)
        // 証明書にデシリアライズ
        return ObjectInputStream(ByteArrayInputStream(obj)).readObject() as? X509Certificate
    }
}