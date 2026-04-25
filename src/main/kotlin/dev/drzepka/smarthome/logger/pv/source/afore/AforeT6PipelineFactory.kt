package dev.drzepka.smarthome.logger.pv.source.afore

import dev.drzepka.smarthome.logger.core.config.ConfigPropertySource
import dev.drzepka.smarthome.logger.core.pipeline.Pipeline
import dev.drzepka.smarthome.logger.core.pipeline.PipelineFactory
import dev.drzepka.smarthome.logger.core.pipeline.component.datasource.FixedRateDataSource
import dev.drzepka.smarthome.logger.core.transport.SocketClient
import dev.drzepka.smarthome.logger.pvstats.model.config.SourceType
import java.time.Duration

class AforeT6PipelineFactory : PipelineFactory {
    override val sourceType = SourceType.AFORE_T6

    override fun create(name: String, properties: ConfigPropertySource): Pipeline {
        val config = AforeConfig(name, properties)
        val socketClient = SocketClient(config.host, config.port, Duration.ofSeconds(3))
        val collector = AforeDataCollector(socketClient, config.slaveAddress, config.sn)
        val dataSource = FixedRateDataSource(
            name = name,
            interval = Duration.ofSeconds(config.measurementInterval?.toLong() ?: 60),
            collector = collector,
            decoder = AforeT6Decoder
        )
        return Pipeline(name, Duration.ofSeconds(10)).also {
            it.addDataSource(dataSource)
        }
    }
}
