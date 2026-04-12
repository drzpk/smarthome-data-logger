package dev.drzepka.smarthome.logger.sensors

import dev.drzepka.smarthome.logger.DataLoggerModule
import org.koin.dsl.bind
import org.koin.dsl.module

val sensorsModule = module {
    single { SensorsModule(get(), get()) } bind DataLoggerModule::class
}
