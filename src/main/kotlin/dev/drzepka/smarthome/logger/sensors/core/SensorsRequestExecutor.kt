package dev.drzepka.smarthome.logger.sensors.core

import dev.drzepka.smarthome.common.util.Mockable
import dev.drzepka.smarthome.logger.core.executor.RequestExecutor
import dev.drzepka.smarthome.logger.sensors.model.config.SensorsConfig
import dev.drzepka.smarthome.logger.sensors.model.server.CreateMeasurementsRequest
import dev.drzepka.smarthome.logger.sensors.model.server.CreateMeasurementsResponse
import dev.drzepka.smarthome.logger.sensors.model.server.Device

@Mockable
class SensorsRequestExecutor(sensorsConfig: SensorsConfig, timeoutSeconds: Int) :
    RequestExecutor(sensorsConfig.serverUrl, timeoutSeconds) {

    init {
        basicAuthorization(sensorsConfig.loggerId.toString(), sensorsConfig.loggerSecret)
    }

    suspend fun getDevices(): List<Device> {
        return executeRequest<Any, List<Device>>("GET", "/api/devices", null)
    }

    suspend fun sendMeasurements(request: CreateMeasurementsRequest): CreateMeasurementsResponse {
        return executeRequest("POST", "/api/measurements", request)
    }
}