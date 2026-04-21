package dev.drzepka.smarthome.logger.pv

import dev.drzepka.smarthome.common.TaskScheduler
import dev.drzepka.smarthome.logger.core.config.ConfigPropertySource
import dev.drzepka.smarthome.logger.core.pipeline.component.datasource.DataSource
import dev.drzepka.smarthome.logger.core.pipeline.component.datasource.FixedRateDataSource
import dev.drzepka.smarthome.logger.pv.client.SocketClient
import dev.drzepka.smarthome.logger.pv.source.afore.AforeConfig
import dev.drzepka.smarthome.logger.pv.source.afore.AforeDataCollector
import dev.drzepka.smarthome.logger.pv.source.afore.AforeT6Decoder
import dev.drzepka.smarthome.logger.pvstats.model.config.source.SourceConfigFactory
import java.time.Duration

class PvDataSourceFactory(
    private val configPropertySource: ConfigPropertySource,
    private val scheduler: TaskScheduler
) {

    fun createDataSources(): List<DataSource<*>> {
        val dataSources = mutableListOf<DataSource<*>>()

        val sourceNames = SourceConfigFactory.getAvailableNames(configPropertySource)
        for (it in sourceNames) {
            val config = SourceConfigFactory.createSourceConfig(it, configPropertySource)
            if (config !is AforeConfig)
                continue

            dataSources.add(createAforeT6DataSource(config))
        }

        return dataSources
    }

    private fun createAforeT6DataSource(config: AforeConfig): DataSource<*> {
        val parts = config.url.split(":")
        val socketClient = SocketClient(parts[0], parts[1].toInt(), Duration.ofSeconds(3))
        // todo: slave address
        val aforeCollector = AforeDataCollector(socketClient, 1, config.sn!!)

        return FixedRateDataSource(
            name = config.name,
            interval = Duration.ofSeconds(config.measurementInterval?.toLong() ?: 60),
            scheduler = scheduler,
            collector = aforeCollector,
            decoder = AforeT6Decoder
        )
    }
}
