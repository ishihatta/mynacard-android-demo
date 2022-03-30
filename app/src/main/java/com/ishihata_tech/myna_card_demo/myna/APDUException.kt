package com.ishihata_tech.myna_card_demo.myna

class APDUException(val sw1: Byte, val sw2: Byte): Exception()