package dev.drzepka.smarthome.logger.pv.client

import dev.drzepka.smarthome.common.util.Logger
import dev.drzepka.smarthome.logger.core.frame.Frame
import dev.drzepka.smarthome.logger.core.util.HexUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.time.Duration

// todo: tests
class SocketClient(private val host: String, private val port: Int, private val timeout: Duration) {

    suspend fun <T> send(frame: Frame<T>): T? = withContext(Dispatchers.IO) {
        val encoded = frame.encodeRequest()
        logger.trace("Sending frame to {}:{}: {}", host, port, HexUtils.byteArrayToHex(encoded, true))

        val socket = Socket()
        socket.connect(InetSocketAddress(host, port), timeout.toMillis().toInt())
        socket.getOutputStream().write(encoded)

        val inputStream = socket.getInputStream()
        waitForData(inputStream)

        val array = ByteArray(inputStream.available())
        inputStream.read(array)
        inputStream.close()
        socket.close()

        logger.trace("Received response from {}:{}: {}", host, port, HexUtils.byteArrayToHex(array, true))
        frame.decodeResponse(array)
    }

    private suspend fun waitForData(inputStream: InputStream): Boolean = withContext(Dispatchers.IO) {
        var waitTime = 0L
        while (inputStream.available() == 0) {
            delay(SOCKET_RESPONSE_SLEEP_TIME)
            if (waitTime > timeout.toMillis() * 1000L) {
                logger.warn("Timeout occurred while waiting for response from {}:{}", host, port)
                return@withContext false
            }

            waitTime += SOCKET_RESPONSE_SLEEP_TIME
        }

        true
    }

    companion object {
        private val logger by Logger()
        private const val SOCKET_RESPONSE_SLEEP_TIME = 100L
    }
}
