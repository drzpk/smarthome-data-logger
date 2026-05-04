package dev.drzepka.smarthome.logger.pv.source.sofar

import com.intelligt.modbus.jlibmodbus.Modbus
import com.intelligt.modbus.jlibmodbus.master.ModbusMasterRTU
import com.intelligt.modbus.jlibmodbus.serial.SerialParameters
import com.intelligt.modbus.jlibmodbus.serial.SerialPort
import com.intelligt.modbus.jlibmodbus.serial.SerialPortFactoryJSSC
import com.intelligt.modbus.jlibmodbus.serial.SerialUtils
import dev.drzepka.smarthome.logger.core.pipeline.component.DataCollector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SofarModbusDataCollector(
    private val device: String,
    private val slaveId: Int
) : DataCollector<SofarData> {

    private val serialParameters: SerialParameters

    init {
        Modbus.setLogLevel(Modbus.LogLevel.LEVEL_RELEASE)
        SerialUtils.setSerialPortFactory(SerialPortFactoryJSSC())

        serialParameters = SerialParameters().also {
            it.device = device
            it.setBaudRate(SerialPort.BaudRate.BAUD_RATE_9600)
            it.dataBits = 8
            it.parity = SerialPort.Parity.NONE
            it.stopBits = 1
        }
    }

    override suspend fun getData(): Collection<SofarData> = withContext(Dispatchers.IO) {
        val master = ModbusMasterRTU(serialParameters)
        try {
            master.connect()
            val registers = master.readHoldingRegisters(slaveId, 0, 40)
            val data = decodeRegisters(registers) ?: return@withContext emptyList()
            listOf(data)
        } finally {
            if (master.isConnected) master.disconnect()
        }
    }

    private fun decodeRegisters(registers: IntArray): SofarData? {
        // Modbus RTU uses 16-bit registers; convert to byte array (big-endian).
        // Prepend one zero byte to maintain the 1-indexed offset convention of SofarFrame.
        val bytes = ByteArray(registers.size * 2 + 1)
        registers.forEachIndexed { i, reg ->
            bytes[1 + i * 2] = (reg shr 8).toByte()
            bytes[2 + i * 2] = reg.toByte()
        }
        return SofarFrame().decodeResponse(bytes)
    }
}
