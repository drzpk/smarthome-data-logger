package dev.drzepka.smarthome.logger.pv.source.sma

import dev.drzepka.smarthome.logger.core.config.ConfigPropertySource
import dev.drzepka.smarthome.logger.core.model.SourceType
import dev.drzepka.smarthome.logger.core.pipeline.Pipeline
import dev.drzepka.smarthome.logger.core.pipeline.PipelineFactory
import dev.drzepka.smarthome.logger.core.pipeline.component.datasource.FixedRateDataSource
import dev.drzepka.smarthome.logger.core.util.NoopX509TrustManager
import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import java.time.Duration

class SmaPipelineFactory : PipelineFactory {
    override val sourceType = SourceType.SMA

    override fun create(name: String, properties: ConfigPropertySource): Pipeline {
        val config = SmaConfig(name, properties)
        val httpClient = createHttpClient()
        val decoder = SmaDecoder(config.host)
        val pipeline = Pipeline(name)

        val metricsCollector = SmaMetricsCollector(config.url, config.timeout, httpClient)
        pipeline.addDataSource(
            FixedRateDataSource(
                name = "$name-metrics",
                interval = Duration.ofSeconds(config.metricsInterval.toLong()),
                collector = metricsCollector,
                decoder = decoder
            )
        )

        config.measurementInterval?.let { interval ->
            val measurementCollector = SmaMeasurementCollector(config.url, config.timeout, httpClient)
            pipeline.addDataSource(
                FixedRateDataSource(
                    name = "$name-measurement",
                    interval = Duration.ofSeconds(interval.toLong()),
                    collector = measurementCollector,
                    decoder = decoder
                )
            )
        }

        return pipeline
    }

    private fun createHttpClient(): CloseableHttpClient =
        HttpClients.custom()
            .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
            .setSSLContext(NoopX509TrustManager.sslContext)
            .build()
}
