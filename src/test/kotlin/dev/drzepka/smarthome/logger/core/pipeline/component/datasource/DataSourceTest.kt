package dev.drzepka.smarthome.logger.core.pipeline.component.datasource

import dev.drzepka.smarthome.logger.core.pipeline.component.DataDecoder
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test

internal class DataSourceTest {

    @Test
    fun `should forward data to listener`() {
        val source = TestDataSource(true)
        source.testForwardData(listOf("3", "21", "9991"))
        then(source.forwardedData).containsExactly(3, 21, 9991)
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
        then(source.forwardedData).containsExactly(1, 3)
    }

    private class TestDataSource(createListener: Boolean) : DataSource<String, Int>("test", TestDataDecoder()) {
        val forwardedData = mutableListOf<Int>()

        init {
            if (createListener) {
                receiver = object : DataReceiver<Int> {
                    override fun onDataAvailable(items: Collection<Int>) {
                        forwardedData.addAll(items)
                    }
                }
            }
        }

        fun testForwardData(data: Collection<String>) {
            forwardData(data)
        }
    }

    private class TestDataDecoder : DataDecoder<String, Int> {
        override fun decode(data: String): List<Int> = listOf(data.toInt())
    }
}
