/*
 * Copyright (C) 2026 Phal AS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.podbike.app.data.bluetooth.values

import java.util.UUID

class HaarekBoardSpec
private constructor() {
    companion object {
        val PODBIKE_SERVICE_UUID: UUID = UUID.fromString("00001500-a619-448c-b990-2d716cba1130")

        val SPEED_CHARACTERISTIC_UUID: UUID =
            UUID.fromString("00001501-a619-448c-b990-2d716cba1130")
        val TEMPERATURE_CHARACTERISTIC_UUID: UUID =
            UUID.fromString("00001502-a619-448c-b990-2d716cba1130")
        val GENERATED_POWER_CHARACTERISTIC_UUID: UUID =
            UUID.fromString("00001503-a619-448c-b990-2d716cba1130")
        val AVERAGE_CADENCE_CHARACTERISTIC_UUID: UUID =
            UUID.fromString("00001504-a619-448c-b990-2d716cba1130")
        val ASSIST_SETTING_CHARACTERISTIC_UUID: UUID =
            UUID.fromString("00001505-a619-448c-b990-2d716cba1130")
        val CADENCE_SETTING_CHARACTERISTIC_UUID: UUID =
            UUID.fromString("00001506-a619-448c-b990-2d716cba1130")
        val BATTERY_SOC_CHARACTERISTIC_UUID: UUID =
            UUID.fromString("00001507-a619-448c-b990-2d716cba1130")
        val LIGHT_STATUS_CHARACTERISTIC_UUID: UUID =
            UUID.fromString("00001508-a619-448c-b990-2d716cba1130")
        val TIME_SYNC_CHARACTERISTIC_UUID: UUID =
            UUID.fromString("00001509-a619-448c-b990-2d716cba1130")
        val FTP_CONTROL_CHARACTERISTIC_UUID: UUID =
            UUID.fromString("00001510-a619-448c-b990-2d716cba1130")
        val FTP_DATA_CHARACTERISTIC_UUID: UUID =
            UUID.fromString("00001511-a619-448c-b990-2d716cba1130")
        val AVERAGE_SPEED_CHARACTERISTIC_UUID: UUID =
            UUID.fromString("00001512-a619-448c-b990-2d716cba1130")
        val ACCESSORIES_CHARACTERISTIC_UUID: UUID =
            UUID.fromString("00001513-a619-448c-b990-2d716cba1130")
        val BICYCLE_CHARACTERISTIC_UUID: UUID =
            UUID.fromString("00001514-a619-448c-b990-2d716cba1130")
        val DISTANCE_CHARACTERISTIC_UUID: UUID =
            UUID.fromString("00001515-a619-448c-b990-2d716cba1130")
        val RANGE_CHARACTERISTIC_UUID: UUID =
            UUID.fromString("00001516-a619-448c-b990-2d716cba1130")
        val RX_CHARACTERISTIC_UUID: UUID = UUID.fromString("00001520-a619-448c-b990-2d716cba1130")
        val TX_CHARACTERISTIC_UUID: UUID = UUID.fromString("00001521-a619-448c-b990-2d716cba1130")
    }
}
