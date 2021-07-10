package dev.drzepka.smarthome.logger.sensors.bluetooth

import dev.drzepka.smarthome.logger.sensors.model.bluetooth.BluetoothServiceData

@FunctionalInterface
interface BroadcastListener {
    fun onDataReceived(data: BluetoothServiceData)
}