package dev.drzepka.smarthome.logger.connector.base

import dev.drzepka.smarthome.common.pvstats.model.vendor.SofarData
import dev.drzepka.smarthome.common.pvstats.model.vendor.VendorData
import dev.drzepka.smarthome.common.util.hexStringToBytes
import dev.drzepka.smarthome.logger.PVStatsDataLogger
import dev.drzepka.smarthome.logger.model.config.source.SourceConfig
import dev.drzepka.smarthome.logger.util.Logger
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException

abstract class SocketConnector(private val config: SourceConfig) : Connector {

    private val log by Logger()

    override fun initialize() = Unit

    @Suppress("ConstantConditionIf")
    final override fun getData(dataType: DataType, silent: Boolean): VendorData? {
        if (PVStatsDataLogger.DEBUG) return getTestVendorData()

        val split = splitSocketUrl(getUrl(dataType))
        val socket = Socket()
        try {
            socket.connect(InetSocketAddress(split.first, split.second), config.timeout * 1000)
        } catch (e: SocketTimeoutException) {
            if (!silent)
                log.warning("Connection to source ${config.name} timed out (${split.first}:${split.second}")
            return null
        }

        socket.getOutputStream().write(getSocketRequestData().toByteArray())
        val inputStream = socket.getInputStream()

        var responseWaitTime = 0L
        while (inputStream.available() == 0) {
            Thread.sleep(SOCKET_RESPONSE_SLEEP_TIME)
            if (responseWaitTime > config.timeout * 1000L) {
                log.warning("Timeout occurred while waiting for source ${config.name} response data")
                socket.close()
                return null
            }
            responseWaitTime += SOCKET_RESPONSE_SLEEP_TIME
        }

        val buffer = ByteArray(inputStream.available())
        inputStream.read(buffer)
        inputStream.close()
        socket.close()

        if (buffer.size != 110) {
            if (!silent)
                log.warning("Response from source ${config.name} does not appear to contain inverter data. " +
                        "Did you supplied correct SN?")
            return null
        }

        val byteArray = buffer.toTypedArray()
        return parseSocketResponseData(byteArray)
    }

    abstract fun getUrl(dataType: DataType): String

    abstract fun getSocketRequestData(): Array<Byte>

    abstract fun parseSocketResponseData(response: Array<Byte>): VendorData

    private fun splitSocketUrl(url: String): Pair<String, Int> {
        val split = url.split(":")
        if (split.size != 2) throw IllegalArgumentException("Malformed url")
        return Pair(split[0], split[1].toInt())
    }

    private fun getTestVendorData(): VendorData {
        val bytes = hexStringToBytes("a5610010150072f3a0386602018e8002009c2400006232b4" +
                "5e01034e0002000000000000000000000f22027d0317000100f7000000f00041138609890158096901580953015700" +
                "0000400000002c093302800026003219e00f18031d003c000000010000054d087206cdccad0315")

        return SofarData(bytes.copyOfRange(27, bytes.size))
    }

    companion object {
        private const val SOCKET_RESPONSE_SLEEP_TIME = 100L

    }
}