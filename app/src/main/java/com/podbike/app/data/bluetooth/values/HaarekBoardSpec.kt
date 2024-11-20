package com.podbike.app.data.bluetooth.values

import java.util.UUID

class HaarekBoardSpec
private constructor() {
    companion object {

        val RX_CHARACTERISTIC_UUID: UUID = UUID.nameUUIDFromBytes(byteArrayOf(0x15, 0x20))
        val TX_CHARACTERISTIC_UUID: UUID = UUID.nameUUIDFromBytes(byteArrayOf(0x15, 0x21))
        val SPEED_CHARACTERISTIC_UUID: UUID = UUID.nameUUIDFromBytes(byteArrayOf(0x15, 0x01))
        val TEMPERATURE_CHARACTERISTIC_UUID: UUID = UUID.nameUUIDFromBytes(byteArrayOf(0x15, 0x02))
        val GENERATED_POWER_CHARACTERISTIC_UUID: UUID = UUID.nameUUIDFromBytes(byteArrayOf(0x15, 0x03))
        val AVERAGE_CADENCE_CHARACTERISTIC_UUID: UUID = UUID.nameUUIDFromBytes(byteArrayOf(0x15, 0x04))
        val ASSIST_SETTING_CHARACTERISTIC_UUID: UUID = UUID.nameUUIDFromBytes(byteArrayOf(0x15, 0x05))
        val CADENCE_SETTING_CHARACTERISTIC_UUID: UUID = UUID.nameUUIDFromBytes(byteArrayOf(0x15, 0x06))
        val BATTERY_SOC_CHARACTERISTIC_UUID: UUID = UUID.nameUUIDFromBytes(byteArrayOf(0x15, 0x07))
        val LIGHT_STATUS_CHARACTERISTIC_UUID: UUID = UUID.nameUUIDFromBytes(byteArrayOf(0x15, 0x08))
        val TIME_SYNC_CHARACTERISTIC_UUID: UUID = UUID.nameUUIDFromBytes(byteArrayOf(0x15, 0x09))
        val FTP_CONTROL_CHARACTERISTIC_UUID: UUID = UUID.nameUUIDFromBytes(byteArrayOf(0x15, 0x10))
        val FTP_DATA_CHARACTERISTIC_UUID: UUID = UUID.nameUUIDFromBytes(byteArrayOf(0x15, 0x11))
        val AVERAGE_SPEED_CHARACTERISTIC_UUID: UUID = UUID.nameUUIDFromBytes(byteArrayOf(0x15, 0x12))
        val ACCESSORIES_CHARACTERISTIC_UUID: UUID = UUID.nameUUIDFromBytes(byteArrayOf(0x15, 0x13))
        val BICYCLE_CHARACTERISTIC_UUID: UUID = UUID.nameUUIDFromBytes(byteArrayOf(0x15, 0x14))
        val DISTANCE_CHARACTERISTIC_UUID: UUID = UUID.nameUUIDFromBytes(byteArrayOf(0x15, 0x15))
        val RANGE_CHARACTERISTIC_UUID: UUID = UUID.nameUUIDFromBytes(byteArrayOf(0x15, 0x16))
    }
}
