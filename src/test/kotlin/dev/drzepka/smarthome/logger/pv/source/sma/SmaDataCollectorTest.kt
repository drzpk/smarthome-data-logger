package dev.drzepka.smarthome.logger.pv.source.sma

import kotlinx.coroutines.runBlocking
import org.apache.http.client.ResponseHandler
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.impl.client.CloseableHttpClient
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.doReturn
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any

@ExtendWith(MockitoExtension::class)
internal class SmaDataCollectorTest {

    @Mock
    private lateinit var httpClient: CloseableHttpClient

    private lateinit var collector: SmaDataCollector

    @BeforeEach
    fun setup() {
        collector = SmaDataCollector("https://sma-device", timeout = 5, httpClient = httpClient)
    }

    @Test
    fun `should extract field without factor`(): Unit = runBlocking {
        stubResponse("""{"result":{"uid":{"6100_40263F00":{"1":[{"val":1639}]}}}}""")

        val result = collector.getData().single()

        then(result.values[SmaFields.gridPower]).isEqualTo(1639.0)
    }

    @Test
    fun `should apply factor when extracting field`(): Unit = runBlocking {
        stubResponse("""{"result":{"uid":{"6100_00465700":{"1":[{"val":5000}]}}}}""")

        val result = collector.getData().single()

        then(result.values[SmaFields.frequency]).isEqualTo(50.0)
    }

    @Test
    fun `should extract indexed field for pv string`(): Unit = runBlocking {
        stubResponse("""{"result":{"uid":{"6380_40251E00":{"1":[{"val":300},{"val":450}]}}}}""")

        val result = collector.getData().single()

        then(result.values[SmaFields.pvPowerA]).isEqualTo(300.0)
        then(result.values[SmaFields.pvPowerB]).isEqualTo(450.0)
    }

    @Test
    fun `should omit field when val is null`(): Unit = runBlocking {
        stubResponse("""{"result":{"uid":{"6100_40263F00":{"1":[{"val":null}]}}}}""")

        val result = collector.getData().single()

        then(result.values).doesNotContainKey(SmaFields.gridPower)
    }

    @Test
    fun `should omit field when key is absent from response`(): Unit = runBlocking {
        stubResponse("""{"result":{"uid":{}}}""")

        val result = collector.getData().single()

        then(result.values).doesNotContainKey(SmaFields.gridPower)
    }

    private fun stubResponse(json: String) {
        doReturn(json.toByteArray())
            .`when`(httpClient).execute(any<HttpUriRequest>(), any<ResponseHandler<*>>())
    }
}
