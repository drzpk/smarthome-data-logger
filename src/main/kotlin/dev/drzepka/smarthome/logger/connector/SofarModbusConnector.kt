package dev.drzepka.smarthome.logger.connector

import com.intelligt.modbus.jlibmodbus.serial.SerialParameters
import com.intelligt.modbus.jlibmodbus.serial.SerialPort
import dev.drzepka.smarthome.common.pvstats.model.vendor.SofarData
import dev.drzepka.smarthome.common.pvstats.model.vendor.VendorData
import dev.drzepka.smarthome.logger.connector.base.DataType
import dev.drzepka.smarthome.logger.connector.base.ModbusConnector
import dev.drzepka.smarthome.logger.model.config.source.SofarModbusConfig

/**
 * Tests in Linux:
 * `mbpoll -m rtu -b 9600 -P none -c 48 -t 4:hex /dev/ttyUSB0`
 */
class SofarModbusConnector(config: SofarModbusConfig) : ModbusConnector(config) {
    override val supportedDataTypes = listOf(DataType.METRICS)
    override val serialDevice = config.device
    override val slaveDeviceId = config.slaveId!!
    override val startAddress = 0
    override val registersToRead = 40

    override fun setSerialDeviceParameters(parameters: SerialParameters) {
        parameters.setBaudRate(SerialPort.BaudRate.BAUD_RATE_9600)
        parameters.dataBits = 8
        parameters.parity = SerialPort.Parity.NONE
        parameters.stopBits = 1
    }

    override fun getVendorData(registers: IntArray): VendorData {
        // Modbus RTU uses 16-bit registers, convert interter response to byte array so it's compatible with
        // the existing model.
        val raw = registers.map {
            // Big endian
            listOf((it shr 8).toByte(), it.toByte())
        }.flatten()

        // For some reason I started indexing registers in the SofarData class from 1, so continue this here
        val mutableRaw = raw.toMutableList()
        mutableRaw.add(0, 0)

        return SofarData(mutableRaw.toTypedArray())
    }
}