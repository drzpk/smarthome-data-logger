package dev.drzepka.smarthome.logger.pv.client

import dev.drzepka.smarthome.logger.core.frame.Frame
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import java.net.ServerSocket
import java.time.Duration
import kotlin.concurrent.thread

internal class SocketClientTest {

    @Test
    fun `should send request and return decoded response`() = runBlocking {
        val requestBytes = byteArrayOf(0x01, 0x02, 0x03)
        val responseBytes = byteArrayOf(0x04, 0x05, 0x06, 0x07)

        val serverSocket = ServerSocket(0) // OS assigns a free port
        val serverThread = thread {
            serverSocket.accept().use { socket ->
                // drain the incoming request
                val buf = ByteArray(1024)
                socket.getInputStream().read(buf)
                // write response
                socket.getOutputStream().write(responseBytes)
                socket.getOutputStream().flush()
            }
        }

        try {
            val client = SocketClient("localhost", serverSocket.localPort, Duration.ofSeconds(5))
            val result = client.send(object : Frame<ByteArray> {
                override fun encodeRequest() = requestBytes
                override fun decodeResponse(content: ByteArray) = content
            })
            assertArrayEquals(responseBytes, result)
        } finally {
            serverSocket.close()
            serverThread.join(1000)
        }
    }

    @Test
    fun `should pass encoded request bytes to the server`() = runBlocking {
        val requestBytes = byteArrayOf(0xAA.toByte(), 0xBB.toByte(), 0xCC.toByte())
        var receivedRequest: ByteArray? = null

        val serverSocket = ServerSocket(0)
        val serverThread = thread {
            serverSocket.accept().use { socket ->
                val buf = ByteArray(1024)
                val n = socket.getInputStream().read(buf)
                receivedRequest = buf.copyOf(n)
                socket.getOutputStream().write(byteArrayOf(0x00)) // minimal response
                socket.getOutputStream().flush()
            }
        }

        try {
            val client = SocketClient("localhost", serverSocket.localPort, Duration.ofSeconds(5))
            client.send(object : Frame<Unit> {
                override fun encodeRequest() = requestBytes
                override fun decodeResponse(content: ByteArray) = Unit
            })
            serverThread.join(1000)
            assertArrayEquals(requestBytes, receivedRequest)
        } finally {
            serverSocket.close()
        }
    }
}
