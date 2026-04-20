package dev.drzepka.smarthome.logger.core.pipeline.component.datasource

import dev.drzepka.smarthome.logger.core.model.measurement.Measurement
import dev.drzepka.smarthome.logger.core.model.measurement.TemperatureMeasurement
import dev.drzepka.smarthome.logger.core.pipeline.component.DataDecoder
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import java.math.BigDecimal

internal class DataSourceTest {

    @Test
    fun `should forward data to listener`() {
        val source = TestDataSource(true)
        source.testForwardData(listOf("3", "21", "9991"))
        then(source.forwardedData.map { it.mac }).containsExactly("3", "21", "9991")
    }

    @Test
    fun `should do nothing when listener is not set`() {
        val source = TestDataSource(false)
        source.testForwardData(listOf("1", "2"))
        then(source.forwardedData).isEmpty()
    }

    @Test
    fun `should handle decoding errors`() {
        val source = TestDataSource(true)
        source.testForwardData(listOf("1", "not integer", "3"))
        then(source.forwardedData.map { it.mac }).containsExactly("1", "3")
    }

    private class TestDataSource(createListener: Boolean) : DataSource<String>("test", TestDataDecoder()) {
        val forwardedData = mutableListOf<Measurement>()

        init {
            if (createListener) {
                receiver = object : DataReceiver {
                    override fun onDataAvailable(items: Collection<Measurement>) {
                        forwardedData.addAll(items)
                    }
                }
            }
        }

        fun testForwardData(data: Collection<String>) {
            forwardData(data)
        }
    }

    private class TestDataDecoder : DataDecoder<String> {
        override fun decode(item: String): Collection<Measurement> =
            listOf(TemperatureMeasurement(mac = item.toInt().toString(), temperature = BigDecimal(item.toInt())))
    }
}
