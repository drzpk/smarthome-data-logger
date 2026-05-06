package dev.drzepka.smarthome.logger.pv.source.sofar

import dev.drzepka.smarthome.logger.core.frame.Frame
import dev.drzepka.smarthome.logger.core.frame.modbus.ModbusRegisterData
import dev.drzepka.smarthome.logger.core.transport.SocketClient
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*

@ExtendWith(MockitoExtension::class)
internal class SofarWifiDataCollectorTest {

    @Mock
    private lateinit var client: SocketClient

    private lateinit var collector: SofarWifiDataCollector

    @BeforeEach
    fun setup() {
        collector = SofarWifiDataCollector(client, deviceSn = 111222333L)
    }

    @Test
    fun `should send exactly one frame for Sofar registers`(): Unit = runBlocking {
        whenever(client.send(any<Frame<ModbusRegisterData>>())).thenReturn(mapOf())

        collector.getData()

        verify(client, times(1)).send(any<Frame<ModbusRegisterData>>())
    }

    @Test
    fun `should set deviceId from deviceSn`(): Unit = runBlocking {
        whenever(client.send(any<Frame<ModbusRegisterData>>())).thenReturn(mapOf())

        val result = collector.getData().toList()

        then(result[0].deviceId).isEqualTo("111222333")
    }

    @Test
    fun `should use holding register function code`(): Unit = runBlocking {
        whenever(client.send(any<Frame<ModbusRegisterData>>())).thenReturn(mapOf())
        val captor = argumentCaptor<Frame<ModbusRegisterData>>()

        collector.getData()
        verify(client).send(captor.capture())

        // SolarmanV5Frame layout: 11 bytes header + 15 bytes payload header + modbus frame
        // ModbusFrame: byte 0 = slave address, byte 1 = function code
        val encoded = captor.firstValue.encodeRequest()
        then(encoded[27].toInt() and 0xFF).isEqualTo(3) // 3 = read holding registers
    }
}
