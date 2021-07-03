package dev.drzepka.smarthome.logger.core.util

import org.slf4j.LoggerFactory
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class Logger : ReadOnlyProperty<Any, org.slf4j.Logger> {

    private var instance: org.slf4j.Logger? = null

    override fun getValue(thisRef: Any, property: KProperty<*>): org.slf4j.Logger {
        if (instance == null)
            instance = LoggerFactory.getLogger(thisRef.javaClass)

        return instance!!
    }
}