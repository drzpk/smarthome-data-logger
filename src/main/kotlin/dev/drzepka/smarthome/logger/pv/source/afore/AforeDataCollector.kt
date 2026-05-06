package dev.drzepka.smarthome.logger.pv.source.afore

import dev.drzepka.smarthome.logger.core.frame.Frame
import dev.drzepka.smarthome.logger.core.frame.modbus.ModbusDataFrameFactory
import dev.drzepka.smarthome.logger.core.frame.modbus.ModbusFrame
import dev.drzepka.smarthome.logger.core.frame.modbus.ModbusRegisterData
import dev.drzepka.smarthome.logger.core.transport.SocketClient
import dev.drzepka.smarthome.logger.pv.common.SocketDataCollector

class AforeDataCollector(
    client: SocketClient,
    private val slaveAddress: Int,
    deviceSN: Long,
) : SocketDataCollector(client, deviceSN) {

    private val dataFrames = ModbusDataFrameFactory(AforeT6Registers.registers).createDataFrames()

    override fun createModbusFrames(): List<Frame<ModbusRegisterData>> =
        dataFrames.map { ModbusFrame.readInputRegisters(slaveAddress, it) }
}
