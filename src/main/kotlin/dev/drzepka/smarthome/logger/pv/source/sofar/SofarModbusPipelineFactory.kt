package dev.drzepka.smarthome.logger.pv.source.sofar

import dev.drzepka.smarthome.logger.core.config.ConfigPropertySource
import dev.drzepka.smarthome.logger.core.model.SourceType
import dev.drzepka.smarthome.logger.core.pipeline.Pipeline
import dev.drzepka.smarthome.logger.core.pipeline.PipelineFactory
import dev.drzepka.smarthome.logger.core.pipeline.component.datasource.FixedRateDataSource
import java.time.Duration

class SofarModbusPipelineFactory : PipelineFactory {
    override val sourceType = SourceType.SOFAR_MODBUS

    override fun create(name: String, properties: ConfigPropertySource): Pipeline {
        val config = SofarModbusConfig(name, properties)
        val collector = SofarModbusDataCollector(config.device, config.slaveId)
        val dataSource = FixedRateDataSource(
            name = name,
            interval = Duration.ofSeconds(config.metricsInterval.toLong()),
            collector = collector,
            decoder = SofarDecoder(config.device)
        )
        return Pipeline(name).also { it.addDataSource(dataSource) }
    }
}
