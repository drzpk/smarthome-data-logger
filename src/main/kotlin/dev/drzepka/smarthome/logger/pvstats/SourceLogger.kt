package dev.drzepka.smarthome.logger.pvstats

import com.fasterxml.jackson.databind.ObjectMapper
import dev.drzepka.smarthome.common.pvstats.model.PutDataRequest
import dev.drzepka.smarthome.common.pvstats.model.vendor.VendorData
import dev.drzepka.smarthome.logger.pvstats.connector.SMAConnector
import dev.drzepka.smarthome.logger.pvstats.connector.SofarModbusConnector
import dev.drzepka.smarthome.logger.pvstats.connector.SofarWifiConnector
import dev.drzepka.smarthome.logger.pvstats.connector.base.Connector
import dev.drzepka.smarthome.logger.pvstats.connector.base.DataType
import dev.drzepka.smarthome.logger.pvstats.model.config.PvStatsConfig
import dev.drzepka.smarthome.logger.pvstats.model.config.source.SMAConfig
import dev.drzepka.smarthome.logger.pvstats.model.config.source.SofarModbusConfig
import dev.drzepka.smarthome.logger.pvstats.model.config.source.SofarWifiConfig
import dev.drzepka.smarthome.logger.pvstats.model.config.source.SourceConfig
import dev.drzepka.smarthome.logger.core.util.Logger
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.logging.Level
import kotlin.math.floor
import kotlin.system.exitProcess

class SourceLogger(private val pvStatsConfig: PvStatsConfig, private val sourceConfig: SourceConfig) {

    private val log by Logger()
    private val objectMapper = ObjectMapper()

    private val connector = getConnector(sourceConfig)
    private val connectorThrottling: Int

    private var throttle = false
    private var connectorErrorCount = 0
    private var throttlingCountdown = 0

    private val endpointUrl: String = pvStatsConfig.url.toString() + "/api/data"
    private val authorizationHeader: String

    init {
        val authData = sourceConfig.user + ":" + sourceConfig.password
        authorizationHeader = "Basic " + Base64.getEncoder().encodeToString(authData.toByteArray())

        val throttledInterval = 2 * 60 // seconds
        // Consecutive error responses will cause connector to throttle fetch request frequency.
        // Throttling is meant to be used for requests with high frequency (more frequent than throttledInterval),
        // those with lower frequency won't be throttled at all
        val minInterval = getIntervals().map { it.value }.min()!!
        connectorThrottling = floor(throttledInterval.toFloat() / minInterval).toInt()
    }

    fun getIntervals(): Map<DataType, Int> = connector.supportedDataTypes.map {
        val interval = when (it) {
            DataType.METRICS -> sourceConfig.metricsInterval
            DataType.MEASUREMENT -> sourceConfig.measurementInterval
        } ?: throw IllegalArgumentException("interval of type $it" +
                " is required by device ${sourceConfig.type} but is missing")

        Pair(it, interval)
    }.toMap()

    fun execute(dataType: DataType) {
        try {
            doExecute(dataType)
        } catch (e: Exception) {
            log.log(Level.SEVERE, "Unexpected exception caught during execution of logger for source {${sourceConfig.name}", e)
        } catch (t: Throwable) {
            log.log(Level.SEVERE, "Unrecoverable exception caught", t)
            exitProcess(1)
        }
    }

    private fun getConnector(sourceConfig: SourceConfig): Connector {
        val connector = when (sourceConfig) {
            is SMAConfig -> SMAConnector(sourceConfig)
            is SofarWifiConfig -> SofarWifiConnector(sourceConfig)
            is SofarModbusConfig -> SofarModbusConnector(sourceConfig)
            else -> throw IllegalStateException("Unsupported source config type: ${sourceConfig.type}")
        }

        log.info("Initializing source '${sourceConfig.name}'")
        connector.initialize()
        return connector
    }

    private fun doExecute(dataType: DataType) {
        if (throttlingCountdown > 0) {
            throttlingCountdown--
            return
        }
        if (throttlingCountdown == 0 && throttle)
            throttlingCountdown = connectorThrottling

        val dataSent = try {
            sendData(dataType)
        } catch (e: Exception) {
            if (!throttle)
                log.log(Level.SEVERE, "Error while collecting data for source ${sourceConfig.name}", e)
            false
        }

        if (!dataSent) {
            connectorErrorCount++
            if (connectorErrorCount == 3) {
                log.warning("Inverter responded with error 3 times in a row, increasing request interval")
                log.info("Subsequent inverter connection errors won't be logged")
                throttle = true
                throttlingCountdown = connectorThrottling
            }
        } else {
            if (connectorErrorCount > 2) {
                log.info("Inverter responded normally after $connectorErrorCount errors, restoring normal request interval")
                throttle = false
                throttlingCountdown = 0
            }

            connectorErrorCount = 0
        }
    }

    private fun sendData(dataType: DataType): Boolean {
        val data = connector.getData(dataType, throttlingCountdown > 0) ?: return false
        sendData(data)
        return true
    }

    private fun sendData(data: VendorData) {
        val url = URL(endpointUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = pvStatsConfig.timeout * 1000
        connection.readTimeout = pvStatsConfig.timeout * 1000
        connection.setRequestProperty("Authorization", authorizationHeader)
        connection.requestMethod = "PUT"
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json")

        val writer = OutputStreamWriter(connection.outputStream)
        writer.write(prepareRequest(data))
        writer.close()

        if (connection.responseCode != 201)
            log.warning("Data sent failed: server returned with HTTP code ${connection.responseCode}")
    }

    private fun prepareRequest(data: VendorData): String {
        val request = PutDataRequest()
        request.type = sourceConfig.type.deviceType
        request.data = data.serialize()
        return objectMapper.writeValueAsString(request)
    }
}