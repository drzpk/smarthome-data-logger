package dev.drzepka.smarthome.logger.core.transport

import dev.drzepka.smarthome.logger.core.config.ServerProperties
import dev.drzepka.smarthome.logger.core.executor.RequestExecutor
import dev.drzepka.smarthome.logger.core.model.Device
import dev.drzepka.smarthome.logger.core.model.measurement.CreateMeasurementsRequest
import dev.drzepka.smarthome.logger.core.model.measurement.CreateMeasurementsResponse

class ServerRequestExecutor(properties: ServerProperties) :
    RequestExecutor(properties.serverUrl, properties.timeout) {

    init {
        basicAuthorization(properties.loggerId.toString(), properties.loggerSecret)
    }

    suspend fun getDevices(): List<Device> {
        return executeRequest<Any, List<Device>>("GET", "/api/devices", null)
    }

    suspend fun sendMeasurements(request: CreateMeasurementsRequest): CreateMeasurementsResponse {
        return executeRequest("POST", "/api/measurements", request)
    }
}
