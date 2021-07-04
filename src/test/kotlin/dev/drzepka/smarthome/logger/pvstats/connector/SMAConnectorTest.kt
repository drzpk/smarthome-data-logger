package dev.drzepka.smarthome.logger.pvstats.connector

import dev.drzepka.smarthome.common.pvstats.model.vendor.SMAData
import dev.drzepka.smarthome.logger.pvstats.PVStatsModule
import dev.drzepka.smarthome.logger.pvstats.connector.base.DataType
import dev.drzepka.smarthome.logger.pvstats.model.config.source.SMAConfig
import dev.drzepka.smarthome.logger.core.config.ConfigurationLoader
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

class SMAConnectorTest {

    @Test
    fun `verify data format`() {
        val realData = getBytes(MEASUREMENT_FILENAME)

        val connector = getConnector()
        val data = connector.parseResponseData(DataType.MEASUREMENT, realData) as SMAData
        Assertions.assertNotEquals(0, data.measurement?.getEntries())

        // 15 April 2020 18:20:00
        val expectedFirstRecordTime = LocalDateTime.of(2020, 4, 15, 18, 20, 0).atZone(ZoneOffset.UTC).toInstant()
        val actualFirstRecordTime = data.measurement?.getEntries()?.first()?.t?.toInstant()
        Assertions.assertEquals(expectedFirstRecordTime.epochSecond, actualFirstRecordTime?.epochSecond)
    }

    @Test
    fun `check getting power`() {
        val connector = getConnector()
        val data = connector.parseResponseData(DataType.METRICS, getBytes(DASH_VALUES_FILENAME)) as SMAData
        Assertions.assertEquals(1639, data.dashValues?.getPower())
    }

    @Test
    fun `check getting null power`() {
        val connector = getConnector()
        val data = connector.parseResponseData(DataType.METRICS, getBytes(DASH_VALUES_NULL_POWER_FILENAME)) as SMAData
        Assertions.assertEquals(0, data.dashValues?.getPower())
    }

    @Test
    fun `check getting device name`() {
        val connector = getConnector()
        val data = connector.parseResponseData(DataType.METRICS, getBytes(DASH_VALUES_FILENAME)) as SMAData
        Assertions.assertEquals("STP4.0-3AV-40 752", data.dashValues?.getDeviceName())
    }

    private fun getConnector(): SMAConnector {
        val properties = Properties()
        properties.setProperty("source.test.url", "localhost")
        properties.setProperty("source.test.user", "user")
        properties.setProperty("source.test.password", "password")
        properties.setProperty("source.test.timeout", "1")

        val config = SMAConfig("test", ConfigurationLoader(properties))
        return SMAConnector(config)
    }

    private fun getBytes(filename: String): ByteArray {
        val stream = PVStatsModule::class.java.classLoader.getResourceAsStream(filename)!!
        val bytes = stream.readBytes()
        stream.close()
        return bytes
    }

    companion object {
        private const val MEASUREMENT_FILENAME = "sma_measurement_data.json"
        private const val DASH_VALUES_FILENAME = "sma_dash_values.json"
        private const val DASH_VALUES_NULL_POWER_FILENAME = "sma_dash_values_null_power.json"
    }
}