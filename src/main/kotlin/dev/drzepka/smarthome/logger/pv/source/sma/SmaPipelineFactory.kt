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
        val collector = SmaDataCollector(config.url, config.timeout, httpClient)
        val decoder = SmaDecoder(config.host)
        val pipeline = Pipeline(name)

        pipeline.addDataSource(
            FixedRateDataSource(
                name = name,
                interval = Duration.ofSeconds(config.interval.toLong()),
                collector = collector,
                decoder = decoder
            )
        )

        return pipeline
    }

    private fun createHttpClient(): CloseableHttpClient =
        HttpClients.custom()
            .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
            .setSSLContext(NoopX509TrustManager.sslContext)
            .build()
}
