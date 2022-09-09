package com.guillaumepayet.remotenumpad.settings.socket

/**
 * A listener to the socket host scanning process.
 *
 * @see SocketHostScanner
 */
interface ISocketHostScanListener {

    /**
     * Method called when a host scan is started.
     */
    fun onScanStarted()

    /**
     * Method called when the host scan is completed and a list of hosts is passed through.
     *
     * @param hosts a list of hosts as pairs of (label, address)
     */
    fun onScanResults(hosts: Iterable<Pair<String, String>>)
}