package com.guillaumepayet.remotenumpad.connection.hid

import com.guillaumepayet.remotenumpad.connection.IHostValidator

class HidHostValidator : IHostValidator {

    override fun isHostValid(address: String): Boolean = address == "VANAHEIM"
}