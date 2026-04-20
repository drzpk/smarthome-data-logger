package dev.drzepka.smarthome.logger.core.network

import dev.drzepka.smarthome.common.util.Mockable
import dev.drzepka.smarthome.logger.core.executor.RequestExecutor
import dev.drzepka.smarthome.logger.core.model.Device
import dev.drzepka.smarthome.logger.core.model.measurement.CreateMeasurementsRequest
import dev.drzepka.smarthome.logger.core.model.measurement.CreateMeasurementsResponse
import dev.drzepka.smarthome.logger.sensors.model.config.SensorsConfig

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
