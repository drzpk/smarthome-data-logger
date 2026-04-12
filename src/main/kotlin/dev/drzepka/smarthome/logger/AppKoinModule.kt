package dev.drzepka.smarthome.logger

import dev.drzepka.smarthome.common.TaskScheduler
import dev.drzepka.smarthome.logger.core.config.ConfigurationLoader
import org.koin.dsl.module

val appModule = module {
    single { ConfigurationLoader() }
    single { TaskScheduler(8) }
}
