package dev.drzepka.smarthome.logger.pv.source.sma

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import dev.drzepka.smarthome.common.pvstats.model.sma.SMAMeasurement
import dev.drzepka.smarthome.logger.core.pipeline.component.DataCollector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import java.net.URI

class SmaMeasurementCollector(
    private val baseUrl: String,
    private val timeout: Int,
    private val httpClient: CloseableHttpClient
) : DataCollector<SmaData> {

    override suspend fun getData(): Collection<SmaData> = withContext(Dispatchers.IO) {
        val bytes = fetch("$baseUrl/dyn/getDashLogger.json")
        val measurement = objectMapper.readValue(bytes, SMAMeasurement::class.java)
        val entries = try {
            measurement.getEntries()
        } catch (_: Exception) {
            return@withContext emptyList()
        }
        entries.mapNotNull { entry ->
            val energy = entry.v ?: return@mapNotNull null
            SmaData.Measurement(entry.t.toInstant(), energy)
        }
    }

    private fun fetch(url: String): ByteArray {
        val config = RequestConfig.custom()
            .setConnectTimeout(timeout * 1000)
            .setSocketTimeout(timeout * 1000)
            .build()
        val request = HttpGet(URI(url)).also { it.config = config }
        return httpClient.execute(request) { it.entity.content.use { s -> s.readBytes() } }
    }

    companion object {
        private val objectMapper = ObjectMapper().apply {
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            findAndRegisterModules()
        }
    }
}
