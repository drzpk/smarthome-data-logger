package dev.drzepka.smarthome.logger.pv

import dev.drzepka.smarthome.logger.DataLoggerModule
import dev.drzepka.smarthome.logger.pv.source.afore.AforeT6PipelineFactory
import org.koin.dsl.bind
import org.koin.dsl.module

val pvModule = module {
    single { PVModule(get(), get(), get()) } bind DataLoggerModule::class
    single { AforeT6PipelineFactory() }
}
