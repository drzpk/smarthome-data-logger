package dev.drzepka.smarthome.logger.pv.source.afore

import dev.drzepka.smarthome.logger.core.frame.Frame
import dev.drzepka.smarthome.logger.core.frame.modbus.IntModbusRegister
import dev.drzepka.smarthome.logger.core.frame.modbus.ModbusRegisterData
import dev.drzepka.smarthome.logger.pv.client.SocketClient
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
internal class AforeDataCollectorTest {

    @Mock
    private lateinit var client: SocketClient

    private lateinit var collector: AforeDataCollector

    @BeforeEach
    fun setup() {
        collector = AforeDataCollector(client, slaveAddress = 1, deviceSN = 123456789L)
    }

    @Test
    fun `should send one request per data frame`() = runBlocking {
        whenever(client.send(any<Frame<ModbusRegisterData>>())).thenReturn(mapOf())

        collector.getData()

        // AforeRegisters splits into 2 frames: addresses 506-560 and 1000-1014
        verify(client, times(2)).send(any<Frame<ModbusRegisterData>>())
        Unit
    }

    @Test
    fun `should merge data from all frames into a single map`() = runBlocking {
        val register1 = IntModbusRegister("r1", 100, 2)
        val register2 = IntModbusRegister("r2", 200, 2)
        val data1: ModbusRegisterData = mapOf(register1 to 10)
        val data2: ModbusRegisterData = mapOf(register2 to 20)
        whenever(client.send(any<Frame<ModbusRegisterData>>())).thenReturn(data1, data2)

        val result = collector.getData().toList()

        Assertions.assertEquals(1, result.size)
        Assertions.assertEquals(123456789L, result[0].sn)
        Assertions.assertEquals(10, result[0].registerData[register1])
        Assertions.assertEquals(20, result[0].registerData[register2])
    }

    @Test
    fun `should return empty map when client returns null`() = runBlocking {
        whenever(client.send(any<Frame<ModbusRegisterData>>())).thenReturn(null)

        val result = collector.getData().toList()

        result.forEach { Assertions.assertEquals(emptyMap<Any, Any>(), it.registerData) }
    }

    @Test
    fun `should wrap send exception in IllegalStateException`() = runBlocking {
        whenever(client.send(any<Frame<ModbusRegisterData>>())).thenThrow(RuntimeException("connection failed"))

        val ex = assertThrows<IllegalStateException> { collector.getData() }
        Assertions.assertEquals("Error while sending frame 1/2", ex.message)
    }
}