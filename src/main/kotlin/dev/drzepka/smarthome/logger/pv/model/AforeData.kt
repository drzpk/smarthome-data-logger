package dev.drzepka.smarthome.logger.pv.model

import dev.drzepka.smarthome.logger.core.frame.modbus.ModbusRegisterData

data class AforeData(val sn: Long, val registerData: ModbusRegisterData)
