package dev.drzepka.smarthome.logger.pv.source.sofar

import dev.drzepka.smarthome.logger.core.config.ConfigPropertySource
import dev.drzepka.smarthome.logger.core.model.SourceType
import dev.drzepka.smarthome.logger.core.pipeline.Pipeline
import dev.drzepka.smarthome.logger.core.pipeline.PipelineFactory
import dev.drzepka.smarthome.logger.core.pipeline.component.datasource.FixedRateDataSource
import dev.drzepka.smarthome.logger.core.transport.SocketClient
import java.time.Duration

class SofarWifiPipelineFactory : PipelineFactory {
    override val sourceType = SourceType.SOFAR_WIFI

    override fun create(name: String, properties: ConfigPropertySource): Pipeline {
        val config = SofarWifiConfig(name, properties)
        val client = SocketClient(config.host, config.port, Duration.ofSeconds(10))
        val collector = SofarWifiDataCollector(client, config.sn)
        val dataSource = FixedRateDataSource(
            name = name,
            interval = Duration.ofSeconds(config.metricsInterval.toLong()),
            collector = collector,
            decoder = SofarDecoder(config.sn.toString())
        )
        return Pipeline(name).also { it.addDataSource(dataSource) }
    }
}
