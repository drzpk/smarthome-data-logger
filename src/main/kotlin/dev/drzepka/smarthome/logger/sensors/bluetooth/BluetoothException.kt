package dev.drzepka.smarthome.logger.sensors.bluetooth

class BluetoothException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}