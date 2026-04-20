package dev.drzepka.smarthome.logger.pv.pipeline.collector

import dev.drzepka.smarthome.common.util.Logger
import dev.drzepka.smarthome.logger.core.frame.Frame
import dev.drzepka.smarthome.logger.core.frame.modbus.ModbusDataFrameFactory
import dev.drzepka.smarthome.logger.core.frame.modbus.ModbusFrame
import dev.drzepka.smarthome.logger.core.frame.modbus.ModbusRegisterData
import dev.drzepka.smarthome.logger.core.pipeline.component.DataCollector
import dev.drzepka.smarthome.logger.pv.client.SocketClient
import dev.drzepka.smarthome.logger.pv.model.AforeData
import dev.drzepka.smarthome.logger.pv.vendor.SolarmanV5Frame
import dev.drzepka.smarthome.logger.pv.vendor.afore.AforeT6Registers

class AforeDataCollector(
    private val client: SocketClient,
    private val slaveAddress: Int,
    private val deviceSN: Long,
) : DataCollector<AforeData> {

    private val dataFrames = ModbusDataFrameFactory(AforeT6Registers.registers).createDataFrames()
    private var requestEcho = Byte.MIN_VALUE

    override suspend fun getData(): Collection<AforeData> {
        val modbusFrames = dataFrames.map { ModbusFrame.readInputRegisters(slaveAddress, it) }
        val solarmanFrames = modbusFrames.map {
            SolarmanV5Frame(requestEcho++, deviceSN, it)
        }

        val results = sendFrames(solarmanFrames)
        val merged: ModbusRegisterData = results.flatMap { it.entries }.associate { it.key to it.value }
        return listOf(AforeData(deviceSN, merged))
    }

    private suspend fun sendFrames(frames: List<Frame<ModbusRegisterData>>): Collection<ModbusRegisterData> {
        return frames.mapIndexed { index, frame ->
            try {
                val response = client.send(frame)
                if (response == null) {
                    log.warn("Received null response for frame {} ({} of {})", frame, index + 1, frames.size)
                    mapOf()
                } else response
            } catch (e: Exception) {
                throw IllegalStateException("Error while sending frame ${index + 1}/${frames.size}", e)
            }
        }
    }

    companion object {
        private val log by Logger()
    }
}
