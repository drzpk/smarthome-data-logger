package dev.drzepka.smarthome.logger.pvstats.connector.base

import com.intelligt.modbus.jlibmodbus.Modbus
import com.intelligt.modbus.jlibmodbus.master.ModbusMasterRTU
import com.intelligt.modbus.jlibmodbus.serial.SerialParameters
import com.intelligt.modbus.jlibmodbus.serial.SerialPortFactoryJSSC
import com.intelligt.modbus.jlibmodbus.serial.SerialUtils
import dev.drzepka.smarthome.common.pvstats.model.vendor.VendorData
import dev.drzepka.smarthome.common.util.Logger
import dev.drzepka.smarthome.logger.pvstats.model.config.source.SourceConfig
import jssc.SerialPortList
import java.io.File

abstract class ModbusConnector(private val config: SourceConfig) : Connector {

    abstract val serialDevice: String
    abstract val slaveDeviceId: Int
    abstract val startAddress: Int
    abstract val registersToRead: Int

    private lateinit var serialParameters: SerialParameters
    private val log by Logger()

    override fun initialize() {
        Modbus.setLogLevel(Modbus.LogLevel.LEVEL_DEBUG)
        SerialUtils.setSerialPortFactory(SerialPortFactoryJSSC())

        val device = serialDevice
        checkSerialDevice(device)

        serialParameters = SerialParameters()
        setSerialDeviceParameters(serialParameters)
        serialParameters.device = device
    }

    final override fun getData(dataType: DataType, silent: Boolean): VendorData? {
        val rtu = getModbusMasterRTU()
        if (!rtu.isConnected)
            rtu.connect()

        val registers = rtu.readHoldingRegisters(slaveDeviceId, startAddress, registersToRead)
        rtu.disconnect()

        return getVendorData(registers)
    }

    abstract fun getVendorData(registers: IntArray): VendorData

    protected abstract fun setSerialDeviceParameters(parameters: SerialParameters)

    protected open fun getModbusMasterRTU(): ModbusMasterRTU = ModbusMasterRTU(serialParameters)

    private fun checkSerialDevice(device: String) {
        val platform = System.getProperty("os.name")!!
        when (platform.toLowerCase()) {
            "linux" -> checkSerialDeviceLinux(device)
            "windows" -> checkSerialDeviceWindows(device)
            else -> log.warn("Skipping checking serial device on unknown platform: {}", platform)
        }
    }

    private fun checkSerialDeviceLinux(device: String) {
        val file = File(device)
        if (!file.exists())
            throw IllegalArgumentException("Serial device $device is not available")
    }

    private fun checkSerialDeviceWindows(device: String) {
        // todo: check if this works on windows, because on linux it doesn't

        val portNames = SerialPortList.getPortNames()
        val exists = portNames.any { it == device }

        if (!exists) {
            val names = if (portNames.isNotEmpty()) portNames.reduce { acc, s -> "$acc, $s" } else "<none>"
            throw IllegalArgumentException("Serial device $device is not available (source ${config.name}). Available devices: $names")
        }
    }

}