package dev.drzepka.smarthome.logger.pv.common

import dev.drzepka.smarthome.logger.core.frame.Frame
import dev.drzepka.smarthome.logger.core.frame.modbus.IntModbusRegister
import dev.drzepka.smarthome.logger.core.frame.modbus.ModbusRegisterData
import dev.drzepka.smarthome.logger.core.transport.SocketClient
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*

@ExtendWith(MockitoExtension::class)
internal class SocketDataCollectorTest {

    @Mock
    private lateinit var client: SocketClient

    @Test
    fun `should set deviceId from deviceSn`(): Unit = runBlocking {
        val collector = collector(deviceSn = 987654321L)
        whenever(client.send(any<Frame<ModbusRegisterData>>())).thenReturn(mapOf())

        val result = collector.getData().toList()

        then(result).hasSize(1)
        then(result[0].deviceId).isEqualTo("987654321")
    }

    @Test
    fun `should send one request per modbus frame`(): Unit = runBlocking {
        val collector = collector(frames = listOf(mock(), mock(), mock()))
        whenever(client.send(any<Frame<ModbusRegisterData>>())).thenReturn(mapOf())

        collector.getData()

        verify(client, times(3)).send(any<Frame<ModbusRegisterData>>())
    }

    @Test
    fun `should merge register data from all frames into single PvData`(): Unit = runBlocking {
        val register1 = IntModbusRegister("r1", 100, 2)
        val register2 = IntModbusRegister("r2", 200, 2)
        val collector = collector(frames = listOf(mock(), mock()))
        whenever(client.send(any<Frame<ModbusRegisterData>>()))
            .thenReturn(mapOf(register1 to 10), mapOf(register2 to 20))

        val result = collector.getData().toList()

        then(result).hasSize(1)
        then(result[0].registerData[register1]).isEqualTo(10)
        then(result[0].registerData[register2]).isEqualTo(20)
    }

    @Test
    fun `should treat null response as empty register data`(): Unit = runBlocking {
        val collector = collector(frames = listOf(mock()))
        whenever(client.send(any<Frame<ModbusRegisterData>>())).thenReturn(null)

        val result = collector.getData().toList()

        then(result[0].registerData).isEmpty()
    }

    @Test
    fun `should wrap send exception in IllegalStateException with frame index`(): Unit = runBlocking {
        val collector = collector(frames = listOf(mock(), mock()))
        whenever(client.send(any<Frame<ModbusRegisterData>>()))
            .thenReturn(mapOf())
            .thenThrow(RuntimeException("timeout"))

        val ex = assertThrows<IllegalStateException> { collector.getData() }

        then(ex.message).isEqualTo("Error while sending frame 2/2")
    }

    private fun collector(
        deviceSn: Long = 1L,
        frames: List<Frame<ModbusRegisterData>> = listOf(mock())
    ): SocketDataCollector = object : SocketDataCollector(client, deviceSn) {
        override fun createModbusFrames() = frames
    }
}
