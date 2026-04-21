package dev.drzepka.smarthome.logger.pv.source.afore

import dev.drzepka.smarthome.logger.core.frame.modbus.ModbusRegisterData

data class AforeData(val sn: Long, val registerData: ModbusRegisterData)
