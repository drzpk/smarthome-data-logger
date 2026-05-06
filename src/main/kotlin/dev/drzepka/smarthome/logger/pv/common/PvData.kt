package dev.drzepka.smarthome.logger.pv.common

import dev.drzepka.smarthome.logger.core.frame.modbus.ModbusRegisterData

data class PvData(val deviceId: String, val registerData: ModbusRegisterData)
