package com.ishihata_tech.myna_card_demo.certification_service

import android.content.Context
import java.security.SecureRandom
import java.security.Signature
import java.security.cert.X509Certificate

class CertificationService(private val context: Context) {
    private var nonce: ByteArray? = null

    /**
     * ユーザの公開鍵を含む証明書を登録する
     *
     * @param certificate ユーザの証明書
     */
    fun registerCertificate(certificate: X509Certificate) {
        AuthCertificateRepository.store(context, certificate)
    }

    /**
     * 認証の開始を要求する
     * nonce が返却される
     *
     * @return nonce
     */
    fun requestCertification(): ByteArray {
        nonce = ByteArray(20)
        SecureRandom().nextBytes(nonce)
        return nonce!!
    }

    /**
     * 署名を検証する
     *
     * @param signature 署名（nonceを秘密鍵で暗号化したもの）
     * @return 認証に成功したら true, 失敗したら false
     */
    fun verifySignature(signature: ByteArray): Boolean {
        val nonce = nonce ?: return false
        val certificate = AuthCertificateRepository.fetch(context) ?: return false
        return Signature.getInstance("SHA1withRSA").apply {
            initVerify(certificate.publicKey)
            update(nonce)
        }.verify(signature)
    }
}