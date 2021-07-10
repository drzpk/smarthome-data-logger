package dev.drzepka.smarthome.logger.sensors.bluetooth

interface BluetoothFacade {
    fun startListening()
    fun stopListening()

    fun addBroadcastListener(listener: BroadcastListener)
}