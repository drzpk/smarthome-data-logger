package dev.drzepka.smarthome.logger.pv.source.sofar

import com.intelligt.modbus.jlibmodbus.Modbus
import com.intelligt.modbus.jlibmodbus.master.ModbusMasterRTU
import com.intelligt.modbus.jlibmodbus.serial.SerialParameters
import com.intelligt.modbus.jlibmodbus.serial.SerialPort
import com.intelligt.modbus.jlibmodbus.serial.SerialPortFactoryJSSC
import com.intelligt.modbus.jlibmodbus.serial.SerialUtils
import dev.drzepka.smarthome.logger.core.frame.modbus.ModbusDataFrame
import dev.drzepka.smarthome.logger.core.frame.modbus.ModbusDataFrameFactory
import dev.drzepka.smarthome.logger.core.pipeline.component.DataCollector
import dev.drzepka.smarthome.logger.pv.common.PvData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SofarModbusDataCollector(
    private val device: String,
    private val slaveId: Int
) : DataCollector<PvData> {

    private val serialParameters: SerialParameters
    private val dataFrames: Collection<ModbusDataFrame>

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

        dataFrames = ModbusDataFrameFactory(SofarRegisters.registers).createDataFrames()
    }

    override suspend fun getData(): Collection<PvData> = withContext(Dispatchers.IO) {
        val master = ModbusMasterRTU(serialParameters)
        try {
            master.connect()
            val registerData = dataFrames.flatMap { frame ->
                val registers = master.readHoldingRegisters(slaveId, frame.startAddress, frame.qty)
                frame.decodeResponse(buildByteArray(registers)).entries
            }.associate { it.key to it.value }
            listOf(PvData(device, registerData))
        } finally {
            if (master.isConnected) master.disconnect()
        }
    }

    private fun buildByteArray(registers: IntArray): ByteArray {
        val bytes = ByteArray(registers.size * 2)
        registers.forEachIndexed { i, reg ->
            bytes[i * 2] = (reg shr 8).toByte()
            bytes[i * 2 + 1] = reg.toByte()
        }
        return bytes
    }
}
