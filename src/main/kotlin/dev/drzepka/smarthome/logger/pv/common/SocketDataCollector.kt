package dev.drzepka.smarthome.logger.pv.common

import dev.drzepka.smarthome.logger.core.frame.Frame
import dev.drzepka.smarthome.logger.core.frame.modbus.ModbusRegisterData
import dev.drzepka.smarthome.logger.core.pipeline.component.DataCollector
import dev.drzepka.smarthome.logger.core.transport.SocketClient
import dev.drzepka.smarthome.logger.core.util.Logger
import dev.drzepka.smarthome.logger.pv.vendor.SolarmanV5Frame

abstract class SocketDataCollector(
    protected val client: SocketClient,
    protected val deviceSn: Long,
) : DataCollector<PvData> {

    private var requestEcho = Byte.MIN_VALUE

    protected abstract fun createModbusFrames(): List<Frame<ModbusRegisterData>>

    override suspend fun getData(): Collection<PvData> {
        val solarmanFrames = createModbusFrames().map { SolarmanV5Frame(requestEcho++, deviceSn, it) }

        val results = sendFrames(solarmanFrames)
        val merged: ModbusRegisterData = results.flatMap { it.entries }.associate { it.key to it.value }
        return listOf(PvData(deviceSn.toString(), merged))
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
