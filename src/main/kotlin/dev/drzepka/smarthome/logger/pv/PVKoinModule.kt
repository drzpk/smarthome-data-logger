package dev.drzepka.smarthome.logger.pv

import dev.drzepka.smarthome.logger.pv.source.afore.AforeT6PipelineFactory
import org.koin.dsl.module

val pvModule = module {
    single { AforeT6PipelineFactory() }
}
