package dev.drzepka.smarthome.logger.pv

import dev.drzepka.smarthome.logger.DataLoggerModule
import org.koin.dsl.bind
import org.koin.dsl.module

val pvModule = module {
    single { PVModule(get(), get()) } bind DataLoggerModule::class
}
