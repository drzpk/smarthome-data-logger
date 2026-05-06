package dev.drzepka.smarthome.logger.pv.source.sofar

import dev.drzepka.smarthome.logger.core.config.ConfigPropertySource
import dev.drzepka.smarthome.logger.core.model.SourceType
import dev.drzepka.smarthome.logger.core.pipeline.Pipeline
import dev.drzepka.smarthome.logger.core.pipeline.PipelineFactory
import dev.drzepka.smarthome.logger.core.pipeline.component.DataCollector
import dev.drzepka.smarthome.logger.core.pipeline.component.datasource.FixedRateDataSource
import dev.drzepka.smarthome.logger.core.transport.SocketClient
import dev.drzepka.smarthome.logger.pv.common.PvData
import java.time.Duration

abstract class SofarPipelineFactory : PipelineFactory {

    protected fun createPipeline(name: String, interval: Duration, collector: DataCollector<PvData>): Pipeline {
        val dataSource = FixedRateDataSource(
            name = name,
            interval = interval,
            collector = collector,
            decoder = SofarDecoder
        )
        return Pipeline(name).also { it.addDataSource(dataSource) }
    }
}

class SofarWifiPipelineFactory : SofarPipelineFactory() {
    override val sourceType = SourceType.SOFAR_WIFI

    override fun create(name: String, properties: ConfigPropertySource): Pipeline {
        val config = SofarWifiConfig(name, properties)
        val client = SocketClient(config.host, config.port, Duration.ofSeconds(10))
        val collector = SofarWifiDataCollector(client, config.sn)
        return createPipeline(name, Duration.ofSeconds(config.metricsInterval.toLong()), collector)
    }
}

class SofarModbusPipelineFactory : SofarPipelineFactory() {
    override val sourceType = SourceType.SOFAR_MODBUS

    override fun create(name: String, properties: ConfigPropertySource): Pipeline {
        val config = SofarModbusConfig(name, properties)
        val collector = SofarModbusDataCollector(config.device, config.slaveId)
        return createPipeline(name, Duration.ofSeconds(config.metricsInterval.toLong()), collector)
    }
}
