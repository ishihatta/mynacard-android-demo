package com.ishihata_tech.myna_card_demo.myna

class ASN1Frame(
    val tag: Int,
    val length: Int,
    val frameSize: Int,
    val value: ByteArray? = null
)