package dev.drzepka.smarthome.logger.sensors.source.xiaomimijia

import dev.drzepka.smarthome.logger.core.config.ConfigPropertySource
import dev.drzepka.smarthome.logger.core.pipeline.Pipeline
import dev.drzepka.smarthome.logger.core.pipeline.PipelineFactory
import dev.drzepka.smarthome.logger.core.pipeline.component.datasource.ListenerDataSource
import dev.drzepka.smarthome.logger.core.transport.bluetooth.BluetoothCtlBluetoothListener
import dev.drzepka.smarthome.logger.pvstats.model.config.SourceType
import java.time.Duration

class XiaomiMijiaPipelineFactory : PipelineFactory {
    override val sourceType = SourceType.XIAOMI_MIJIA

    override fun create(name: String, properties: ConfigPropertySource): Pipeline {
        val mock = properties.getBoolean("mock", false)
        val listener = if (mock) XiaomiMijiaMockBluetoothListener() else BluetoothCtlBluetoothListener()
        val dataSource = ListenerDataSource(name, listener, BluetoothServiceDataDecoder)
        return Pipeline(name, Duration.ofSeconds(30)).also {
            it.addDataSource(dataSource)
        }
    }
}
