package dev.drzepka.smarthome.logger.sensors.pipeline.datasource

import dev.drzepka.smarthome.logger.core.pipeline.PipelineContext
import dev.drzepka.smarthome.logger.core.pipeline.component.datasource.ListenerDataSource
import dev.drzepka.smarthome.logger.sensors.core.DeviceManager
import dev.drzepka.smarthome.logger.sensors.model.bluetooth.BluetoothServiceData
import dev.drzepka.smarthome.logger.sensors.model.server.Measurement
import dev.drzepka.smarthome.logger.sensors.pipeline.decoder.BluetoothServiceDataDecoder
import dev.drzepka.smarthome.logger.sensors.pipeline.listener.bluetooth.BluetoothCtlBluetoothListener
import dev.drzepka.smarthome.logger.sensors.pipeline.listener.bluetooth.MockBluetoothListener

class BluetoothDataSource(private val deviceManager: DeviceManager, useMock: Boolean = false) :
    ListenerDataSource<BluetoothServiceData, Measurement>(
        "bluetooth",
        if (useMock) MockBluetoothListener() else BluetoothCtlBluetoothListener(),
        BluetoothServiceDataDecoder(deviceManager)
    ) {

    override fun start(context: PipelineContext) {
        super.start(context)
        deviceManager.start(context)
    }

    override fun stop(context: PipelineContext) {
        super.stop(context)
        deviceManager.stop(context)
    }
}
