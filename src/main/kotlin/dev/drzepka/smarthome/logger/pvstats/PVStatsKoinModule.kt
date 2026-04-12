package dev.drzepka.smarthome.logger.pvstats

import dev.drzepka.smarthome.logger.DataLoggerModule
import org.koin.dsl.bind
import org.koin.dsl.module

val pvStatsModule = module {
    single { PVStatsModule(get(), get()) } bind DataLoggerModule::class
}
