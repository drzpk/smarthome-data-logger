package dev.drzepka.smarthome.logger.pv.source.sma

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import dev.drzepka.smarthome.logger.core.pipeline.component.DataCollector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import java.net.URI
import java.time.Instant

class SmaDataCollector(
    private val baseUrl: String,
    timeout: Int,
    private val httpClient: CloseableHttpClient,
) : DataCollector<SmaData> {

    private val requestConfig = RequestConfig.custom()
        .setConnectTimeout(timeout * 1000)
        .setSocketTimeout(timeout * 1000)
        .build()

    override suspend fun getData(): Collection<SmaData> = withContext(Dispatchers.IO) {
        val resultBody = fetchResultBody()
        val values = SmaFields.fields.mapNotNull { field ->
            extractValue(resultBody, field)?.let { field to it }
        }.toMap()
        listOf(SmaData(Instant.now(), values))
    }

    private fun fetchResultBody(): JsonNode {
        val request = HttpPost(URI("$baseUrl/dyn/getDashValues.json")).apply {
            config = requestConfig
            entity = StringEntity("""{"destDev":[],"keys":[]}""", ContentType.APPLICATION_JSON)
        }
        val bytes = httpClient.execute(request) { it.entity.content.use { s -> s.readBytes() } }
        val root = mapper.readTree(bytes)
        val result = root["result"] ?: return mapper.createObjectNode()
        return result.fields().asSequence().firstOrNull()?.value ?: mapper.createObjectNode()
    }

    private fun extractValue(resultBody: JsonNode, field: SmaField): Double? {
        val keyNode = resultBody[field.key] ?: return null
        val arrayNode = keyNode.fields().asSequence().firstOrNull()?.value ?: return null
        if (!arrayNode.isArray) return null
        val entry = arrayNode.get(field.keyIdx) ?: return null
        val valNode = entry["val"] ?: return null
        if (valNode.isNull) return null
        val raw = valNode.doubleValue()
        return if (field.factor != null) raw / field.factor else raw
    }

    companion object {
        private val mapper = ObjectMapper()
    }
}
