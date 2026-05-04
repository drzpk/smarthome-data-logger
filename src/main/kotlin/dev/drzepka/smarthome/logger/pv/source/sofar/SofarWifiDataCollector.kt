package dev.drzepka.smarthome.logger.pv.source.sofar

import dev.drzepka.smarthome.logger.core.pipeline.component.DataCollector
import dev.drzepka.smarthome.logger.core.transport.SocketClient
import dev.drzepka.smarthome.logger.pv.vendor.SolarmanV5Frame

class SofarWifiDataCollector(
    private val client: SocketClient,
    private val deviceSn: Long
) : DataCollector<SofarData> {

    private var requestEcho = Byte.MIN_VALUE

    override suspend fun getData(): Collection<SofarData> {
        val frame = SolarmanV5Frame(requestEcho++, deviceSn, SofarWifiFrame())
        val data = client.send(frame) ?: return emptyList()
        return listOf(data)
    }
}
