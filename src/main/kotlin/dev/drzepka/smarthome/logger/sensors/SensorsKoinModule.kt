package dev.drzepka.smarthome.logger.sensors

import dev.drzepka.smarthome.logger.DataLoggerModule
import dev.drzepka.smarthome.logger.core.config.OfflineDeviceProperties
import dev.drzepka.smarthome.logger.core.device.DeviceManager
import dev.drzepka.smarthome.logger.core.device.OfflineDeviceManager
import dev.drzepka.smarthome.logger.core.device.OnlineDeviceManager
import dev.drzepka.smarthome.logger.core.network.SensorsRequestExecutor
import dev.drzepka.smarthome.logger.core.pipeline.component.DataSender
import dev.drzepka.smarthome.logger.sensors.model.config.SensorsConfig
import dev.drzepka.smarthome.logger.sensors.pipeline.SensorsDataSender
import org.koin.dsl.bind
import org.koin.dsl.module

val sensorsModule = module {
    single { SensorsModule(get()) } bind DataLoggerModule::class
    single { SensorsConfig.load(get()) }
    single { SensorsDataSender(get()) } bind DataSender::class
    single { SensorsRequestExecutor(get<SensorsConfig>()!!, 3) }
    single<DeviceManager> {
        val props = OfflineDeviceProperties(get())
        if (props.enabled)
            OfflineDeviceManager(props)
        else
            OnlineDeviceManager(get(), get())
    }
}
