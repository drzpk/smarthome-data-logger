package dev.drzepka.smarthome.logger.pv.source.sofar

import dev.drzepka.smarthome.logger.core.frame.Frame
import dev.drzepka.smarthome.logger.core.frame.modbus.ModbusDataFrameFactory
import dev.drzepka.smarthome.logger.core.frame.modbus.ModbusFrame
import dev.drzepka.smarthome.logger.core.frame.modbus.ModbusRegisterData
import dev.drzepka.smarthome.logger.core.transport.SocketClient
import dev.drzepka.smarthome.logger.pv.common.SocketDataCollector

class SofarWifiDataCollector(
    client: SocketClient,
    deviceSn: Long
) : SocketDataCollector(client, deviceSn) {

    private val dataFrames = ModbusDataFrameFactory(SofarRegisters.registers).createDataFrames()

    override fun createModbusFrames(): List<Frame<ModbusRegisterData>> =
        dataFrames.map { ModbusFrame.readHoldingRegisters(1, it) }
}
