package dev.drzepka.smarthome.logger.sensors

import dev.drzepka.smarthome.logger.DataLoggerModule
import dev.drzepka.smarthome.logger.sensors.core.DeviceManager
import dev.drzepka.smarthome.logger.sensors.core.SensorsRequestExecutor
import dev.drzepka.smarthome.logger.sensors.model.config.SensorsConfig
import dev.drzepka.smarthome.logger.sensors.pipeline.SensorsDataSender
import org.koin.dsl.bind
import org.koin.dsl.module

val sensorsModule = module {
    single { SensorsModule(get(), get()) } bind DataLoggerModule::class
    single { SensorsConfig.load(get())!! }
    single { SensorsRequestExecutor(get<SensorsConfig>()!!, 3) }
    single { SensorsDataSender(get()) }
    single { DeviceManager(get()) }
}
